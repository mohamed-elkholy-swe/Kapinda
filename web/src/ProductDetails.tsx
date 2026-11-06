import { useEffect, useRef } from 'react';
import RestaurantImage from './RestaurantImage';
import type { MenuItem } from './types';
import type { Translate } from './Orders';

export default function ProductDetails({ item, fallback, price, t, onClose, onAdd }: {
  item: MenuItem;
  fallback: string;
  price: string;
  t: Translate;
  onClose: () => void;
  onAdd: () => void;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const element = dialog.current;
    const overflow = document.body.style.overflow;
    element?.showModal();
    document.body.style.overflow = 'hidden';
    return () => { element?.close(); document.body.style.overflow = overflow; };
  }, []);
  return <dialog ref={dialog} className="product-details" aria-labelledby="product-details-title" onCancel={onClose} onClick={event => {
    if (event.target !== event.currentTarget) return;
    const bounds = event.currentTarget.getBoundingClientRect();
    if (event.clientX < bounds.left || event.clientX > bounds.right || event.clientY < bounds.top || event.clientY > bounds.bottom) onClose();
  }}>
    <button type="button" className="product-details-close" aria-label={t('إغلاق', 'Close')} onClick={onClose} autoFocus>×</button>
    <div className="product-details-photo"><RestaurantImage src={item.photoUrl} name={item.name} fallback={fallback}/></div>
    <div className="product-details-copy">
      {item.category && <span className="eyebrow">{item.category}</span>}
      <h2 id="product-details-title">{item.name}</h2>
      <p>{item.description}</p>
      <div className="product-details-footer"><strong>{price}</strong><button type="button" className="primary" onClick={onAdd}>{t('أضف للسلة', 'Add to basket')} +</button></div>
    </div>
  </dialog>;
}
