import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

const source = readFileSync(new URL('../public/firebase-messaging-sw.js', import.meta.url), 'utf8');
function worker(tabs = []) {
  const handlers = {}, shown = [], opened = [];
  vm.runInNewContext(source, { URL, URLSearchParams, self: {
    location: { origin: 'https://kapinda.test' },
    addEventListener: (name, handler) => { handlers[name] = handler; },
    registration: { showNotification: async (...args) => { shown.push(args); } },
    clients: { matchAll: async () => tabs, openWindow: async url => { opened.push(url); } }
  } });
  return { handlers, shown, opened };
}
test('background push displays one alert and preserves the operational destination', async () => {
  const w = worker(); let done;
  w.handlers.push({ data: { json: () => ({ data: { title: 'Kapinda', body: 'Update', tag: 'event', page: 'delivery', deliveries: 'available' } }) }, waitUntil: promise => { done = promise; } });
  await done;
  assert.equal(w.shown.length, 1);
  assert.equal(w.shown[0][1].tag, 'event');
  assert.match(w.shown[0][1].data.url, /page=delivery/);
  assert.match(w.shown[0][1].data.url, /deliveries=available/);
});
test('invalid push is ignored and unrecognized destinations fall back to orders', async () => {
  const w = worker();
  w.handlers.push({ data: { json: () => { throw new Error(); } } });
  assert.equal(w.shown.length, 0);
  let done;
  w.handlers.push({ data: { json: () => ({ data: { page: 'https://other.test' } }) }, waitUntil: promise => { done = promise; } });
  await done;
  assert.match(w.shown[0][1].data.url, /^\/#page=orders&/);
});
test('notification click focuses an existing tab instead of opening another', async () => {
  const visited = []; const w = worker([{ url: 'https://kapinda.test/', navigate: async url => { visited.push(url); }, focus: async () => { visited.push('focused'); } }]);
  let done, closed = false;
  w.handlers.notificationclick({ notification: { close: () => { closed = true; }, data: { url: '/#page=restaurantOrders' } }, waitUntil: promise => { done = promise; } });
  await done;
  assert.equal(closed, true); assert.deepEqual(visited, ['/#page=restaurantOrders', 'focused']); assert.equal(w.opened.length, 0);
});
test('notification click opens the app when no tab is open', async () => {
  const w = worker(); let done;
  w.handlers.notificationclick({ notification: { close() {}, data: { url: '/#page=orders' } }, waitUntil: promise => { done = promise; } });
  await done; assert.deepEqual(w.opened, ['/#page=orders']);
});
