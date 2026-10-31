import { useState } from 'react';
import type { Restaurant } from './types';
import type { Translate } from './Orders';
export default function MenuGallery({ restaurant, t }: { restaurant: Restaurant; t: Translate }) {
  const pages = restaurant.details?.menuImages || [];
  const [index, setIndex] = useState(0);
  const [failed, setFailed] = useState(false);
  if (!pages.length) return null;
  return <section className="panel menu-gallery" aria-label={t('صور المنيو','Menu photographs')}><h2>{t('صور المنيو المنشور','Published menu photos')}</h2><p className="menu-import-note">{t('الأسعار غير مؤكدة وقد تكون قديمة. يرجى التأكيد مع المطعم قبل الطلب.','Prices are unverified and may be outdated. Confirm with the restaurant before ordering.')}</p><p>{restaurant.details?.menuNote}</p><div className="menu-pages" role="group" aria-label={t('صفحات المنيو','Menu pages')}>{pages.map((_,i)=><button key={i} type="button" aria-pressed={index===i} onClick={()=>{setIndex(i);setFailed(false);}}>{i+1}</button>)}</div><p>{index+1} / {pages.length} · <a href={pages[index]} target="_blank" rel="noreferrer">{t('افتح الصورة بالحجم الكامل','Open full-size image')}</a></p>{failed ? <p role="status">{t('تعذر تحميل الصورة. افتحها بالحجم الكامل أو من المصدر.','Image could not load. Open it full size or visit the source.')}</p> : <a href={pages[index]} target="_blank" rel="noreferrer"><img key={pages[index]} className="menu-scan" src={pages[index]} alt={restaurant.name+' — '+t('صفحة المنيو','Menu page')+' '+(index+1)} referrerPolicy="no-referrer" onError={()=>setFailed(true)}/></a>}<p><a href={restaurant.details?.menuSourceUrl || restaurant.details?.mapsUrl} target="_blank" rel="noreferrer">{t('مصدر المنيو','Menu source')}</a> · <a href={restaurant.details?.photoSourceUrl || restaurant.details?.mapsUrl} target="_blank" rel="noreferrer">{t('مصدر صورة المطعم: Google Maps','Restaurant photo source: Google Maps')}</a></p></section>;
}
