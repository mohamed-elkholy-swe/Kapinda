import type { Role } from './types';
export function isAdministrator(role?: Role) { return role === 'ADMIN' || role === 'SUB_ADMIN'; }
export function canUseDelivery(role?: Role) { return isAdministrator(role) || role === 'DELIVERY'; }
export function canManageRestaurants(role?: Role) { return isAdministrator(role) || role === 'RESTAURANT'; }
export function canManageUser(actor: Role, target: Role) { return isAdministrator(actor) && (actor === 'ADMIN' || target !== 'ADMIN'); }
export function assignableRoles(actor: Role): Role[] {
  if (!isAdministrator(actor)) return [];
  return actor === 'ADMIN' ? ['CUSTOMER', 'DELIVERY', 'RESTAURANT', 'SUB_ADMIN', 'ADMIN'] : ['CUSTOMER', 'DELIVERY', 'RESTAURANT', 'SUB_ADMIN'];
}
