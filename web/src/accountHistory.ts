import type { Order } from './types';
export type AccountHistory = { accountUid: string; orders: Order[] };

export async function requestAccountHistory(expectedUid: string, request: () => Promise<AccountHistory>, currentUid: () => string | null): Promise<Order[]> {
  if (!expectedUid || currentUid() !== expectedUid) throw new Error('Your account changed. Please reload your orders. / تم تغيير الحساب. أعد تحميل الطلبات.');
  const result = await request();
  if (currentUid() !== expectedUid || result.accountUid !== expectedUid) throw new Error('Your account changed. Please reload your orders. / تم تغيير الحساب. أعد تحميل الطلبات.');
  if (!Array.isArray(result.orders)) throw new Error('Please restart the updated backend. / يرجى إعادة تشغيل الخادم المحدث.');
  return result.orders;
}
