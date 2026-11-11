import { useState } from 'react';
import { api } from './api';
import useRestaurantCategories from './useRestaurantCategories';
import { categoryKey, restaurantCategoryLabel, type RestaurantCategory } from './restaurantCategories';
import type { Translate } from './Orders';

export default function RestaurantCategoryField({ t, initialCategory = '', workspace = false }: { t: Translate; initialCategory?: string; workspace?: boolean }) {
  const { categories, setCategories, loading, error, reload } = useRestaurantCategories();
  const [selected, setSelected] = useState(categoryKey(initialCategory));
  const [adding, setAdding] = useState(false);
  const [nameAr, setNameAr] = useState('');
  const [nameEn, setNameEn] = useState('');
  const [busy, setBusy] = useState(false);
  const [saveError, setSaveError] = useState('');
  async function add() {
    if (!nameAr.trim() || busy) return;
    setBusy(true); setSaveError('');
    try {
      const result = await api<RestaurantCategory>(workspace ? '/restaurant-workspace/restaurant-categories' : '/admin/restaurant-categories', {method:'POST', body:JSON.stringify({nameAr:nameAr.trim(),nameEn:nameEn.trim() || null})}, true);
      setCategories(current => [...current.filter(c => c.id !== result.id), result]);
      setSelected(result.id); setNameAr(''); setNameEn(''); setAdding(false);
    } catch(e) { setSaveError(e instanceof Error ? e.message : t('تعذر حفظ التصنيف','Could not save category')); }
    finally { setBusy(false); }
  }
  return <div className="restaurant-category-field">
    <label>{t('تصنيف المطعم', 'Restaurant category')}<select name="category" required value={categories.some(c => c.id === selected) ? selected : ''} onChange={event => setSelected(event.target.value)}>
      <option value="">{loading ? t('جاري تحميل التصنيفات…','Loading categories…') : t('اختر التصنيف','Choose a category')}</option>
      {categories.map(c => <option key={c.id} value={c.id}>{restaurantCategoryLabel(c.id, categories, t)}</option>)}
    </select></label>
    {error && <p role="alert">{t('تعذر تحميل التصنيفات. أعد المحاولة قبل حفظ المطعم.','Could not load categories. Retry before saving the restaurant.')} <button type="button" onClick={() => void reload()}>{t('إعادة المحاولة','Retry')}</button></p>}
    <button type="button" disabled={loading || error || busy} aria-expanded={adding} onClick={() => setAdding(value => !value)}>{adding ? t('إلغاء إضافة التصنيف','Cancel new category') : t('إضافة تصنيف جديد','Add a new category')}</button>
    {adding && <fieldset disabled={busy} className="new-restaurant-category"><legend>{t('تصنيف جديد','New category')}</legend><label>{t('الاسم بالعربية (مطلوب)','Arabic name (required)')}<input value={nameAr} maxLength={100} dir="rtl" onChange={event => setNameAr(event.target.value)}/></label><label>{t('الاسم بالإنجليزية (اختياري)','English name (optional)')}<input value={nameEn} maxLength={100} dir="ltr" onChange={event => setNameEn(event.target.value)}/></label><button type="button" disabled={busy || !nameAr.trim()} onClick={() => void add()}>{busy ? t('جاري الحفظ…','Saving…') : t('حفظ التصنيف واختياره','Save and select category')}</button></fieldset>}
    {saveError && <p className="alert" role="alert">{saveError}</p>}
  </div>;
}
