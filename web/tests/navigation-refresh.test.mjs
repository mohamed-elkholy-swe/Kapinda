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
