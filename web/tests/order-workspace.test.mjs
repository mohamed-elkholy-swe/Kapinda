import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { createRequire } from 'node:module';
import test from 'node:test';
import ts from 'typescript';
import { build } from 'esbuild';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';

const source = await readFile(new URL('../src/orderDates.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { cairoOrderDay } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('order day uses Cairo midnight in winter and summer rather than the device timezone', () => {
  assert.equal(cairoOrderDay('2026-11-12T21:59:59Z'), '2026-11-12');
  assert.equal(cairoOrderDay('2026-11-12T22:00:00Z'), '2026-11-13');
  assert.equal(cairoOrderDay('2026-07-12T20:59:59Z'), '2026-07-12');
  assert.equal(cairoOrderDay('2026-07-12T21:00:00Z'), '2026-07-13');
});

const bundle = await build({ entryPoints: [new URL('../src/OrderWorkspace.tsx', import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1')], bundle: true, platform: 'node', format: 'cjs', jsx: 'automatic', write: false, external: ['react'], loader: { '.css': 'empty' } });
const mod = { exports: {} };
new Function('require', 'module', 'exports', bundle.outputFiles[0].text)(createRequire(import.meta.url), mod, mod.exports);
const { default: OrderWorkspace, CourierContact, RestaurantProgress } = mod.exports;
const t = (_ar, en) => en;
const order = { id: '42', status: 'READY', customerName: 'Buyer', customerPhone: '', customerAddress: '', area: 'City', total: 68, subtotal: 50, deliveryFee: 18, createdAt: '2026-11-12T15:00:00Z', items: [{ itemName: 'Meal', restaurantName: 'Kitchen', quantity: 1, lineTotal: 50 }] };

test('courier contact distinguishes unassigned from assigned profiles with missing details', () => {
  const render = o => renderToStaticMarkup(React.createElement(CourierContact, { order: o, t }));
  assert.match(render(order), /Not assigned yet/);
  const assigned = render({ ...order, deliveryAssigned: true });
  assert.match(assigned, /Name unavailable/);
  assert.match(assigned, /Phone unavailable/);
  assert.doesNotMatch(assigned, /Not assigned yet/);
  const contact = render({ ...order, deliveryAssigned: true, deliveryPersonName: '<Mohamed>', deliveryPersonPhone: '+20 100 000 0000' });
  assert.match(contact, /&lt;Mohamed&gt;/);
  assert.match(contact, /href="tel:\+201000000000"/);
});

test('shared workspace renders supplied actions and courier contacts in desktop and mobile layouts', () => {
  const html = renderToStaticMarkup(React.createElement(OrderWorkspace, { orders: [{ ...order, deliveryAssigned: true, deliveryPersonName: 'Mohamed Hassan', deliveryPersonPhone: '01012345678' }], filters: false, t, money: n => `${n} EGP`, action: () => React.createElement('button', {}, 'Claim order') }));
  assert.match(html, /workspace-desktop/);
  assert.match(html, /workspace-mobile/);
  assert.match(html, /Mohamed Hassan/);
  assert.match(html, /tel:01012345678/);
  assert.match(html, /Claim order/);
});

test('mixed orders show each kitchens own progress once even when it has several items', () => {
  const html = renderToStaticMarkup(React.createElement(RestaurantProgress, { t, order: { ...order, status: 'PLACED', items: [
    { ...order.items[0], restaurantId: 'a', restaurantName: 'Kitchen A', restaurantStatus: 'READY' },
    { ...order.items[0], restaurantId: 'a', restaurantName: 'Kitchen A', restaurantStatus: 'READY' },
    { ...order.items[0], restaurantId: 'b', restaurantName: 'Kitchen B', restaurantStatus: 'ACCEPTED' },
  ] } }));
  assert.equal((html.match(/Kitchen A/g) || []).length, 1);
  assert.equal((html.match(/Kitchen B/g) || []).length, 1);
  assert.match(html, /Ready/);
  assert.match(html, /Accepted/);
});
