import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/authErrors.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { authErrorMessage } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);
const en = (_, english) => english;
const ar = arabic => arabic;

test('common authentication failures give actionable messages in both languages', () => {
  for (const code of ['invalid-email', 'invalid-credential', 'email-already-in-use', 'weak-password', 'user-disabled', 'too-many-requests', 'network-request-failed', 'popup-blocked', 'popup-closed-by-user', 'account-exists-with-different-credential']) {
    const error = { code: `auth/${code}`, message: 'Firebase: raw technical details' };
    assert.match(authErrorMessage(error, ar), /[\u0600-\u06ff]/);
    assert.match(authErrorMessage(error, en), /[a-z]/i);
    assert.doesNotMatch(authErrorMessage(error, en), /Firebase|auth\//);
    assert.notEqual(authErrorMessage(error, en), authErrorMessage({}, en));
  }
});

test('credential failures do not reveal whether an account exists', () => {
  for (const t of [ar, en]) {
    for (const code of ['user-not-found', 'wrong-password']) {
      assert.equal(authErrorMessage({ code: `auth/${code}` }, t), authErrorMessage({ code: 'auth/invalid-credential' }, t));
    }
  }
});

test('unexpected failures never expose raw error text', () => {
  for (const error of [null, undefined, 'Firebase error', new Error('sensitive details'), { code: 'auth/internal-error' }, { code: 'auth/unknown' }]) {
    assert.equal(authErrorMessage(error, en), authErrorMessage({}, en));
    assert.match(authErrorMessage(error, ar), /[\u0600-\u06ff]/);
  }
});
