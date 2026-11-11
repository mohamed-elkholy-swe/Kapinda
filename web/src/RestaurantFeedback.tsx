import { useEffect, useState } from 'react';
import { api } from './api';
import type { Translate } from './Orders';
type Feedback = { stars: number; feedback: string; updatedAt?: string | null };
type FeedbackPage = { items: Feedback[]; hasMore: boolean };
export default function RestaurantFeedback({ restaurantId, t }: { restaurantId: string; t: Translate }) {
  const [items, setItems] = useState<Feedback[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    let cancelled = false;
    setLoading(true); setError('');
    api<FeedbackPage>(`/restaurants/${restaurantId}/feedback?page=${page}`)
      .then(result => { if (!cancelled) { setItems(previous => page === 0 ? result.items : [...previous, ...result.items]); setHasMore(result.hasMore); } })
      .catch(() => { if (!cancelled) setError(t('تعذر تحميل آراء العملاء.', 'Could not load customer feedback.')); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [restaurantId, page, retry]);
  return <section className="panel customer-feedback" aria-label={t('آراء العملاء', 'Customer feedback')}><h2>{t('آراء العملاء', 'Customer feedback')}</h2>
    {items.map((item, index) => <article className="customer-review" key={index}><header><strong>{t('عميل', 'Customer')}</strong><span className="review-stars" aria-label={`${item.stars} / 5`}>{'★'.repeat(item.stars)}{'☆'.repeat(5-item.stars)}</span>{item.updatedAt && <time dateTime={item.updatedAt}>{new Date(item.updatedAt).toLocaleDateString(t('ar-EG', 'en-GB'))}</time>}</header><p dir="auto">{item.feedback}</p></article>)}
    {loading && <p role="status">{t('جاري تحميل الآراء…', 'Loading feedback…')}</p>}
    {!loading && !error && !items.length && <p className="muted">{t('لا توجد آراء مكتوبة بعد. شاركنا تجربتك!', 'No written feedback yet. Share your experience!')}</p>}
    {error && <div role="alert"><p>{error}</p><button onClick={() => setRetry(value => value + 1)}>{t('إعادة المحاولة', 'Retry')}</button></div>}
    {!loading && !error && hasMore && <button onClick={() => setPage(value => value + 1)}>{t('عرض المزيد', 'Show more')}</button>}
  </section>;
}
