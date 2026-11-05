import { useState } from 'react';
import { api } from './api';
import type { Translate } from './Orders';

type Row = { name: string; price: string; category: string; description: string };
const emptyRow = (): Row => ({ name: '', price: '', category: '', description: '' });
function priceValue(value: string) {
  return Number(value.trim().replace(/[٠-٩]/g, digit => String('٠١٢٣٤٥٦٧٨٩'.indexOf(digit))));
}

export default function BulkMenuEditor({ t, endpoint, onChanged }: {
  t: Translate; endpoint: string; onChanged: () => Promise<void>;
}) {
  const [rows, setRows] = useState<Row[]>([emptyRow()]);
  const [paste, setPaste] = useState('');
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  function update(index: number, key: keyof Row, value: string) {
    setRows(current => current.map((row, i) => i === index ? { ...row, [key]: value } : row));
    setMessage('');
  }
  function importPaste() {
    const lines = paste.trim().split(/\r?\n/).filter(line => line.trim());
    if (!lines.length) return;
    if (lines.some(line => { const count = line.split('\t').length; return count < 2 || count > 4; })) {
      setMessage(t('الصق من عمودين إلى أربعة أعمدة من جدول: الاسم، السعر، القسم، الوصف. بدون عناوين.', 'Paste two to four spreadsheet columns: name, price, category, description. Exclude headers.'));
      return;
    }
    const imported = lines.map(line => {
      const [name = '', price = '', category = '', description = ''] = line.split('\t');
      return { name: name.trim(), price: price.trim(), category: category.trim(), description: description.trim() };
    });
    setRows(current => [...current.filter(row => Object.values(row).some(value => value.trim())), ...imported]);
    setPaste(''); setMessage('');
  }
  async function save() {
    if (busy) return;
    if (!rows.length || rows.some(row => !row.name.trim() || !Number.isSafeInteger(priceValue(row.price)) || priceValue(row.price) < 1 || row.category.trim().length > 100)) {
      setMessage(t('راجع كل صف: الاسم مطلوب، السعر عدد صحيح أكبر من صفر، والقسم حتى ١٠٠ حرف.', 'Check every row: a name is required, price must be a positive whole number, and category is limited to 100 characters.'));
      return;
    }
    setBusy(true); setMessage('');
    let saved = 0;
    try {
      for (const row of rows) {
        await api(`${endpoint}/menu`, { method: 'POST', body: JSON.stringify({ name: row.name.trim(), price: priceValue(row.price), category: row.category.trim(), description: row.description.trim() }) }, true);
        saved++;
        setRows(current => current.slice(1));
      }
      setMessage(t(`تم حفظ ${saved} صنف.`, `Saved ${saved} items.`));
    } catch (error) {
      setMessage(t(`تم حفظ ${saved} صنف. توقف الحفظ؛ راجع المنيو قبل إعادة المحاولة لتجنب التكرار. `, `Saved ${saved} items. Saving stopped; check the menu before retrying to avoid duplicates. `) + (error instanceof Error ? error.message : ''));
    } finally {
      try { if (saved) await onChanged(); }
      catch { setMessage(t('تم حفظ أصناف، لكن تعذر تحديث العرض. أعد تحميل الصفحة لمراجعة المنيو.', 'Items were saved, but the view could not refresh. Reload the page to review the menu.')); }
      setBusy(false);
    }
  }
  return <details className="panel bulk-menu-editor">
    <summary>{t('إضافة أصناف بالجملة', 'Add menu items in bulk')}</summary>
    <p className="muted">{t('انقل البيانات من صورة المنيو إلى الصفوف وراجع الأسعار. أضف سنجل ودبل وتربل كأصناف منفصلة حالياً.', 'Enter the menu photo’s details below and review prices. Add single, double and triple as separate items for now.')}</p>
    <fieldset disabled={busy}>
      <label>{t('لصق من جدول (بدون صف العناوين)', 'Paste from a spreadsheet (without headers)')}
        <textarea value={paste} onChange={e => setPaste(e.target.value)} placeholder={t('الاسم ← السعر ← القسم ← الوصف (أعمدة من Excel)', 'Name → Price → Category → Description (columns from Excel)')}/>
      </label>
      <button type="button" onClick={importPaste} disabled={!paste.trim()}>{t('إضافة للمعاينة', 'Add to preview')}</button>
      <div className="bulk-menu-scroll"><table><thead><tr>{[t('الاسم', 'Name'), t('السعر بالجنيه', 'Price (EGP)'), t('القسم', 'Category'), t('الوصف', 'Description'), t('حذف', 'Remove')].map(label => <th key={label} scope="col">{label}</th>)}</tr></thead>
        <tbody>{rows.map((row, index) => <tr key={index}>{(['name', 'price', 'category', 'description'] as const).map((key, column) => <td key={key}><input aria-label={`${[t('الاسم', 'Name'), t('السعر', 'Price'), t('القسم', 'Category'), t('الوصف', 'Description')][column]} ${index + 1}`} value={row[key]} inputMode={key === 'price' ? 'numeric' : undefined} maxLength={key === 'category' ? 100 : undefined} onChange={e => update(index, key, e.target.value)}/></td>)}<td><button type="button" aria-label={`${t('حذف الصف', 'Remove row')} ${index + 1}`} onClick={() => setRows(current => current.filter((_, i) => i !== index))}>×</button></td></tr>)}</tbody>
      </table></div>
      <div className="button-row"><button type="button" onClick={() => setRows(current => [...current, emptyRow()])}>{t('إضافة صف', 'Add row')}</button><button type="button" className="primary" disabled={!rows.length} onClick={() => void save()}>{busy ? t('جاري الحفظ…', 'Saving…') : t(`حفظ ${rows.length} صنف`, `Save ${rows.length} items`)}</button></div>
    </fieldset>
    {message && <p role="status">{message}</p>}
  </details>;
}
