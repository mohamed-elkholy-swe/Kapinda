import { useEffect, useRef, type ReactNode } from 'react';
import type { Translate } from './Orders';

export default function ProductFormDialog({ children, t, busy, onClose }: {
  children: ReactNode;
  t: Translate;
  busy: boolean;
  onClose: () => void;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const element = dialog.current;
    const overflow = document.body.style.overflow;
    element?.showModal();
    document.body.style.overflow = 'hidden';
    return () => { element?.close(); document.body.style.overflow = overflow; };
  }, []);
  return <dialog ref={dialog} className="product-form-dialog" aria-label={t('بيانات الصنف', 'Product details')} onCancel={event => { event.preventDefault(); if (!busy) onClose(); }}>
    <button type="button" className="product-form-close" aria-label={t('إغلاق', 'Close')} disabled={busy} onClick={onClose}>×</button>
    {children}
  </dialog>;
}
