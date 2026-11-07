import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

function worker(clients = []) {
  const handlers = {}, shown = [], opened = [];
  const self = {
    addEventListener: (event, handler) => handlers[event] = handler,
    location: { origin: 'https://kapinda.example' },
    registration: { showNotification: async (...args) => shown.push(args) },
    clients: { matchAll: async () => clients, openWindow: async url => opened.push(url) },
  };
  vm.runInNewContext(readFileSync(new URL('../public/firebase-messaging-sw.js', import.meta.url), 'utf8'), { self, URL });
  return { handlers, shown, opened };
}
test('background push displays exactly one notification with a safe local destination', async () => {
  const w = worker();let done;
  w.handlers.push({ data: { json: () => ({ data: { title: 'Kapinda', body: 'Order update', tag: 'notice', url: 'https://untrusted.example' } }) }, waitUntil: promise => done = promise });
  await done;
  assert.equal(w.shown.length, 1);
  assert.equal(w.shown[0][1].data.url, '/#page=notifications');
  assert.equal(w.shown[0][1].tag, 'notice');
});
test('notification click opens Kapinda when no window exists', async () => {
  const w = worker();let done, closed = false;
  w.handlers.notificationclick({ notification: { close: () => closed = true }, waitUntil: promise => done = promise });
  await done;assert.equal(closed, true);assert.deepEqual(w.opened, ['https://kapinda.example/#page=notifications']);
});
test('notification click reuses an existing Kapinda window', async () => {
  let destination, focused = false, done;
  const w = worker([{ url: 'https://kapinda.example/', navigate: async url => destination = url, focus: async () => focused = true }]);
  w.handlers.notificationclick({ notification: { close() {} }, waitUntil: promise => done = promise });
  await done;assert.equal(destination, 'https://kapinda.example/#page=notifications');assert.equal(focused, true);assert.equal(w.opened.length, 0);
});
