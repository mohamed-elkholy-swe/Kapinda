import { useState } from 'react';
import { api } from './api';
import { menuCategoryDefinitions, menuCategoryKey } from './menuCategories';
import type { Restaurant } from './types';
import type { Translate } from './Orders';

export default function MenuCategoryEditor({ t, restaurant, initialCategory, endpoint, onChanged, createOnly = false }: {
  t: Translate; restaurant: Restaurant; initialCategory?: string | null; endpoint: string; onChanged: () => Promise<void>; createOnly?: boolean;
}) {
  const [category, setCategory] = useState(initialCategory?.trim() || '');
  const [name, setName] = useState('');
  const [nameEn, setNameEn] = useState('');
  const [adding, setAdding] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [created, setCreated] = useState<ReturnType<typeof menuCategoryDefinitions>>([]);
  const categories = [...new Map([...menuCategoryDefinitions(restaurant), ...created].map(c => [c.id, c])).values()];
  async function addCategory() {
    if (!name.trim()) return;
    setBusy(true); setError('');
    try {
      const updated = await api<Restaurant>(`${endpoint}/menu-categories`, { method: 'POST', body: JSON.stringify({ nameAr: name.trim(), nameEn: nameEn.trim() || null }) }, true);
      const definitions = menuCategoryDefinitions(updated);
      setCreated(definitions);
      const chosen = definitions.find(c => menuCategoryKey(c.nameAr) === menuCategoryKey(name) || (nameEn.trim() && c.nameEn && menuCategoryKey(c.nameEn) === menuCategoryKey(nameEn)));
      if (chosen) setCategory(chosen.id);
      setName(''); setNameEn(''); setAdding(false);
      await onChanged(); setCreated([]);
    } catch (e) { setError(e instanceof Error ? e.message : 'Request failed'); }
    finally { setBusy(false); }
  }
  return <div className="menu-category-editor">
    {!createOnly && <label>{t('قسم المنيو (اختياري)', 'Menu category (optional)')}
      <select name="category" value={categories.some(c => c.id === category) ? category : ''} onChange={e => setCategory(e.target.value)}>
        <option value="">{t('إضافة مباشرة بدون قسم', 'Add directly without a category')}</option>
        {categories.map(value => <option key={value.id} value={value.id}>{t(value.nameAr, value.nameEn || value.nameAr)}</option>)}
      </select>
    </label>}
    <button type="button" disabled={busy} aria-expanded={adding} onClick={() => setAdding(value => !value)}>{adding ? t('إلغاء إضافة القسم', 'Cancel new category') : t('إضافة قسم جديد', 'Add new category')}</button>
    {adding && <fieldset className="new-restaurant-category" disabled={busy}><legend>{t('قسم جديد', 'New menu category')}</legend>
      <label>{t('الاسم بالعربية (مطلوب)', 'Arabic name (required)')}<input value={name} maxLength={100} dir="rtl" onChange={e => setName(e.target.value)}/></label>
      <label>{t('الاسم بالإنجليزية (اختياري)', 'English name (optional)')}<input value={nameEn} maxLength={100} dir="ltr" onChange={e => setNameEn(e.target.value)}/></label>
      <button type="button" disabled={busy || !name.trim()} onClick={() => void addCategory()}>{busy ? t('جاري الحفظ…', 'Saving…') : t('إضافة القسم', 'Add category')}</button>
    </fieldset>}
    {error && <p className="alert" role="alert">{error}</p>}
  </div>;
}
