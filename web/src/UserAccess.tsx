import { useEffect, useState, type FormEvent } from 'react';
import { api } from './api';
import type { Account, Restaurant, Role } from './types';
import type { Translate } from './Orders';
import UserHistory from './UserHistory';
import { assignableRoles, canManageUser } from './permissions';

export function roleLabel(role: Role, t: Translate) {
  return { CUSTOMER: t('عميل', 'Customer'), ADMIN: t('مدير رئيسي', 'Administrator'), SUB_ADMIN: t('مدير مساعد', 'Sub administrator'), DELIVERY: t('مندوب توصيل', 'Delivery person'), RESTAURANT: t('مسؤول مطعم', 'Restaurant staff') }[role];
}

export default function UserAccess({ t, restaurants, actorUid, actorRole }: { t: Translate; restaurants: Restaurant[]; actorUid?: string; actorRole: Role }) {
  const [users, setUsers] = useState<Account[]>([]), [query, setQuery] = useState('');
  const [section, setSection] = useState<'' | 'DELIVERY' | 'RESTAURANT'>('');
  const [selected, setSelected] = useState<Account | null>(null), [role, setRole] = useState<Role>('CUSTOMER');
  const [ids, setIds] = useState<string[]>([]);
  const [busy, setBusy] = useState(false), [error, setError] = useState(''), [notice, setNotice] = useState('');
  const locked = selected ? !canManageUser(actorRole, selected.role) : false;
  useEffect(() => { setSelected(null); setRole('CUSTOMER'); setIds([]); }, [actorRole]);
  async function run(task: () => Promise<void>) {
    setBusy(true); setError(''); setNotice('');
    try { await task(); } catch (e) { setError(e instanceof Error ? e.message : 'Request failed'); } finally { setBusy(false); }
  }
  async function search(filter = section) {
    const params = new URLSearchParams({ search: query });
    if (filter) params.set('role', filter);
    setUsers(await api<Account[]>(`/admin/users?${params}`, {}, true));
  }
  useEffect(() => { void run(async () => { await search(); }); }, []);
  function choose(user: Account) { setSelected(user); setRole(user.role); setIds((user.restaurantIds || []).filter(id => restaurants.some(r => r.id === id))); setNotice(''); }
  async function save(event: FormEvent) {
    event.preventDefault(); if (!selected?.uid || locked) return;
    const uid = selected.uid;
    await run(async () => {
      const updated = await api<Account>(`/admin/users/${encodeURIComponent(uid)}/access`, { method: 'PUT', body: JSON.stringify({ role, restaurantIds: role === 'RESTAURANT' ? ids : [] }) }, true);
      setSelected(updated); setUsers(current => current.map(u => u.uid === uid ? updated : u).filter(u => !section || u.role === section)); setNotice(t('تم حفظ الصلاحيات. يتم تطبيقها على الطلب التالي.', 'Access saved. Permissions apply to the next request.'));
    });
  }
  return <>
    <div className="categories" aria-label={t('أقسام المستخدمين', 'User sections')} role="group">
      {([
        ['', t('كل المستخدمين', 'All users')],
        ['DELIVERY', t('مندوبو التوصيل', 'Delivery staff')],
        ['RESTAURANT', t('مسؤولو المطاعم', 'Restaurant staff')],
      ] as const).map(([filter, label]) => <button key={filter} className={section === filter ? 'chosen' : ''} aria-pressed={section === filter} disabled={busy} onClick={() => {
        setSection(filter);
        setSelected(null);
        setUsers([]);
        void run(() => search(filter));
      }}>{label}</button>)}
    </div>
    {error && <p className="alert" role="alert">{error}</p>}{notice && <p className="success-note" role="status">✓ {notice}</p>}
    <form className="toolbar" onSubmit={e => { e.preventDefault(); void run(search); }}><label className="search"><span>⌕</span><input aria-label={t('ابحث عن مستخدم', 'Find a user')} value={query} onChange={e => setQuery(e.target.value)} placeholder="name@example.com"/></label><button className="primary" disabled={busy}>{t('بحث', 'Search')}</button></form>
    <div className="access-layout"><div className="panel people-list"><h2>{t('المستخدمون', 'Users')}</h2><span className="eyebrow">{t('حتى ٥٠ نتيجة — ضيق البحث للوصول للشخص المطلوب', 'UP TO 50 RESULTS — NARROW YOUR SEARCH TO FIND SOMEONE')}</span>{!users.length && <p className="muted">{t('لا توجد نتائج.', 'No users found.')}</p>}{users.map(user => <button className={`person-row ${selected?.uid === user.uid ? 'person-selected' : ''}`} key={user.uid} disabled={busy} onClick={() => choose(user)}><span className="person-initial">{(user.displayName || user.email || '?').slice(0, 1).toUpperCase()}</span><span><strong>{user.displayName || t('بدون اسم', 'Unnamed account')}</strong><small dir="ltr">{user.email}</small><small>{user.phoneNumber || '—'} · {roleLabel(user.role, t)}</small></span><span>↗</span></button>)}</div>
    <form className="panel form" onSubmit={save}><h2>{t('الدور والصلاحيات', 'Role and access')}</h2>{selected && <strong>{selected.displayName || selected.email}</strong>}{selected ? <><p className="muted email-text" dir="ltr">{selected.email}</p><label>{t('الدور', 'Role')}<select value={role} onChange={e => setRole(e.target.value as Role)} disabled={busy || locked || selected.uid === actorUid}>{(locked ? ['ADMIN'] as Role[] : assignableRoles(actorRole)).map(value => <option value={value} key={value}>{roleLabel(value, t)}</option>)}</select></label>{locked && <p className="muted">{t('المدير الرئيسي فقط يمكنه تغيير صلاحيات حساب مدير رئيسي.', 'Only a full administrator can change an Administrator account.')}</p>}{actorRole === 'SUB_ADMIN' && !locked && <p className="muted">{t('يمكنك إدارة الأدوار الأخرى. منح دور المدير الرئيسي غير متاح.', 'You can manage the other roles. Granting Administrator access is not available.')}</p>}{selected.uid === actorUid && <p className="muted">{t('لا يمكنك إزالة صلاحية الإدارة من حسابك.', 'You cannot remove your own administrator access.')}</p>}
    {role === 'RESTAURANT' && <fieldset className="assignment-picker"><legend>{t('المطاعم المسموح بتعديلها', 'Restaurants this person can manage')}</legend><p className="muted">{t('اختار مطعم واحد أو أكثر. لا يمكنه تعديل أي مطعم غير محدد.', 'Select one or more. Other restaurants remain inaccessible.')}</p>{restaurants.map(r => <label key={r.id}><input type="checkbox" checked={ids.includes(r.id)} onChange={e => setIds(current => e.target.checked ? [...current, r.id] : current.filter(id => id !== r.id))}/><span>{r.name}<small>{r.category}</small></span></label>)}{!restaurants.length && <p>{t('ضيف مطعم أولاً.', 'Create a restaurant first.')}</p>}</fieldset>}
    <p className="muted">{selected.phoneNumber || '—'} · {selected.status === 'ACTIVE' ? t('نشط', 'Active') : selected.status}</p>
    {selected.role === 'RESTAURANT' && <p>{t('المطاعم المسندة', 'Assigned restaurants')} · {restaurants.filter(r => selected.restaurantIds?.includes(r.id)).map(r => r.name).join(' · ') || '—'}</p>}
    <button className="primary" disabled={busy || locked || (role === 'RESTAURANT' && !ids.length)}>{t('حفظ الصلاحيات', 'Save access')}</button></> : <div className="empty">{t('اختار شخص من النتائج لتعديل دوره.', 'Select a person from the results to manage their access.')}</div>}</form></div>
    {selected?.uid && <UserHistory key={JSON.stringify([selected?.uid, selected.role, selected.restaurantIds])} user={selected} t={t}/>}

  </>;
}
