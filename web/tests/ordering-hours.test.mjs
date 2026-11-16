import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/orderingHours.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { orderingIsOpen, orderingNoticeAr, orderingNoticeEn } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('daily ordering closes exactly at 4am and reopens at 4pm Cairo time', () => {
  for (const [instant, expected] of [
    ['2026-11-17T01:59:59Z', true], ['2026-11-17T02:00:00Z', false],
    ['2026-11-17T13:59:59Z', false], ['2026-11-17T14:00:00Z', true],
    ['2026-11-17T22:00:00Z', true], ['2026-07-17T00:59:59Z', true],
    ['2026-07-17T01:00:00Z', false], ['2026-07-17T12:59:59Z', false],
    ['2026-07-17T13:00:00Z', true],
  ]) assert.equal(orderingIsOpen(new Date(instant)), expected, instant);
});

test('both notices explain the daily closure hours and Cairo timezone', () => {
  assert.match(orderingNoticeAr, /٤:٠٠ صباحاً إلى ٤:٠٠ مساءً بتوقيت القاهرة/);
  assert.match(orderingNoticeEn, /daily from 4:00 AM to 4:00 PM Cairo time/);
});
