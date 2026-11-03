import { useEffect, useState } from 'react';
import { api } from './api';
import { StatusBadge, type Translate } from './Orders';
import type { Account, Order } from './types';

type OrderSummary = Pick<Order, 'id' | 'status' | 'createdAt'> & { restaurants: string[] };

export default function UserHistory({ user, t }: { user: Account; t: Translate }) {
  const [today] = useState(() => {
    const parts = new Intl.DateTimeFormat('en', { timeZone: 'Africa/Cairo', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts();
    return ['year', 'month', 'day'].map(type => parts.find(part => part.type === type)?.value).join('-');
  });
  const [startDate, setStartDate] = useState(today);
  const [endDate, setEndDate] = useState(today);
  const validRange = !!startDate && !!endDate && startDate <= endDate;
  const [page, setPage] = useState(0);
  const [history, setHistory] = useState<{ orders: OrderSummary[]; hasNext: boolean }>();
  const [error, setError] = useState('');
  useEffect(() => {
    if (!user.uid || !validRange) return;
    let active = true;
    api<{ orders: OrderSummary[]; hasNext: boolean }>(`/admin/users/${encodeURIComponent(user.uid)}/history?startDate=${startDate}&endDate=${endDate}&page=${page}`, {}, true)
      .then(result => {
        if (!Array.isArray(result?.orders) || typeof result.hasNext !== 'boolean') {
          throw new Error('Could not read order history. Check the backend version. / تعذر قراءة سجل الطلبات. تحقق من إصدار الخادم.');
        }
        if (active) setHistory(result);
      })
      .catch(err => { if (active) setError((err as Error).message); });
    return () => { active = false; };
  }, [user.uid, startDate, endDate, validRange, page]);

  function goToPage(next: number) {
    setHistory(undefined);
    setError('');
    setPage(next);
  }

  const title = user.role === 'DELIVERY' ? t('آخر الطلبات المسندة', 'Recently assigned deliveries')
    : user.role === 'RESTAURANT' ? t('آخر طلبات المطاعم المسندة', 'Recent orders for assigned restaurants')
    : t('آخر الطلبات الشخصية', 'Recent personal orders');

  return <section className="panel user-history">
    <h2>{t('سجل الطلبات', 'Order history')} · {user.displayName || user.email}</h2>
    <p>{title}</p>
    <div className="user-history-dates">
      <label>{t('من تاريخ', 'Start date')}<input type="date" value={startDate} onChange={event => {
        goToPage(0);
        setStartDate(event.target.value);
      }}/></label>
      <label>{t('إلى تاريخ', 'End date')}<input type="date" value={endDate} onChange={event => {
        goToPage(0);
        setEndDate(event.target.value);
      }}/></label>
    </div>
    <p className="muted">{t('٢٠ طلبًا في الصفحة · توقيت القاهرة', '20 orders per page · Cairo time')}</p>
    {!validRange ? <p role="status">{t('اختر تاريخ بداية ونهاية بالترتيب الصحيح.', 'Choose a start date and an end date on or after it.')}</p> : error ? <p className="alert" role="alert">{error}</p> : !history
      ? <p role="status">{t('جاري تحميل الطلبات…', 'Loading orders…')}</p>
      : history.orders.length === 0 ? <p className="muted">{t('لا توجد طلبات في هذه الصفحة.', 'No orders on this page.')}</p>
      : history.orders.map(order => <div className="menu-line" key={order.id}>
        <div><strong>#{order.id}</strong><p>{order.restaurants.join(' · ')}</p><small>{new Date(order.createdAt).toLocaleString(undefined, { timeZone: 'Africa/Cairo' })}</small></div>
        <StatusBadge status={order.status} t={t}/>
      </div>)}
    {(page > 0 || history?.hasNext) && <div role="navigation" className="button-row" aria-label={t('صفحات سجل الطلبات', 'Order history pages')}>
      <button type="button" disabled={page === 0 || (!history && !error)} onClick={() => goToPage(page - 1)}>{t('السابق', 'Previous')}</button>
      <span role="status">{t('صفحة', 'Page')} {page + 1}</span>
      <button type="button" disabled={!history?.hasNext} onClick={() => goToPage(page + 1)}>{t('التالي', 'Next')}</button>
    </div>}
  </section>;
}

