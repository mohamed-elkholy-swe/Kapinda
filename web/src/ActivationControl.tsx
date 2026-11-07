import { useState } from 'react';
import { api } from './api';
import type { Translate } from './Orders';

export default function ActivationControl({ active = true, name, endpoint, t, onChanged, disabled = false }: {
  active?: boolean; name: string; endpoint: string; t: Translate;
  onChanged: () => Promise<void>; disabled?: boolean;
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function toggle() {
    if (busy) return;
    setBusy(true); setError('');
    try {
      await api(`${endpoint}/active`, { method: 'PUT', body: JSON.stringify({ active: !active }) }, true);
    } catch (e) {
      setError(e instanceof Error ? e.message : t('تعذر تحديث الحالة', 'Could not update status'));
      setBusy(false); return;
    }
    try { await onChanged(); }
    catch { setError(t('تم تغيير الحالة. أعد تحميل الصفحة لتحديث العرض.', 'Status changed. Reload the page to refresh the view.')); }
    finally { setBusy(false); }
  }
  const action = active ? t('إلغاء التفعيل', 'Deactivate') : t('تفعيل', 'Activate');
  return <div className="activation-control">
    <span className={`activation-status ${active ? 'is-active' : 'is-inactive'}`}>{active ? t('مفعل', 'Active') : t('غير مفعل', 'Inactive')}</span>
    <button type="button" disabled={disabled || busy} aria-label={`${action}: ${name}`} onClick={() => void toggle()}>{busy ? t('جاري الحفظ…', 'Saving…') : action}</button>
    {error && <span className="activation-error" role="alert">{error}</span>}
  </div>;
}
