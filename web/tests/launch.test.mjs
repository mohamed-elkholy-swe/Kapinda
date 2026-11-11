import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/launch.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { launchAt, ordersHaveOpened, launchOrderMessage } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('ordering opens at exactly Thursday 4pm Cairo time', () => {
  assert.equal(launchAt, Date.parse('2026-11-12T14:00:00Z'));
  assert.equal(ordersHaveOpened(launchAt - 1), false);
  assert.equal(ordersHaveOpened(launchAt), true);
  assert.equal(ordersHaveOpened(launchAt + 1), true);
});

test('launch notice includes the opening time in both languages', () => {
  assert.match(launchOrderMessage((ar, en) => en), /Thursday, November 12 at 4:00 PM Egypt time/);
  assert.match(launchOrderMessage(ar => ar), /الخميس ١٢ نوفمبر الساعة ٤ مساءً/);
});
