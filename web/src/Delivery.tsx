import useAutoRefresh from './useAutoRefresh';
import useHistoryState from './useHistoryState';
import { useEffect, useRef, useState } from 'react';
import { api } from './api';
import { type Translate } from './Orders';
import OrderWorkspace from './OrderWorkspace';
import type { Order } from './types';
type Task = { assignedToMe: boolean; assignedToOther: boolean; canClaim: boolean; canUpdate: boolean; order: Order };
export default function Delivery({ t, money, supervisor = false }: { t: Translate; money: (n: number) => string; supervisor?: boolean }) {
  const [tasks, setTasks] = useState<Task[]>([]), [tab, setTab] = useHistoryState<string>('deliveries', 'available');
  const [busy, setBusy] = useState(false), [error, setError] = useState('');
  const requestVersion = useRef(0);
  async function load() { const version = ++requestVersion.current; const next = await api<Task[]>('/delivery/orders', {}, true); if (version === requestVersion.current) setTasks(next); }
  async function run(task: () => Promise<void>) { setBusy(true); setError(''); try { await task(); } catch (e) { setError(e instanceof Error ? e.message : 'Request failed'); } finally { setBusy(false); } }
  useEffect(() => { setTasks([]); void run(load); return () => { requestVersion.current++; }; }, [supervisor]);
  useAutoRefresh(load, !busy);
  const visible = tasks.filter(task => tab === 'available' ? !task.assignedToMe && !task.assignedToOther && ['PLACED','ACCEPTED','PREPARING','READY'].includes(task.order.status) : tab === 'mine' ? (supervisor || task.assignedToMe) && !task.canClaim && !['DELIVERED','CANCELLED'].includes(task.order.status) : (supervisor || task.assignedToMe) && ['DELIVERED','CANCELLED'].includes(task.order.status));
  return <section className="page delivery-page"><div className="section-heading"><div><span className="eyebrow">{t('مساحة التوصيل', 'DELIVERY WORKSPACE')}</span><h1>{supervisor ? t('إدارة كل التوصيلات', 'Delivery management') : t('طلبات التوصيل', 'Delivery orders')}</h1><p className="muted">{t('تابع الطلبات الجديدة واستلمها لما تكون جاهزة.','Follow new orders. Collect when ready.')}</p></div><button disabled={busy} onClick={() => void run(load)}>{t('تحديث', 'Refresh')} ↻</button></div><div className="categories">{[['available',t('طلبات متاحة', 'Available')],['mine',supervisor ? t('كل التوصيلات الحالية', 'All active deliveries') : t('توصيلاتي الحالية', 'My deliveries')],['history',t('سجل التوصيلات', 'Completed')]].map(([key,label]) => <button key={key} className={tab === key ? 'chosen' : ''} onClick={() => setTab(key)}>{label}</button>)}</div>{error && <p className="alert" role="alert">{error}</p>}{busy && !tasks.length && <p role="status">{t('جاري تحميل الطلبات…','Loading deliveries…')}</p>}<OrderWorkspace orders={visible.map(task=>task.order)} t={t} money={money} action={order=>{
 const task=tasks.find(task=>task.order.id===order.id);
 if(task?.canClaim) return <button className="primary" disabled={busy} onClick={()=>void run(async()=>{await api('/delivery/orders/'+order.id+'/claim',{method:'POST'},true);await load();setTab('mine');})}>{t('استلام الطلب','Claim order')}</button>;
 if(task?.canUpdate && ['READY','OUT_FOR_DELIVERY'].includes(order.status)) return <button className="primary" disabled={busy} onClick={()=>void run(async()=>{await api('/delivery/orders/'+order.id+'/status?status='+(order.status==='READY'?'OUT_FOR_DELIVERY':'DELIVERED'),{method:'PUT'},true);await load();})}>{order.status==='READY'?t('خرجت للتوصيل','Start delivery'):t('تم التوصيل','Mark delivered')}</button>;
 return <small className="muted">{['PLACED','ACCEPTED','PREPARING'].includes(order.status)?t('في انتظار المطعم','Waiting for restaurant'):t('للعرض فقط','View only')}</small>;
 }}/></section>;
}
