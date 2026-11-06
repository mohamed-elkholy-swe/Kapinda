import { useEffect, useState, type SetStateAction } from 'react';

let pending: URL | null = null;
const eventName = 'kapinda:navigate';

// Clean only after a complete navigation batch, regardless of setter order.
function cleanRoute(url: URL) {
  const params = new URLSearchParams(url.hash.slice(1));
  const page = params.get('page') || 'browse';
  const section = params.get('admin') || 'overview';
  const active: Record<string, boolean> = {
    restaurant: page === 'restaurant',
    admin: page === 'admin',
    menuRestaurant: page === 'admin' && section === 'menu',
    editRestaurant: page === 'admin' && section === 'restaurant-form',
    deliveries: page === 'delivery',
    managedRestaurant: page === 'workspace',
    restaurantTab: page === 'workspace' && !!params.get('managedRestaurant'),
  };
  for (const [key, keep] of Object.entries(active)) {
    if (!keep) params.delete(key);
  }
  if (page === 'browse') params.delete('page');
  url.hash = params.toString();
  return url;
}

function cleanCurrentRoute() {
  const target = cleanRoute(new URL(location.href));
  if (target.href !== location.href) history.replaceState(null, '', target);
}
export default function useHistoryState<T extends string>(key: string, fallback: T) {
  const read = () => new URLSearchParams(cleanRoute(new URL(location.href)).hash.slice(1)).get(key) as T || fallback;
  const [value, update] = useState<T>(read);
  useEffect(() => {
    const sync = () => { cleanCurrentRoute(); update(read()); };
    cleanCurrentRoute();
    window.addEventListener('popstate', sync);
    window.addEventListener('hashchange', sync);
    window.addEventListener(eventName, sync);
    return () => {
      window.removeEventListener('popstate', sync);
      window.removeEventListener('hashchange', sync);
      window.removeEventListener(eventName, sync);
    };
  }, [key, fallback]);
  const setValue = (next: SetStateAction<T>) => {
    const scheduled = !!pending;
    pending ||= new URL(location.href);
    const params = new URLSearchParams(pending.hash.slice(1));
    const current = params.get(key) as T || fallback;
    const result = typeof next === 'function' ? next(current) : next;
    if (result === fallback) params.delete(key); else params.set(key, result);
    pending.hash = params.toString();
    if (!scheduled) queueMicrotask(() => {
      const target = pending && cleanRoute(pending); pending = null;
      if (target && target.href !== location.href) {
        history.pushState(null, '', target);
        window.dispatchEvent(new Event(eventName));
      }
    });
  };
  return [value, setValue] as const;
}
