import { orderingNoticeAr, orderingNoticeEn } from './orderingHours';
import './ordering-notice.css';

export default function OrderingNotice({ open }: { open: boolean }) {
  return <aside className="ordering-hours-notice" role="status" aria-live="polite"><strong>{open ? 'ساعات استقبال الطلبات / Ordering hours' : 'استقبال الطلبات مغلق حالياً / Ordering is currently closed'}</strong><p lang="ar" dir="rtl">{orderingNoticeAr}</p><p lang="en" dir="ltr">{orderingNoticeEn}</p></aside>;
}
