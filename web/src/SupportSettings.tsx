import { useEffect, useState, type FormEvent } from 'react';
import { api } from './api';
import type { Translate } from './Orders';
export default function SupportSettings({t}:{t:Translate}) {
 const [phone,setPhone]=useState(''),[busy,setBusy]=useState(true),[error,setError]=useState(''),[notice,setNotice]=useState('');
 useEffect(()=>{let alive=true;api<{supportPhone:string}>('/settings').then(data=>{if(alive)setPhone(data.supportPhone);}).catch(()=>{if(alive)setError(t('تعذر تحميل الإعدادات','Could not load settings'));}).finally(()=>{if(alive)setBusy(false);});return()=>{alive=false;};},[]);
 async function save(e:FormEvent){e.preventDefault();setBusy(true);setError('');setNotice('');try{const data=await api<{supportPhone:string}>('/admin/settings',{method:'PUT',body:JSON.stringify({supportPhone:phone.trim()})},true);setPhone(data.supportPhone);setNotice(t('تم حفظ رقم المساعدة','Support number saved'));}catch(e){setError(e instanceof Error?e.message:'Request failed');}finally{setBusy(false);}}
 return <form className="panel form support-settings" onSubmit={save}><h2>{t('رقم المساعدة للطلبات','Order support phone')}</h2>{error&&<p className="alert" role="alert">{error}</p>}{notice&&<p className="success-note" role="status">{notice}</p>}<label>{t('رقم المساعدة','Support phone')}<input type="tel" required value={phone} onChange={e=>setPhone(e.target.value)} pattern="[+0-9 ()-]{10,20}" maxLength={20} dir="ltr"/></label><button className="primary" disabled={busy}>{t('حفظ','Save')}</button></form>;
}
