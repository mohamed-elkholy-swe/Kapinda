import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';
const source = await readFile(new URL('../src/pushPreference.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const moduleUrl = `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`;
const { shouldPromptForPush, pushPreference, rememberPushPreference, pushChangeEvent } = await import(moduleUrl);

test('new users see the offer until they choose, including previously granted browsers without a registration', () => {
  assert.equal(shouldPromptForPush(true, 'default', false, null), true);
  assert.equal(shouldPromptForPush(true, 'granted', false, null), true);
  assert.equal(shouldPromptForPush(true, 'granted', true, null), false);
  assert.equal(shouldPromptForPush(false, 'default', false, null), false);
  assert.equal(shouldPromptForPush(true, 'denied', false, null), false);
  assert.equal(shouldPromptForPush(true, 'default', false, 'dismissed'), false);
  assert.equal(shouldPromptForPush(true, 'granted', false, 'enabled'), false);
});
test('declining persists across reloads, stays scoped to the account, and can be changed from settings', async () => {
  const values = new Map();
  globalThis.localStorage = { getItem: key => values.get(key), setItem: (key, value) => values.set(key, value) };
  globalThis.window = new EventTarget();
  let changes = 0;
  window.addEventListener(pushChangeEvent, () => changes++);
  rememberPushPreference('buyer', 'dismissed');
  const reloaded = await import(`${moduleUrl}#reload`);
  assert.equal(reloaded.pushPreference('buyer'), 'dismissed');
  assert.equal(pushPreference('other-account'), null);
  rememberPushPreference('buyer', 'enabled');
  assert.equal(pushPreference('buyer'), 'enabled');
  assert.equal(changes, 2);
});
test('blocked browser storage still remembers the choice for the current visit', () => {
  globalThis.localStorage = { getItem() { throw new Error('blocked'); }, setItem() { throw new Error('blocked'); } };
  globalThis.window = new EventTarget();
  rememberPushPreference('private-browser', 'dismissed');
  assert.equal(pushPreference('private-browser'), 'dismissed');
});
