import { statuses, type Order } from './types';
export type Translate = (ar: string, en: string) => string;
export function statusLabel(status: Order['status'], t: Translate) {
  const ar = ['تم الطلب', 'تم القبول', 'قيد التحضير', 'جاهز للاستلام', 'في الطريق', 'تم التوصيل', 'ملغي'];
  const en = ['Placed', 'Accepted', 'Preparing', 'Ready', 'On the way', 'Delivered', 'Cancelled'];
  return t(ar[statuses.indexOf(status)], en[statuses.indexOf(status)]);
}
export const activeOrder = (order: Order) => !['DELIVERED', 'CANCELLED'].includes(order.status);
export function StatusBadge({ status, t }: { status: Order['status']; t: Translate }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}><i/>{statusLabel(status, t)}</span>;
}
