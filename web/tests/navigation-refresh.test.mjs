import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';

async function hook(file, react) {
  globalThis.testReact = react;
  const source = await readFile(new URL(`../src/${file}.ts`, import.meta.url), 'utf8');
  const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
  const code = outputText.replace(/import .*? from 'react';/, 'const {useEffect,useRef,useState} = globalThis.testReact;');
  return (await import(`data:text/javascript;base64,${Buffer.from(code).toString('base64')}`)).default;
}

test('navigation batches restaurant selection and restores browser history', async () => {
  const cleanups = [], values = [];
  globalThis.window = new EventTarget();
  globalThis.location = new URL('http://localhost/');
  const entries = [location.href];
  globalThis.history = { pushState(_state, _title, url) { location = new URL(url); entries.push(location.href); } };
  const useHistoryState = await hook('useHistoryState', {
    useEffect(fn) { cleanups.push(fn()); },
    useState(initial) { const index = values.length; values.push(initial()); return [values[index], value => values[index] = value]; }
  });
  const [, select] = useHistoryState('restaurant', '');
  const [, navigate] = useHistoryState('page', 'browse');
  select('kitchen'); navigate('restaurant');
  await Promise.resolve();
  assert.equal(entries.length, 2);
  assert.deepEqual(values, ['kitchen', 'restaurant']);
  location = new URL(entries[0]); window.dispatchEvent(new Event('popstate'));
  assert.deepEqual(values, ['', 'browse']);
  location = new URL(entries[1]); window.dispatchEvent(new Event('popstate'));
  assert.deepEqual(values, ['kitchen', 'restaurant']);
  cleanups.forEach(fn => fn());
});

test('automatic refresh pauses in background and avoids overlapping requests', async () => {
  let timer, cleanup, release, calls = 0;
  globalThis.window = new EventTarget();
  window.setInterval = (fn, ms) => { assert.equal(ms, 30000); timer = fn; return 1; };
  globalThis.document = new EventTarget();
  document.visibilityState = 'visible'; document.activeElement = null;
  Object.defineProperty(globalThis, 'navigator', { configurable: true, value: { onLine: true } });
  const useAutoRefresh = await hook('useAutoRefresh', { useRef: value => ({ current: value }), useEffect: fn => { cleanup = fn(); } });
  useAutoRefresh(() => { calls++; return new Promise(resolve => release = resolve); });
  document.visibilityState = 'hidden'; timer(); assert.equal(calls, 0);
  document.visibilityState = 'visible'; document.activeElement = { closest: () => ({}) }; timer(); assert.equal(calls, 0);
  document.activeElement = null; timer(); timer(); assert.equal(calls, 1);
  release(); await Promise.resolve(); timer(); assert.equal(calls, 2);
  release(); await Promise.resolve(); cleanup(); timer(); assert.equal(calls, 2);
});

test('cleans stale links and keeps only the active screen parameters', async () => {
  const cleanups = [];
  globalThis.window = new EventTarget();
  globalThis.location = new URL('http://localhost/#admin=menu&menuRestaurant=old&restaurant=kitchen&page=restaurant');
  const entries = [location.href];
  let replacements = 0;
  globalThis.history = {
    pushState(_state, _title, url) { location = new URL(url); entries.push(location.href); },
    replaceState(_state, _title, url) { location = new URL(url); entries[entries.length - 1] = location.href; replacements++; }
  };
  const useHistoryState = await hook('useHistoryState', {
    useEffect(fn) { cleanups.push(fn()); },
    useState(initial) { return [initial(), () => {}]; }
  });
  const [, navigate] = useHistoryState('page', 'browse');
  const [, select] = useHistoryState('restaurant', '');
  const [, section] = useHistoryState('admin', 'overview');
  const [, menu] = useHistoryState('menuRestaurant', '');
  assert.equal(location.hash, '#restaurant=kitchen&page=restaurant');
  assert.equal(replacements, 1);
  assert.equal(entries.length, 1);
  // Page-first and parameter-first updates must both survive batching.
  navigate('admin'); section('menu'); menu('grill');
  await Promise.resolve();
  assert.equal(location.hash, '#page=admin&admin=menu&menuRestaurant=grill');
  section('orders');
  await Promise.resolve();
  assert.equal(location.hash, '#page=admin&admin=orders');
  select('kitchen'); navigate('restaurant');
  await Promise.resolve();
  assert.equal(location.hash, '#page=restaurant&restaurant=kitchen');
  navigate('cart');
  await Promise.resolve();
  assert.equal(location.hash, '#page=cart');
  location = new URL(entries[3]); window.dispatchEvent(new Event('popstate'));
  assert.equal(location.hash, '#page=restaurant&restaurant=kitchen');
  navigate('browse');
  await Promise.resolve();
  assert.equal(location.hash, '');
  cleanups.forEach(fn => fn());
});
