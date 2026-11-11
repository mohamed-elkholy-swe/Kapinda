import { useEffect, useId, useRef, useState } from 'react';
import type { Translate } from './Orders';
import './menu-photos.css';
import RetryImage from './RetryImage';

export default function MenuPhotoGallery({ images, restaurantName, t }: { images: string[]; restaurantName: string; t: Translate }) {
  const [index, setIndex] = useState(0);
  const [expanded, setExpanded] = useState(false);
  const titleId = useId();
  const count = images.length;
  if (!count) return null;
  const current = images[index % count];
  const alt = `${restaurantName} · ${t('صورة المنيو', 'Menu photo')} ${index + 1}`;
  function move(delta: number) { setIndex(value => (value + delta + count) % count); }
  function photo() {
    return <RetryImage src={current} alt={alt} referrerPolicy="no-referrer"
      pending={<p className="menu-photo-loading" role="status">{t('جاري تحميل الصورة…', 'Loading photo…')}</p>}
      fallback={retry => <p className="menu-photo-error" role="status">{t('تعذر تحميل الصورة. جرّب صورة أخرى.', 'This photo could not load. Try another page.')} <button type="button" onClick={retry}>{t('إعادة المحاولة', 'Retry')}</button></p>}/>;
  }
  function arrows() {
    return count > 1 && <><button className="menu-photo-arrow menu-photo-previous" type="button" aria-label={t('صورة المنيو السابقة', 'Previous menu photo')} onClick={() => move(-1)}>‹</button><button className="menu-photo-arrow menu-photo-next" type="button" aria-label={t('صورة المنيو التالية', 'Next menu photo')} onClick={() => move(1)}>›</button></>;
  }
  function keyboard(event: React.KeyboardEvent) {
    if (event.key === 'ArrowRight') { event.preventDefault(); move(1); }
    if (event.key === 'ArrowLeft') { event.preventDefault(); move(-1); }
  }
  return <section className="menu-photo-gallery" aria-labelledby={titleId} aria-roledescription={t('عارض صور', 'carousel')} onKeyDown={keyboard}>
    <div className="menu-photo-heading"><h2 id={titleId}>{t('صور المنيو', 'Menu photos')}</h2><button type="button" className="menu-photo-enlarge" onClick={() => setExpanded(true)}>{t('عرض بالحجم الكامل', 'View full size')} ↗</button></div>
    <div className="menu-photo-stage">{photo()}{arrows()}</div>
    <div className="menu-photo-controls"><span dir="ltr" aria-live="polite" aria-atomic="true">{index + 1} / {count}</span>{count > 1 && <div className="menu-photo-dots" role="group" aria-label={t('اختر صورة المنيو', 'Choose a menu photo')}>{images.map((url, i) => <button key={url} type="button" aria-label={`${t('صورة المنيو', 'Menu photo')} ${i + 1}`} aria-pressed={i === index} onClick={() => setIndex(i)}><span/></button>)}</div>}</div>
    {expanded && <MenuPhotoViewer t={t} title={alt} onClose={() => setExpanded(false)} onKeyDown={keyboard}>
      <div className="menu-photo-viewer-controls">{count > 1 && <button type="button" onClick={() => move(-1)}>{t('السابقة', 'Previous')}</button>}<span dir="ltr" aria-live="polite">{index + 1} / {count}</span>{count > 1 && <button type="button" onClick={() => move(1)}>{t('التالية', 'Next')}</button>}<a href={current} target="_blank" rel="noopener noreferrer">{t('فتح الصورة الأصلية', 'Open original')}</a></div>
      <div className="menu-photo-full-image">{photo()}</div>
    </MenuPhotoViewer>}
  </section>;
}

function MenuPhotoViewer({ t, title, children, onClose, onKeyDown }: { t: Translate; title: string; children: React.ReactNode; onClose: () => void; onKeyDown: (event: React.KeyboardEvent) => void }) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const element = dialog.current;
    const overflow = document.body.style.overflow;
    element?.showModal(); document.body.style.overflow = 'hidden';
    return () => { element?.close(); document.body.style.overflow = overflow; };
  }, []);
  return <dialog ref={dialog} className="menu-photo-viewer" aria-label={title} onCancel={onClose} onKeyDown={event => { event.stopPropagation(); onKeyDown(event); }}>
    <div className="menu-photo-viewer-heading"><strong>{title}</strong><button type="button" aria-label={t('إغلاق صور المنيو', 'Close menu photos')} onClick={onClose} autoFocus>×</button></div>{children}
  </dialog>;
}
