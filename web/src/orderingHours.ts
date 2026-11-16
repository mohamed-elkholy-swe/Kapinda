export const orderingNoticeAr = 'التطبيق لا يقبل الطلبات يومياً من ٤:٠٠ صباحاً إلى ٤:٠٠ مساءً بتوقيت القاهرة.';
export const orderingNoticeEn = 'The application does not accept orders daily from 4:00 AM to 4:00 PM Cairo time.';

export function orderingIsOpen(now = new Date()) {
  const hour = Number(new Intl.DateTimeFormat('en-GB', { timeZone: 'Africa/Cairo', hour: '2-digit', hourCycle: 'h23' }).format(now));
  return hour < 4 || hour >= 16;
}
