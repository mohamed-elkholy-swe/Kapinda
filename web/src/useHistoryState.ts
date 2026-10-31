import { useEffect, useState, type SetStateAction } from 'react';

let pending: URL | null = null;
const eventName = 'kapinda:navigate';
export default function useHistoryState<T extends string>(key: string, fallback: T) {
  const read = () => new URLSearchParams(location.hash.slice(1)).get(key) as T || fallback;
  const [value, update] = useState<T>(read);
  useEffect(() => {
    const sync = () => update(read());
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
      const target = pending; pending = null;
      if (target && target.href !== location.href) {
        history.pushState(null, '', target);
        window.dispatchEvent(new Event(eventName));
      }
    });
  };
  return [value, setValue] as const;
}
