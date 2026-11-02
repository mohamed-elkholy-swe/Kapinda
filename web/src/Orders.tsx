import useAutoRefresh from './useAutoRefresh';
import { useEffect, useRef, useState } from 'react';
import { api, auth } from './api';
import { requestAccountHistory, type AccountHistory } from './accountHistory';
import { statuses, type Order } from './types';

export type Translate = (ar: string, en: string) => string;
export function statusLabel(status: Order['status'], t: Translate) {
  const ar = ['تم الطلب', 'تم القبول', 'قيد التحضير', 'جاهز للاستلام', 'في الطريق', 'تم التوصيل', 'ملغي'];
  const en = ['Placed', 'Accepted', 'Preparing', 'Ready', 'On the way', 'Delivered', 'Cancelled'];
  return t(ar[statuses.indexOf(status)], en[statuses.indexOf(status)]);
}
export const activeOrder = (order: Order) => !['DELIVERED', 'CANCELLED'].includes(order.status);
export function StatusBadge({ status, t }: { status: Order['status']; t: Translate }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}><i/>{statusLabel(status, t)}</span>;
}
function Progress({order,t}:{order:Order;t:Translate}) {
  const steps=['PLACED','PREPARING','OUT_FOR_DELIVERY','DELIVERED'] as const;
  const current=statuses.indexOf(order.status);
  return order.status==='CANCELLED'?<p className="cancelled-note">{t('تم إلغاء هذا الطلب. تواصل معنا لو محتاج مساعدة.','This order was cancelled. Contact us if you need help.')}</p>:<ol className="order-progress" aria-label={t('مراحل الطلب','Order progress')}>{steps.map((step,index)=><li key={step} className={current>=statuses.indexOf(step)?'step-done':''}><span>{current>=statuses.indexOf(step)?'✓':index+1}</span><small>{statusLabel(step,t)}</small></li>)}</ol>;
}
type Props = { accountUid: string; t: Translate; money: (n: number) => string; onOpen?: (id: string) => void; onBrowse: () => void; compact?: boolean };
export default function Orders({ accountUid, t, money, onBrowse, compact = false }: Props) {
  const [orders,setOrders]=useState<Order[]>([]),[loading,setLoading]=useState(true);
  const [error,setError]=useState(''),[filter,setFilter]=useState('all'),[expanded,setExpanded]=useState('');
  const [phone,setPhone]=useState('');
  const requestVersion=useRef(0);
  async function load(quiet=false){const version=++requestVersion.current;if(!quiet){setOrders([]);setExpanded('');setLoading(true);}setError('');try{const next=await requestAccountHistory(accountUid,()=>api<AccountHistory>('/account/me/orders',{},true),()=>auth?.currentUser?.uid||null);if(version===requestVersion.current)setOrders(next);}catch(e){if(version===requestVersion.current)setError(e instanceof Error?e.message:t('تعذر تحميل الطلبات','Could not load orders'));}finally{if(version===requestVersion.current)setLoading(false);}}
  useEffect(()=>{setFilter('all');void load();let alive=true;void api<{supportPhone:string}>('/settings').then(settings=>{if(alive)setPhone(settings.supportPhone);}).catch(()=>{});return()=>{alive=false;requestVersion.current++;};},[accountUid]);
  useAutoRefresh(() => load(true), !loading);
  const filtered=orders.filter(o=>filter==='all'||(filter==='active'?activeOrder(o):!activeOrder(o)));
  const support=phone.replace(/[^+0-9]/g,'');
  return <section className={compact?'profile-orders':'page orders-page'}><div className="section-heading"><div><span className="eyebrow">{t('من أول لقمة، لآخر طلب','FROM YOUR NEXT MEAL TO YOUR FAVOURITES')}</span>{compact?<h2>{t('سجل طلباتك','Your order history')}</h2>:<h1>{t('تابع طلباتك','Follow your orders')}</h1>}<p className="muted">{t('كل طلبات حسابك، حالتها وتفاصيلها في مكان واحد.','Your orders, progress and details. All in one place.')}</p></div><button onClick={()=>void load()} disabled={loading}>{t('تحديث الطلبات','Refresh orders')} ↻</button></div>
  <div className="order-support-banner"><span className="support-icon">☎</span><div><strong>{t('الطلب اتأخر أو في مشكلة؟','Running late? Something not right?')}</strong><p>{t('إحنا هنا عشان نساعدك.','We’re here to help with your order.')}</p></div>{support?<a className="call-button" href={`tel:${support}`}>{t('اتصل بالمساعدة','Call support')} <span dir="ltr">{phone}</span></a>:<small className="muted">{t('رقم المساعدة لم يتم تحديده بعد.','A support phone hasn’t been set yet.')}</small>}</div>
  <div className="categories">{[['all',t('كل طلباتي','All my orders')],['active',t('طلبات حالية','In progress')],['past',t('طلبات سابقة','Past orders')]].map(([key,label])=><button className={filter===key?'chosen':''} key={key} onClick={()=>setFilter(key)}>{label}<span className="count-pill">{loading ? '—' : orders.filter(o=>key==='all'||(key==='active'?activeOrder(o):!activeOrder(o))).length}</span></button>)}</div>
  {error&&<p className="alert" role="alert">{error}<button onClick={()=>void load()}>{t('إعادة المحاولة','Retry')}</button></p>}
  {loading?<div className="empty" role="status">{t('جاري تحميل طلباتك…','Loading your orders…')}</div>:!error&&!filtered.length?<div className="empty illustrated-empty"><span>♧</span><h2>{t('مفيش طلبات هنا حالياً','No orders here just yet')}</h2><p>{t('أول أكلة حلوة مستنياك.','Your next favourite meal is waiting.')}</p><button className="primary" onClick={onBrowse}>{t('اكتشف المطاعم','Explore restaurants')} ↗</button></div>:!error&&<div className="order-cards tracking-cards">{filtered.map(order=><article className="history-card tracking-card" key={order.id}><div className="order-card-top"><div><span className="eyebrow">{t('طلب','ORDER')} #{order.id}</span><time dateTime={order.createdAt}>{new Date(order.createdAt).toLocaleString(t('ar-EG','en-EG'),{day:'numeric',month:'long',year:'numeric',hour:'2-digit',minute:'2-digit'})}</time></div><StatusBadge status={order.status} t={t}/></div><p className="order-customer"><strong>{t('العميل', 'Client')}:</strong> {order.customerName || t('الاسم غير متاح', 'Name unavailable')}</p><h3>{[...new Set(order.items.map(item=>item.restaurantName).filter(Boolean))].join(' · ') || order.items.map(item=>item.itemName).join(' · ')}</h3><p className="muted">{order.items.map(item=>`${item.quantity} × ${item.itemName}`).join(' · ')}</p><Progress order={order} t={t}/><div className="order-destination"><span>⌖</span><p>{order.area}<small>{order.customerAddress}</small></p></div><div className="order-card-bottom"><strong>{money(order.total)}</strong><button aria-expanded={expanded===order.id} onClick={()=>setExpanded(expanded===order.id?'':order.id)}>{expanded===order.id?t('إخفاء التفاصيل','Hide details'):t('تفاصيل الطلب','Order details')} {expanded===order.id?'−':'+'}</button></div>{expanded===order.id&&<div className="order-receipt">{order.items.map((item,index)=><div className="total-row" key={index}><span>{item.quantity} × {item.itemName}</span><span>{money(item.lineTotal)}</span></div>)}<div className="total-row"><span>{t('التوصيل','Delivery')}</span><span>{money(order.deliveryFee)}</span></div><p className="muted">{t('الدفع نقداً عند الاستلام','Cash on delivery')}</p></div>}{support&&<a className="order-help-link" href={`tel:${support}`}>☎ {t('مساعدة بخصوص الطلب','Need help with this order?')} <span dir="ltr">{phone}</span></a>}</article>)}</div>}
  </section>;
}
