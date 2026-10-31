import { useEffect, useState, type FormEvent } from 'react';
import { api } from './api';
import type { Account, Restaurant, Role } from './types';
import type { Translate } from './Orders';
import { assignableRoles, canManageUser } from './permissions';

export function roleLabel(role: Role, t: Translate) {
  return { CUSTOMER: t('عميل', 'Customer'), ADMIN: t('مدير رئيسي', 'Administrator'), SUB_ADMIN: t('مدير مساعد', 'Sub administrator'), DELIVERY: t('مندوب توصيل', 'Delivery person'), RESTAURANT: t('مسؤول مطعم', 'Restaurant staff') }[role];
}
export default function UserAccess({ t, restaurants, actorUid, actorRole }: { t: Translate; restaurants: Restaurant[]; actorUid?: string; actorRole: Role }) {
  const [users, setUsers] = useState<Account[]>([]), [query, setQuery] = useState('');
  const [selected, setSelected] = useState<Account | null>(null), [role, setRole] = useState<Role>('CUSTOMER');
  const [ids, setIds] = useState<string[]>([]), [phone, setPhone] = useState('');
  const [busy, setBusy] = useState(false), [error, setError] = useState(''), [notice, setNotice] = useState('');
  const locked = selected ? !canManageUser(actorRole, selected.role) : false;
  useEffect(() => { setSelected(null); setRole('CUSTOMER'); setIds([]); }, [actorRole]);
  async function run(task: () => Promise<void>) {
    setBusy(true); setError(''); setNotice('');
    try { await task(); } catch (e) { setError(e instanceof Error ? e.message : 'Request failed'); } finally { setBusy(false); }
  }
  async function search() { setUsers(await api<Account[]>(`/admin/users?search=${encodeURIComponent(query)}`, {}, true)); }
  useEffect(() => { void run(async () => { await search(); setPhone((await api<{supportPhone: string}>('/settings')).supportPhone); }); }, []);
  function choose(user: Account) { setSelected(user); setRole(user.role); setIds((user.restaurantIds || []).filter(id => restaurants.some(r => r.id === id))); setNotice(''); }
  async function save(event: FormEvent) {
    event.preventDefault(); if (!selected?.uid || locked) return;
    const uid = selected.uid;
    await run(async () => {
      const updated = await api<Account>(`/admin/users/${encodeURIComponent(uid)}/access`, { method: 'PUT', body: JSON.stringify({ role, restaurantIds: role === 'RESTAURANT' ? ids : [] }) }, true);
      setSelected(updated); setUsers(current => current.map(u => u.uid === uid ? updated : u)); setNotice(t('تم حفظ الصلاحيات. يتم تطبيقها على الطلب التالي.', 'Access saved. Permissions apply to the next request.'));
    });
  }
  return <><div className="section-heading"><div><h2>{t('المستخدمون والصلاحيات', 'People & access')}</h2><p className="muted">{t('ابحث بالبريد، الاسم، الموبايل أو معرف الحساب. لازم المستخدم يسجل دخول مرة أولاً.', 'Search email, name, phone or account ID. Users must sign in once before you can assign access.')}</p></div></div>
    {error && <p className="alert" role="alert">{error}</p>}{notice && <p className="success-note" role="status">✓ {notice}</p>}
    <form className="toolbar" onSubmit={e => { e.preventDefault(); void run(search); }}><label className="search"><span>⌕</span><input aria-label={t('ابحث عن مستخدم', 'Find a user')} value={query} onChange={e => setQuery(e.target.value)} placeholder="name@example.com"/></label><button className="primary" disabled={busy}>{t('بحث', 'Search')}</button></form>
    <div className="access-layout"><div className="panel people-list"><span className="eyebrow">{t('حتى ٥٠ نتيجة — ضيق البحث للوصول للشخص المطلوب', 'UP TO 50 RESULTS — NARROW YOUR SEARCH TO FIND SOMEONE')}</span>{!users.length && <p className="muted">{t('لا توجد نتائج.', 'No users found.')}</p>}{users.map(user => <button className={`person-row ${selected?.uid === user.uid ? 'person-selected' : ''}`} key={user.uid} onClick={() => choose(user)}><span className="person-initial">{(user.displayName || user.email || '?').slice(0, 1).toUpperCase()}</span><span><strong>{user.displayName || t('بدون اسم', 'Unnamed account')}</strong><small dir="ltr">{user.email}</small><small>{user.phoneNumber || '—'} · {roleLabel(user.role, t)}</small></span><span>↗</span></button>)}</div>
    <form className="panel form" onSubmit={save}><span className="eyebrow">{t('الصلاحية المناسبة، للشخص المناسب', 'THE RIGHT ACCESS FOR THE RIGHT PERSON')}</span><h2>{selected ? selected.displayName || selected.email : t('اختار مستخدم', 'Choose a person')}</h2>{selected ? <><p className="muted email-text" dir="ltr">{selected.email}</p><label>{t('الدور', 'Role')}<select value={role} onChange={e => setRole(e.target.value as Role)} disabled={busy || locked || selected.uid === actorUid}>{(locked ? ['ADMIN'] as Role[] : assignableRoles(actorRole)).map(value => <option value={value} key={value}>{roleLabel(value, t)}</option>)}</select></label>{locked && <p className="muted">{t('المدير الرئيسي فقط يمكنه تغيير صلاحيات حساب مدير رئيسي.', 'Only a full administrator can change an Administrator account.')}</p>}{actorRole === 'SUB_ADMIN' && !locked && <p className="muted">{t('يمكنك إدارة الأدوار الأخرى. منح دور المدير الرئيسي غير متاح.', 'You can manage the other roles. Granting Administrator access is not available.')}</p>}{selected.uid === actorUid && <p className="muted">{t('لا يمكنك إزالة صلاحية الإدارة من حسابك.', 'You cannot remove your own administrator access.')}</p>}
    {role === 'RESTAURANT' && <fieldset className="assignment-picker"><legend>{t('المطاعم المسموح بتعديلها', 'Restaurants this person can manage')}</legend><p className="muted">{t('اختار مطعم واحد أو أكثر. لا يمكنه تعديل أي مطعم غير محدد.', 'Select one or more. Other restaurants remain inaccessible.')}</p>{restaurants.map(r => <label key={r.id}><input type="checkbox" checked={ids.includes(r.id)} onChange={e => setIds(current => e.target.checked ? [...current, r.id] : current.filter(id => id !== r.id))}/><span>{r.name}<small>{r.category}</small></span></label>)}{!restaurants.length && <p>{t('ضيف مطعم أولاً.', 'Create a restaurant first.')}</p>}</fieldset>}
    <button className="primary" disabled={busy || locked || (role === 'RESTAURANT' && !ids.length)}>{t('حفظ الصلاحيات', 'Save access')}</button></> : <div className="empty">{t('اختار شخص من النتائج لتعديل دوره.', 'Select a person from the results to manage their access.')}</div>}</form></div>
    <form className="panel form support-settings" onSubmit={e => { e.preventDefault(); void run(async () => { setPhone((await api<{supportPhone:string}>('/admin/settings', { method: 'PUT', body: JSON.stringify({ supportPhone: phone.trim() }) }, true)).supportPhone); setNotice(t('تم حفظ رقم المساعدة', 'Support number saved')); }); }}><div><span className="eyebrow">{t('خليك قريب من عملائك', 'A HELPING HAND, ONE CALL AWAY')}</span><h2>{t('رقم المساعدة للطلبات', 'Order support phone')}</h2><p className="muted">{t('يظهر للعملاء في الطلبات الحالية وسجل الطلبات.', 'Shown to customers alongside their current and past orders.')}</p></div><label>{t('رقم المساعدة', 'Support phone')}<input type="tel" value={phone} onChange={e => setPhone(e.target.value)} pattern="[+0-9 ()-]{10,20}" maxLength={20} dir="ltr" placeholder="+20…"/></label><button className="primary" disabled={busy}>{t('حفظ رقم المساعدة', 'Save support number')}</button></form>
  </>;
}
