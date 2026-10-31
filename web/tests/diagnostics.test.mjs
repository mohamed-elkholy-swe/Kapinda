import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/diagnostics.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { safeDiagnostic, errorCode } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('retains useful failure metadata and discards credentials and personal data', () => {
  const entry = safeDiagnostic({ event: 'api.failed', token: 'secret', details: {
    method: 'GET', path: '/account/me', status: 503, elapsedMs: 12.8,
    authorization: 'Bearer secret', email: 'private@example.test', body: { password: 'secret' },
  } });
  assert.deepEqual(entry, { event: 'api.failed', details: { method: 'GET', path: '/account/me', status: 503, elapsedMs: 13 } });
  assert.ok(!JSON.stringify(entry).includes('secret'));
});

test('rejects unknown events, query strings, and malformed metadata', () => {
  assert.equal(safeDiagnostic({ event: 'private@example.test' }), null);
  assert.equal(safeDiagnostic(null), null);
  assert.deepEqual(safeDiagnostic({ event: 'api.failed', details: {
    path: '/account/me?token=secret', method: 'GET\nsecret', code: 'auth/internal-error\nsecret', status: 999, elapsedMs: NaN,
  } }), { event: 'api.failed', details: {} });
});

test('extracts only the Firebase error code, never its custom data or message', () => {
  assert.equal(errorCode({ code: 'auth/internal-error', message: 'secret', customData: { token: 'secret' } }), 'auth/internal-error');
  assert.equal(errorCode({ code: 'private@example.test' }), undefined);
  assert.equal(errorCode(new Error('secret')), undefined);
});
