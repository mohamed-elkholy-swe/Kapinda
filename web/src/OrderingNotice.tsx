import { orderingNoticeAr, orderingNoticeEn } from './orderingHours';
import './ordering-notice.css';
import type { Translate } from './OrderStatus';

export default function OrderingNotice({ open, t }: { open: boolean; t: Translate }) {
  return <aside className="ordering-hours-notice" role="status" aria-live="polite"><strong>{open ? t('ساعات استقبال الطلبات', 'Ordering hours') : t('استقبال الطلبات مغلق حالياً', 'Ordering is currently closed')}</strong><p>{t(orderingNoticeAr, orderingNoticeEn)}</p></aside>;
}
