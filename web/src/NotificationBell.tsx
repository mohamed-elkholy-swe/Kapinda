import { useEffect, useRef, useState } from 'react';
import { api } from './api';
import { statusLabel, type Translate } from './Orders';
import type { Order } from './types';
import useAutoRefresh from './useAutoRefresh';
type Notification={id:string;orderId:number;status:Order['status'];createdAt:string;read:boolean;target:'orders'|'admin'|'restaurantOrders'|'delivery'};
export default function NotificationBell({t,onOpen}:{t:Translate;onOpen:(target:Notification['target'],status:Order['status'])=>void}) {
 const [items,setItems]=useState<Notification[]>([]),[open,setOpen]=useState(false),[error,setError]=useState('');
 const [busy,setBusy]=useState(false);
 const request=useRef(0),mutating=useRef(false);
 const alive=useRef(true),panel=useRef<HTMLDivElement>(null);
 async function load(){if(mutating.current)return;const version=++request.current;try{const next=await api<Notification[]>('/notifications',{},true);if(alive.current && version===request.current){setItems(next);setError('');}}catch{if(alive.current && version===request.current)setError(t('تعذر تحميل الإشعارات','Could not load notifications'));}}
 useEffect(()=>{alive.current=true;void load();return()=>{alive.current=false;};},[]);
 useAutoRefresh(load,!busy);
 useEffect(()=>{if(!open)return;const close=(e:PointerEvent)=>{if(!panel.current?.contains(e.target as Node))setOpen(false);};const escape=(e:KeyboardEvent)=>{if(e.key==='Escape')setOpen(false);};document.addEventListener('pointerdown',close);document.addEventListener('keydown',escape);return()=>{document.removeEventListener('pointerdown',close);document.removeEventListener('keydown',escape);};},[open]);
 async function update(task:()=>Promise<void>){if(mutating.current)return;mutating.current=true;request.current++;setBusy(true);setError('');try{await task();}catch{if(alive.current)setError(t('تعذر تحديث الإشعارات. حاول مرة أخرى.','Could not update notifications. Try again.'));}finally{mutating.current=false;if(alive.current)setBusy(false);}}
 async function select(item:Notification){await update(async()=>{await api('/notifications/'+encodeURIComponent(item.id)+'/read',{method:'PUT'},true);if(!alive.current)return;setItems(current=>current.map(n=>n.id===item.id?{...n,read:true}:n));setOpen(false);onOpen(item.target,item.status);});}
 async function readAll(){await update(async()=>{await api('/notifications/read-all',{method:'PUT'},true);if(alive.current)setItems(current=>current.map(n=>({...n,read:true})));});}
 async function removeAll(){await update(async()=>{await api('/notifications',{method:'DELETE'},true);if(alive.current)setItems([]);});}
 const unread=items.filter(n=>!n.read).length;
 return <div className="notification-bell" ref={panel}><button className="notification-trigger" aria-label={t('الإشعارات','Notifications')+(unread?' ('+unread+')':'')} aria-expanded={open} aria-controls="notification-panel" onClick={()=>{setOpen(!open);if(!open)void load();}}><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg>{unread>0&&<b className="notification-count">{unread>99?'99+':unread}</b>}</button>{open&&<section id="notification-panel" className="notification-panel panel" aria-label={t('الإشعارات','Notifications')}><div className="section-heading"><h2>{t('الإشعارات','Notifications')}</h2><button disabled={busy} onClick={()=>void load()}>{t('تحديث','Refresh')}</button></div><div className="notification-actions"><button disabled={busy||!unread} onClick={()=>void readAll()}>{t('قراءة الكل','Mark all as read')}</button><button disabled={busy||!items.length} onClick={()=>void removeAll()}>{t('إزالة الكل','Remove all')}</button></div>{error&&<p role="alert">{error}</p>}{!error&&!items.length&&<p className="muted">{t('لا توجد إشعارات بعد','No notifications yet')}</p>}{items.map(item=><button disabled={busy} className={'notification-item '+(!item.read?'unread':'')} key={item.id} onClick={()=>void select(item)}><strong>{t('طلب','Order')} #{item.orderId}</strong><span>{statusLabel(item.status,t)}</span><small>{new Date(item.createdAt).toLocaleString(t('ar-EG','en-EG'))}</small></button>)}</section>}</div>;
}
