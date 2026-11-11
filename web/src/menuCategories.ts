import type { Restaurant } from './types';
import type { Translate } from './Orders';
export type MenuCategoryDefinition = { id: string; nameAr: string; nameEn?: string | null };
export function menuCategoryKey(value: string) {
  return value.normalize('NFKC').replace(/[\u064B-\u065F\u0670\u0640]/g, '').replace(/[أإآٱ]/g, 'ا').trim().replace(/\s+/g, ' ').toLowerCase();
}
export function menuCategoryDefinitions(restaurant: Restaurant): MenuCategoryDefinition[] {
  const saved = new Map((restaurant.details?.menuCategoryDefinitions || []).map(category => [category.id, category]));
  for (const value of [...(restaurant.details?.menuCategories || []), ...restaurant.menu.map(item => item.category || '')]) {
    const id = value.trim();
    if (id && !saved.has(id)) saved.set(id, {id, nameAr:id, nameEn:null});
  }
  return [...saved.values()];
}
export function menuCategoryLabel(id: string, restaurant: Restaurant, t: Translate) {
  const category = menuCategoryDefinitions(restaurant).find(c => c.id === id);
  return category ? t(category.nameAr, category.nameEn || category.nameAr) : id;
}
export function resolveMenuCategory(value: string, restaurant: Restaurant) {
  const key = menuCategoryKey(value);
  if (!key) return '';
  return menuCategoryDefinitions(restaurant).find(c => c.id === value.trim() || menuCategoryKey(c.nameAr) === key || (c.nameEn && menuCategoryKey(c.nameEn) === key))?.id;
}
