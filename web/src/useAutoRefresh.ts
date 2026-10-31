import { useEffect, useRef } from 'react';

// Refresh data in place, preserving the basket, open screens and form drafts.
export default function useAutoRefresh(refresh: () => Promise<unknown>, enabled = true) {
  const latest = useRef(refresh);
  latest.current = refresh;
  useEffect(() => {
    if (!enabled) return;
    let running = false;
    let disposed = false;
    const run = async () => {
      if (disposed || running || document.visibilityState === 'hidden' || !navigator.onLine) return;
      if (document.activeElement?.closest('form')) return;
      running = true;
      try { await latest.current(); } catch { /* Keep the last successful data during temporary outages. */ }
      finally { running = false; }
    };
    const timer = window.setInterval(() => void run(), 30000);
    window.addEventListener('focus', run);
    window.addEventListener('online', run);
    document.addEventListener('visibilitychange', run);
    return () => {
      disposed = true; clearInterval(timer);
      window.removeEventListener('focus', run);
      window.removeEventListener('online', run);
      document.removeEventListener('visibilitychange', run);
    };
  }, [enabled]);
}
