// No cached pages or API responses: this worker handles notifications only.
self.addEventListener('push', event => {
  let payload;
  try { payload = event.data?.json(); } catch { return; }
  const data = payload?.data;
  if (!data) return;
  event.waitUntil(self.registration.showNotification(data.title || 'Kapinda', {
    body: data.body || 'You have an order update',
    icon: '/kapinda.svg', tag: data.tag || 'kapinda-update',
    data: { url: '/#' + new URLSearchParams({page: ['admin','restaurantOrders','delivery','orders'].includes(data.page) ? data.page : 'orders', admin: 'orders', deliveries: data.deliveries === 'available' ? 'available' : 'mine'}) }
  }));
});
self.addEventListener('notificationclick', event => {
  event.notification.close();
  event.waitUntil((async () => {
    const tabs = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    const tab = tabs.find(client => new URL(client.url).origin === self.location.origin);
    const url = event.notification.data?.url || '/#page=orders';
    if (tab) { await tab.navigate(url); await tab.focus(); }
    else await self.clients.openWindow(url);
  })());
});
