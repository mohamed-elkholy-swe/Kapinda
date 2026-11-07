import { useEffect, useState } from 'react';
import { auth } from './api';
import { disablePush, enablePush, pushEnabled, pushSupported } from './push';
import type { Translate } from './Orders';

export default function PushSettings({ t }: { t: Translate }) {
  const uid = auth?.currentUser?.uid || '';
  const [supported, setSupported] = useState<boolean | null>(null);
  const [enabled, setEnabled] = useState(false), [busy, setBusy] = useState(false), [error, setError] = useState('');
  useEffect(() => {
    let alive = true;
    const refresh = async () => {
      try {
        const available = await pushSupported();
        if (!alive) return;
        setSupported(available);
        if (available && pushEnabled(uid)) {
          await enablePush(uid, false);
          if (alive) { setEnabled(true); setError(''); }
        } else setEnabled(false);
      } catch { if (alive) setError(t('تعذر توصيل الإشعارات. حاول مرة أخرى.', 'Could not connect notifications. Try again.')); }
    };
    void refresh();
    window.addEventListener('focus', refresh);
    return () => { alive = false; window.removeEventListener('focus', refresh); };
  }, [uid]);
  async function toggle() {
    setBusy(true); setError('');
    try { if (enabled) await disablePush(); else await enablePush(uid); setEnabled(!enabled); }
    catch (e) { setError(e instanceof Error ? e.message : t('تعذر تفعيل الإشعارات', 'Could not enable notifications')); }
    finally { setBusy(false); }
  }
  return <div className="push-settings">
    {supported === false ? <p>{t('على iPhone وiPad، أضف كابيندا إلى الشاشة الرئيسية ثم افتحه من الأيقونة. استخدم متصفحاً يدعم الإشعارات واتصال HTTPS.', 'On iPhone and iPad, add Kapinda to your Home Screen and open its icon. Use a browser with notification support and HTTPS.')}</p> : <button disabled={busy || supported === null} onClick={() => void toggle()}>{busy ? t('جاري الاتصال…', 'Connecting…') : enabled ? t('إيقاف إشعارات هذا الجهاز', 'Disable notifications on this device') : t('تفعيل إشعارات هذا الجهاز', 'Enable notifications on this device')}</button>}
    <p className="muted">{t('تحديثات الطلبات حتى عند إغلاق كابيندا.', 'Order updates even when Kapinda is closed.')}</p>
    {error && <p role="alert">{error}</p>}
  </div>;
}
