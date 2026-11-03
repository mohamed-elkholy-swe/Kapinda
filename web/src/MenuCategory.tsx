import { useId, useState, type ReactNode } from 'react';
import type { MenuItem, Restaurant } from './types';
import type { Translate } from './Orders';

export default function MenuCategory({ title, count, children }: { title: string; count: number; children: ReactNode }) {
  const [expanded, setExpanded] = useState(true);
  const id = useId();
  return <section className="menu-category-section">
    <h3 className="menu-category-title"><button type="button" className="menu-category-toggle" aria-expanded={expanded} aria-controls={id} onClick={() => setExpanded(value => !value)}>
      <span>{title} <small className="count-pill">{count}</small></span><span aria-hidden="true">{expanded ? '−' : '+'}</span>
    </button></h3>
    <div id={id} hidden={!expanded}>{expanded && children}</div>
  </section>;
}

export function ManagedMenuCategories({ restaurant, t, renderItem }: { restaurant: Restaurant; t: Translate; renderItem: (item: MenuItem) => ReactNode }) {
  const categories = [...new Set([...(restaurant.details?.menuCategories || []), ...restaurant.menu.map(item => item.category?.trim() || '')])];
  return categories.map(category => {
    const items = restaurant.menu.filter(item => (item.category?.trim() || '') === category);
    return <MenuCategory key={JSON.stringify([restaurant.id, category, items.length])} title={category || t('أصناف أخرى', 'Other items')} count={items.length}>
      {items.length ? items.map(renderItem) : <p className="muted">{t('لا توجد أصناف في هذا القسم بعد.', 'No dishes in this category yet.')}</p>}
    </MenuCategory>;
  });
}
