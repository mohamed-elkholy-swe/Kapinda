import { useEffect, useRef, useState } from 'react';
import { api } from './api';
import useAutoRefresh from './useAutoRefresh';
import { type Translate } from './Orders';
import OrderWorkspace from './OrderWorkspace';
import type { Order } from './types';
export default function RestaurantOrders({t,money}:{t:Translate;money:(n:number)=>string}) {
 const [orders,setOrders]=useState<(Order & {restaurantCanUpdate?:boolean})[]>([]),[error,setError]=useState(''),[loading,setLoading]=useState(true);
 const [saving,setSaving]=useState(false);
 const nextStatus={PLACED:'ACCEPTED',ACCEPTED:'PREPARING',PREPARING:'READY'} as const;
 async function update(id:string,status:string){setSaving(true);version.current++;try{await api('/restaurant-workspace/orders/'+id+'/status?status='+status,{method:'PUT'},true);await load();}catch(e){setError(e instanceof Error?e.message:'Request failed');}finally{setSaving(false);}}
 const version=useRef(0);
 async function load(){const request=++version.current;try{const next=await api<(Order & {restaurantCanUpdate?:boolean})[]>('/restaurant-workspace/orders',{},true);if(request===version.current){setOrders(next);setError('');}}catch(e){if(request===version.current)setError(e instanceof Error?e.message:'Request failed');}finally{if(request===version.current)setLoading(false);}}
 useEffect(()=>{void load();return()=>{version.current++;};},[]);
 useAutoRefresh(load,!saving);
 return <section className="page"><div className="section-heading"><div><span className="eyebrow">{t('طلبات مطاعمك','YOUR RESTAURANT ORDERS')}</span><h1>{t('طلبات المطعم','Restaurant orders')}</h1><p className="muted">{t('طلبات مطاعمك، من القبول لحد الجاهزية.','Your kitchen, every order in one place.')}</p></div><button disabled={saving} onClick={()=>void load()}>{t('تحديث','Refresh')} ↻</button></div>{error&&<p className="alert" role="alert">{error}</p>}{loading?<p role="status">{t('جاري تحميل الطلبات…','Loading orders…')}</p>:<OrderWorkspace orders={orders} t={t} money={money} restaurant action={order=>{
 const canUpdate=orders.find(o=>o.id===order.id)?.restaurantCanUpdate;
 if(canUpdate && order.status in nextStatus) return <button className="primary" disabled={saving} onClick={()=>void update(order.id,nextStatus[order.status as keyof typeof nextStatus])}>{order.status==='PLACED'?t('قبول الطلب','Accept order'):order.status==='ACCEPTED'?t('بدء التحضير','Start preparing'):t('جاهز للاستلام','Mark ready')}</button>;
 return <small className="muted">{['PLACED','ACCEPTED','PREPARING'].includes(order.status)?t('الطلبات المشتركة تحتاج إلى المدير.','Shared orders require an administrator.'):t('للعرض فقط — اكتمل دور المطعم.','Kitchen complete · view only')}</small>;
 }}/>}</section>;
}
