import { useState } from 'react';
import { api } from './api';
import type { Restaurant } from './types';
import type { Translate } from './Orders';

export default function MenuCategoryEditor({ t, restaurant, initialCategory, endpoint, onChanged }: {
  t: Translate; restaurant: Restaurant; initialCategory?: string | null; endpoint: string; onChanged: () => Promise<void>;
}) {
  const [category, setCategory] = useState(initialCategory?.trim() || '');
  const [name, setName] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const categories = [...new Set([...(restaurant.details?.menuCategories || []).map(value => value.trim()), ...restaurant.menu.map(item => item.category?.trim() || '')].filter(Boolean))];
  async function addCategory() {
    if (!name.trim()) return;
    setBusy(true); setError('');
    try {
      await api(`${endpoint}/menu-categories`, { method: 'POST', body: JSON.stringify({ name: name.trim() }) }, true);
      await onChanged(); setCategory(name.trim()); setName('');
    } catch (e) { setError(e instanceof Error ? e.message : 'Request failed'); }
    finally { setBusy(false); }
  }
  return <div className="menu-category-editor">
    <label>{t('قسم المنيو (اختياري)', 'Menu category (optional)')}
      <select name="category" value={categories.includes(category) ? category : ''} disabled={busy} onChange={e => setCategory(e.target.value)}>
        <option value="">{t('إضافة مباشرة بدون قسم', 'Add directly without a category')}</option>
        {categories.map(value => <option key={value} value={value}>{value}</option>)}
      </select>
    </label>
    <div className="form-columns">
      <label>{t('قسم جديد', 'New category')}<input value={name} maxLength={100} onChange={e => setName(e.target.value)} placeholder={t('مثال: بيتزا أو مشروبات', 'e.g. Pizza or drinks')}/></label>
      <button type="button" disabled={busy || !name.trim()} onClick={() => void addCategory()}>{busy ? t('جاري الحفظ…', 'Saving…') : t('إضافة القسم', 'Add category')}</button>
    </div>
    {error && <p className="alert" role="alert">{error}</p>}
  </div>;
}
