import { useEffect, useState } from 'react';
import type { Translate } from './Orders';
import { disablePush, enablePush, pushAvailable, pushEnabled } from './push';

export default function PushSettings({ t }: { t: Translate }) {
  const [available, setAvailable] = useState<boolean | null>(null);
  const [enabled, setEnabled] = useState(pushEnabled);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => { let alive = true; void pushAvailable().then(value => { if (alive) setAvailable(value); }).catch(() => { if (alive) setAvailable(false); }); return () => { alive = false; }; }, []);
  async function toggle() {
    setBusy(true); setError('');
    try { if (enabled) await disablePush(); else await enablePush(); setEnabled(!enabled); }
    catch { setError(t('تعذر تفعيل الإشعارات. راجع إذن الإشعارات في إعدادات المتصفح وحاول مجدداً.', 'Could not update notifications. Check notification permission in browser settings and try again.')); }
    finally { setBusy(false); }
  }
  if (available === null) return null;
  if (!available) return <p className="muted">{t('إشعارات الجهاز غير متاحة هنا. على iPhone أضف التطبيق إلى الشاشة الرئيسية وافتحه منها.', 'Device notifications are unavailable here. On iPhone, add the app to your Home Screen and open it there.')}</p>;
  return <div><button disabled={busy} onClick={() => void toggle()}>{enabled ? t('إيقاف إشعارات هذا الجهاز', 'Disable notifications on this device') : t('تفعيل إشعارات هذا الجهاز', 'Enable notifications on this device')}</button>{error && <p role="alert">{error}</p>}</div>;
}
