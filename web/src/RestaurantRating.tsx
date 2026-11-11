import { useEffect, useState } from 'react';
import { api } from './api';
import type { Account, Restaurant } from './types';
import type { Translate } from './Orders';

export default function RestaurantRating({ restaurant, account, t, onLogin, onRated }: {
  restaurant: Restaurant; account: Account | null; t: Translate;
  onLogin: () => void; onRated: () => Promise<void>;
}) {
  const [stars, setStars] = useState(0);
  const [feedback, setFeedback] = useState('');
  const [savedFeedback, setSavedFeedback] = useState('');
  const [saved, setSaved] = useState(0);
  const [loading, setLoading] = useState(account?.role === 'CUSTOMER');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    if (account?.role !== 'CUSTOMER') return;
    let cancelled = false;
    setLoading(true); setError('');
    api<{ myRating?: number; feedback?: string }>(`/account/restaurant-ratings/${restaurant.id}`, {}, true)
      .then(result => { if (!cancelled) { setFeedback(result.feedback || ''); setSavedFeedback(result.feedback || ''); setStars(result.myRating || 0); setSaved(result.myRating || 0); } })
      .catch(e => { if (!cancelled) setError(e instanceof Error ? e.message : 'Could not load rating'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [restaurant.id, account?.uid, account?.role, retry]);

  async function save() {
    if (!stars || busy) return;
    setBusy(true); setError(''); setNotice('');
    try {
      await api(`/account/restaurant-ratings/${restaurant.id}`, { method: 'PUT', body: JSON.stringify({ stars, feedback }) }, true);
      setSaved(stars); setSavedFeedback(feedback); setNotice(t('تم حفظ تقييمك، شكراً لك!', 'Your rating is saved. Thank you!'));
    } catch (e) {
      setError(e instanceof Error ? e.message : t('تعذر حفظ التقييم', 'Could not save your rating'));
      setBusy(false); return;
    }
    try { await onRated(); }
    catch { setError(t('تم حفظ تقييمك. أعد تحميل الصفحة لتحديث المتوسط.', 'Your rating was saved. Reload to refresh the average.')); }
    finally { setBusy(false); }
  }
  return <section className="panel restaurant-rating" aria-label={t('تقييم المطعم', 'Restaurant rating')}>
    <div><h2>{t('رأيك يهمنا', 'Restaurant feedback')}</h2><p className="muted">{restaurant.ratingCount ? `${restaurant.rating} / 5 · ${restaurant.ratingCount} ${t('تقييم', 'ratings')}` : t('كن أول من يقيّم المطعم.', 'Be the first to rate this restaurant.')}</p></div>
    {!account ? <button type="button" onClick={onLogin}>{t('سجل الدخول للتقييم', 'Sign in to rate')}</button> : account.role !== 'CUSTOMER' ? <p className="muted">{t('التقييم متاح لحسابات العملاء.', 'Ratings are available to customer accounts.')}</p> : <>
      <fieldset disabled={loading || busy} className="rating-stars">
        <legend>{saved ? t('تعديل تقييمك', 'Update your rating') : t('اختار من نجمة إلى خمس نجوم', 'Choose one to five stars')}</legend>
        <div dir="ltr">{[1, 2, 3, 4, 5].map(value => <label key={value} className={value <= stars ? 'selected' : ''}>
          <input type="radio" name={`rating-${restaurant.id}`} value={value} checked={stars === value} onChange={() => { setStars(value); setNotice(''); }} aria-label={`${value} ${t('من ٥ نجوم', 'out of 5 stars')}`}/><span aria-hidden="true">★</span>
        </label>)}</div>
      </fieldset>
      <p className="muted">{t('سيظهر تقييمك وتعليقك للجميع باسم عميل.', 'Your rating and comment will be visible to everyone as Customer.')}</p><label>{t('شاركنا رأيك', 'Share your feedback')}<textarea value={feedback} maxLength={2000} disabled={loading || busy} onChange={e => { setFeedback(e.target.value); setNotice(''); }} placeholder={t('كيف كانت تجربتك؟', 'How was your experience?')}/></label>
      <button type="button" className="primary" disabled={loading || busy || !stars || (stars === saved && feedback === savedFeedback)} onClick={() => void save()}>{busy ? t('جاري الحفظ…', 'Saving…') : t('حفظ التقييم', 'Save feedback')}</button>
    </>}
    {notice && <p className="success-note" role="status">{notice}</p>}
    {error && <div role="alert"><p className="alert">{error}</p><button type="button" disabled={busy || loading} onClick={() => setRetry(value => value + 1)}>{t('إعادة المحاولة', 'Retry')}</button></div>}
  </section>;
}
