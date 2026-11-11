import type { Translate } from './Orders';
export type RestaurantCategory = { id: string; nameAr: string; nameEn?: string | null };
export function categoryKey(value: string) {
  const key = value.normalize('NFKC').replace(/[\u064B-\u065F\u0670\u0640]/g, '').replace(/[أإآٱ]/g, 'ا').trim().replace(/\s+/g, ' ').toLowerCase();
  const aliases: Record<string, string> = {pizza:'pizza',pizzas:'pizza','بيتزا':'pizza',burger:'burger',burgers:'burger','برجر':'burger','برغر':'burger',dessert:'dessert',desserts:'dessert',sweets:'dessert','حلويات':'dessert',grill:'grill',grills:'grill',grilled:'grill','مشويات':'grill',chicken:'chicken','دجاج':'chicken','فراخ':'chicken',egyptian:'egyptian','اكل مصري':'egyptian','مصري':'egyptian'};
  return aliases[key] || key;
}
export function restaurantCategoryLabel(value: string, categories: RestaurantCategory[], t: Translate) {
  const key = categoryKey(value);
  const saved = categories.find(c => c.id === key || categoryKey(c.nameAr) === key || (c.nameEn && categoryKey(c.nameEn) === key));
  if (saved) return t(saved.nameAr, saved.nameEn || saved.nameAr);
  return ({pizza:t('بيتزا','Pizza'),burger:t('برجر','Burgers'),dessert:t('حلويات','Desserts'),grill:t('مشويات','Grills'),chicken:t('دجاج','Chicken'),egyptian:t('أكل مصري','Egyptian')} as Record<string,string>)[key] || value;
}
