import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';
const source = await readFile(new URL('../src/menuCategories.ts', import.meta.url), 'utf8');
const {outputText} = ts.transpileModule(source, {compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.ESNext}});
const {menuCategoryDefinitions,menuCategoryLabel,resolveMenuCategory} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);
const restaurant = {details:{menuCategories:['Drinks','حلويات'],menuCategoryDefinitions:[{id:'Drinks',nameAr:'مشروبات',nameEn:'Drinks'},{id:'حلويات',nameAr:'حلويات',nameEn:null}]},menu:[{category:'Drinks'}]};

test('menu categories preserve stored keys and use optional English translations', () => {
  assert.equal(menuCategoryDefinitions(restaurant).length,2);
  assert.equal(menuCategoryLabel('Drinks',restaurant,(ar)=>ar),'مشروبات');
  assert.equal(menuCategoryLabel('Drinks',restaurant,(_ar,en)=>en),'Drinks');
  assert.equal(menuCategoryLabel('حلويات',restaurant,(_ar,en)=>en),'حلويات');
});
test('bulk category names resolve saved Arabic and English labels and reject missing categories', () => {
  assert.equal(resolveMenuCategory(' DRINKS ',restaurant),'Drinks');
  assert.equal(resolveMenuCategory('مَشروبات',restaurant),'Drinks');
  assert.equal(resolveMenuCategory('',restaurant),'');
  assert.equal(resolveMenuCategory('Unsaved',restaurant),undefined);
  assert.equal(resolveMenuCategory('Drinks',{menu:[],details:null}),undefined);
});
