export const launchAt = Date.parse('2026-11-12T16:00:00+02:00');
export const ordersHaveOpened = (now = Date.now()) => now >= launchAt;
export const launchOrderMessage = (t: (ar: string, en: string) => string) => t(
  'الطلبات هتبدأ يوم الخميس ١٢ نوفمبر الساعة ٤ مساءً بتوقيت مصر. تقدر تتصفح المطاعم والمنيو لحد ما نبدأ!',
  'Orders open Thursday, November 12 at 4:00 PM Egypt time. You can browse restaurants and menus until then!'
);
