/* FCM Web Push uses the browser Push API. No external scripts or private credentials. */
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', event => event.waitUntil(self.clients.claim()));
self.addEventListener('push', event => {
  let payload;
  try { payload = event.data?.json(); } catch { return; }
  const data = payload?.data || payload?.notification;
  if (!data?.title || !data?.body) return;
  event.waitUntil(self.registration.showNotification(data.title, {
    body: data.body, icon: '/icons/icon-192.png', badge: '/icons/icon-192.png',
    tag: data.tag || 'kapinda-order', data: { url: '/#page=notifications' },
  }));
});
self.addEventListener('notificationclick', event => {
  event.notification.close();
  const url = new URL('/#page=notifications', self.location.origin).href;
  event.waitUntil((async () => {
    const windows = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    const existing = windows.find(client => new URL(client.url).origin === self.location.origin);
    if (existing) { await existing.navigate(url); await existing.focus(); }
    else await self.clients.openWindow(url);
  })());
});
