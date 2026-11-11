import './menu-photos.css';
import { useState } from 'react';
import type { Translate } from './Orders';

export default function MenuPhotosField({ t, initialImages }: { t: Translate; initialImages?: string[] | null }) {
  const [images, setImages] = useState(initialImages || []);
  function move(index: number, delta: number) {
    setImages(current => { const next = [...current]; [next[index], next[index + delta]] = [next[index + delta], next[index]]; return next; });
  }
  return <fieldset className="menu-photos-field"><legend>{t('صور المنيو', 'Menu photos')}</legend>
    <p className="muted">{t('أضف حتى ٣٠ رابط صور HTTPS بالترتيب المطلوب. ستظهر فوق أصناف المنيو.', 'Add up to 30 HTTPS photo links in display order. They appear above the menu items.')}</p>
    {images.map((url, index) => <div className="menu-photo-field-row" key={index}>
      <label>{t('صورة المنيو', 'Menu photo')} {index + 1}<input name="menuImages" type="url" pattern="https://.*" maxLength={1500} required dir="ltr" placeholder="https://…" value={url} onChange={event => setImages(current => current.map((value, i) => i === index ? event.target.value : value))}/></label>
      <div className="button-row"><button type="button" disabled={index === 0} aria-label={`${t('تحريك لأعلى', 'Move up')} ${index + 1}`} onClick={() => move(index, -1)}>↑</button><button type="button" disabled={index === images.length - 1} aria-label={`${t('تحريك لأسفل', 'Move down')} ${index + 1}`} onClick={() => move(index, 1)}>↓</button><button type="button" className="danger" aria-label={`${t('إزالة صورة المنيو', 'Remove menu photo')} ${index + 1}`} onClick={() => setImages(current => current.filter((_, i) => i !== index))}>{t('إزالة', 'Remove')}</button></div>
    </div>)}
    <button type="button" disabled={images.length >= 30} onClick={() => setImages(current => [...current, ''])}>+ {t('إضافة صورة منيو', 'Add menu photo')}</button>
    <p className="muted">{t('احفظ بيانات المطعم لتطبيق التغييرات.', 'Save restaurant details to apply changes.')}</p>
  </fieldset>;
}
