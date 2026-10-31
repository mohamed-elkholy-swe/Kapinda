import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';
const source = await readFile(new URL('../src/accountHistory.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { requestAccountHistory } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);
test('new account receives zero personal orders', async () => {
  assert.deepEqual(await requestAccountHistory('new', async () => ({accountUid:'new',orders:[]}), () => 'new'), []);
});
test('a response for another account is rejected rather than displayed', async () => {
  await assert.rejects(requestAccountHistory('new', async () => ({accountUid:'old',orders:[{id:'other-order'}]}), () => 'new'));
});
test('switching accounts while history is loading rejects the old response', async () => {
  let uid = 'old';
  await assert.rejects(requestAccountHistory('old', async () => {uid='new';return {accountUid:'old',orders:[{id:'old-order'}]};}, () => uid));
});
test('signed out accounts do not request history', async () => {
  let called=false;
  await assert.rejects(requestAccountHistory('old', async () => {called=true;return {accountUid:'old',orders:[]};}, () => null));
  assert.equal(called,false);
});
