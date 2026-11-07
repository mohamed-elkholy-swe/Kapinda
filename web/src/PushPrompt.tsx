import { useEffect, useState } from 'react';
import { auth } from './api';
import { enablePush, pushEnabled, pushSupported } from './push';
import { pushChangeEvent, pushPreference, rememberPushPreference, shouldPromptForPush } from './pushPreference';
import type { Translate } from './Orders';

export default function PushPrompt({ uid, t }: { uid: string; t: Translate }) {
  const [visible, setVisible] = useState(false);
  const [busy, setBusy] = useState(false), [error, setError] = useState('');
  useEffect(() => {
    let alive = true;
    const refresh = async () => {
      try {
        const supported = await pushSupported();
        if (alive) setVisible(shouldPromptForPush(supported, 'Notification' in window ? Notification.permission : 'denied', pushEnabled(uid), pushPreference(uid)));
      } catch { if (alive) setVisible(false); }
    };
    void refresh();
    window.addEventListener(pushChangeEvent, refresh);
    window.addEventListener('focus', refresh);
    return () => { alive = false; window.removeEventListener(pushChangeEvent, refresh); window.removeEventListener('focus', refresh); };
  }, [uid]);
  function dismiss() { rememberPushPreference(uid, 'dismissed'); setVisible(false); }
  async function accept() {
    setBusy(true); setError('');
    try { await enablePush(uid); if (auth?.currentUser?.uid === uid) setVisible(false); }
    catch (e) {
      if (Notification.permission === 'denied') dismiss();
      else setError(e instanceof Error ? e.message : t('تعذر تفعيل الإشعارات. حاول مرة أخرى.', 'Could not enable notifications. Try again.'));
    } finally { setBusy(false); }
  }
  if (!visible) return null;
  return <aside className="push-prompt panel" aria-labelledby="push-prompt-title">
    <h2 id="push-prompt-title">{t('تحب توصلك تحديثات الطلبات؟', 'Get order notifications?')}</h2>
    <p>{t('فعّل الإشعارات عشان توصلك تحديثات الطلبات حتى وكابيندا مقفول. تقدر تغيّر اختيارك من جرس الإشعارات في أي وقت.', 'Receive order updates even when Kapinda is closed. You can change this anytime from the notification bell.')}</p>
    {error && <p className="alert" role="alert">{error}</p>}
    <div className="push-prompt-actions">
      <button className="primary" disabled={busy} onClick={() => void accept()}>{busy ? t('جاري التفعيل…', 'Enabling…') : t('تفعيل الإشعارات', 'Enable notifications')}</button>
      <button disabled={busy} onClick={dismiss}>{t('ليس الآن', 'Not now')}</button>
    </div>
  </aside>;
}
