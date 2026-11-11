import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';
const source = await readFile(new URL('../src/restaurantCategories.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(source, {compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.ESNext}});
const { categoryKey, restaurantCategoryLabel } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('equivalent Arabic and English restaurant categories create one filter', () => {
  const values = ['pizza','بيتزا',' PIZZA ','بِيتـزا','burger','برجر'];
  assert.deepEqual([...new Set(values.map(categoryKey))], ['pizza','burger']);
  const pizza = values.filter(value => categoryKey(value) === 'pizza');
  assert.equal(pizza.length,4);
});
test('saved bilingual labels use Arabic when the optional English name is absent', () => {
  const categories = [{id:'custom',nameAr:'مأكولات بحرية',nameEn:null},{id:'pizza',nameAr:'بيتزا',nameEn:'Pizza'}];
  const ar = (arabic) => arabic;
  const en = (_arabic,english) => english;
  assert.equal(restaurantCategoryLabel('custom',categories,en),'مأكولات بحرية');
  assert.equal(restaurantCategoryLabel('بيتزا',categories,en),'Pizza');
  assert.equal(restaurantCategoryLabel('pizza',categories,ar),'بيتزا');
});
