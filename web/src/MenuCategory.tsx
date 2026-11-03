import { useId, useState, type ReactNode } from 'react';
import type { MenuItem, Restaurant } from './types';
import type { Translate } from './Orders';
import { api } from './api';

export default function MenuCategory({ title, count, children, action }: { title: string; count: number; children: ReactNode; action?: ReactNode }) {
  const [expanded, setExpanded] = useState(true);
  const id = useId();
  return <section className="menu-category-section">
    <div className="menu-category-heading"><h3 className="menu-category-title"><button type="button" className="menu-category-toggle" aria-expanded={expanded} aria-controls={id} onClick={() => setExpanded(value => !value)}>
      <span>{title} <small className="count-pill">{count}</small></span><span aria-hidden="true">{expanded ? '−' : '+'}</span>
    </button></h3>{action}</div>
    <div id={id} hidden={!expanded}>{expanded && children}</div>
  </section>;
}

export function ManagedMenuCategories({ restaurant, t, renderItem, endpoint, onDeleted }: {
  restaurant: Restaurant; t: Translate; renderItem: (item: MenuItem, index: number) => ReactNode;
  endpoint: string; onDeleted: () => Promise<void>;
}) {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const categories = [...new Set([...(restaurant.details?.menuCategories || []).map(name => name.trim()), ...restaurant.menu.map(item => item.category?.trim() || '')])];

  async function remove(category: string) {
    if (!window.confirm(t(`حذف قسم «${category}»؟ الأصناف ستنتقل إلى أصناف أخرى.`, `Delete “${category}”? Its dishes will move to Other items.`))) return;
    setDeleting(true);
    setError('');
    try {
      await api(`${endpoint}/menu-categories?name=${encodeURIComponent(category)}`, { method: 'DELETE' }, true);
      await onDeleted();
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setDeleting(false);
    }
  }

  return <>{error && <p role="alert" className="alert">{error}</p>}{categories.map(category => {
    const items = restaurant.menu.filter(item => (item.category?.trim() || '') === category);
    return <MenuCategory key={JSON.stringify([restaurant.id, category, items.length])} title={category || t('أصناف أخرى', 'Other items')} count={items.length}
      action={category && <button type="button" className="danger" disabled={deleting} onClick={() => void remove(category)}>{t('حذف القسم', 'Delete category')}</button>}>
      {items.length ? <div className="dish-grid managed-dish-grid">{items.map(renderItem)}</div> : <p className="muted">{t('لا توجد أصناف في هذا القسم بعد.', 'No dishes in this category yet.')}</p>}
    </MenuCategory>;
  })}</>;
}
