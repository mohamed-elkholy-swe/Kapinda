import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import test from 'node:test';
import ts from 'typescript';
const source=await readFile(new URL('../src/permissions.ts',import.meta.url),'utf8');
const {outputText}=ts.transpileModule(source,{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.ESNext}});
const {isAdministrator,canUseDelivery,canManageRestaurants,canManageUser,assignableRoles}=await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);
test('full and sub administrators can open all operational workspaces',()=>{
  for(const role of ['ADMIN','SUB_ADMIN']){assert.equal(isAdministrator(role),true);assert.equal(canUseDelivery(role),true);assert.equal(canManageRestaurants(role),true);}
});
test('sub administrator cannot select Admin for themselves or another person',()=>{
  assert.equal(assignableRoles('SUB_ADMIN').includes('ADMIN'),false);
  assert.equal(canManageUser('SUB_ADMIN','ADMIN'),false);
  assert.equal(canManageUser('SUB_ADMIN','SUB_ADMIN'),true);
  assert.equal(assignableRoles('ADMIN').includes('ADMIN'),true);
});
test('delivery and restaurant permissions remain separate for regular staff',()=>{
  assert.equal(canUseDelivery('RESTAURANT'),false);assert.equal(canManageRestaurants('DELIVERY'),false);
  assert.equal(canManageUser('CUSTOMER','CUSTOMER'),false);assert.deepEqual(assignableRoles('CUSTOMER'),[]);
});
