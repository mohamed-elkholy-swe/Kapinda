import { useState, type FormEvent } from 'react';
import type { User } from 'firebase/auth';
import { api } from './api';
import Avatar from './Avatar';
import { roleLabel } from './UserAccess';
import Orders, { type Translate } from './Orders';
import type { Account } from './types';

type Props = { user: User; account: Account | null; t: Translate; money: (n: number) => string; onSaved: (account: Account) => void; onOrders: () => void; onOpen: (id: string) => void; onBrowse: () => void; onLogout: () => void };
export default function Profile({ user, account, t, money, onSaved, onOrders, onOpen, onBrowse, onLogout }: Props) {
  const [busy, setBusy] = useState(false), [error, setError] = useState(''), [saved, setSaved] = useState(false);
  const name = account?.displayName || user.displayName || '';
  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); const data = new FormData(event.currentTarget);
    setBusy(true); setError(''); setSaved(false);
    try { const next = await api<Account>('/account/me', { method: 'PUT', body: JSON.stringify({ displayName: String(data.get('name')).trim(), phoneNumber: String(data.get('phone')).trim() }) }, true); onSaved(next); setSaved(true); }
    catch (e) { setError(e instanceof Error ? e.message : 'Could not save profile'); }
    finally { setBusy(false); }
  }
  return <section className="page profile-page"><span className="eyebrow">{t('مساحتك في كابيندا', 'YOUR PLACE AT THE KAPINDA TABLE')}</span><h1>{t('حسابي', 'My account')}</h1><div className="profile-layout"><aside className="profile-identity panel"><Avatar key={user.uid} uid={user.uid} large/><h2>{name || t('أهلاً بيك', 'Welcome home')}</h2><p className="email-text" dir="ltr">{account?.email || user.email}</p><span className="member-tag">{account ? roleLabel(account.role, t) : t('من أهل كابيندا', 'Part of the neighbourhood')}</span>{account && <button className="full" onClick={onOrders}>{t('كل طلباتي', 'All my orders')} ↗</button>}<button className="text-button full danger" onClick={onLogout}>{t('تسجيل الخروج', 'Sign out')}</button></aside><div className="profile-content"><form className="panel form" onSubmit={save} key={account?.uid || user.uid}><div className="section-heading"><div><h2>{t('بياناتك الشخصية', 'Personal details')}</h2><p className="muted">{t('خلّي بياناتك محدثة عشان نوصلك بسهولة.', 'A few details to make every delivery easier.')}</p><span className="detail-icon">♙</span></div></div><label>{t('الاسم', 'Full name')}<input name="name" defaultValue={name} required maxLength={100} autoComplete="name" placeholder={t('تحب نناديك بإيه؟', 'What should we call you?')}/></label><label>{t('البريد الإلكتروني', 'Email address')}<input value={account?.email || user.email || ''} readOnly type="email" dir="ltr"/><small className="muted">{t('مرتبط بحساب تسجيل الدخول الخاص بك.', 'Connected to your sign-in account.')}</small></label><label>{t('رقم الموبايل', 'Phone number')}<input name="phone" type="tel" defaultValue={account?.phoneNumber || user.phoneNumber || ''} required minLength={10} maxLength={20} pattern="[+0-9 ()-]{10,20}" autoComplete="tel" placeholder="01xxxxxxxxx" dir="ltr"/></label>{error && <p className="alert" role="alert">{error}</p>}{saved && <p className="success-note" role="status">✓ {t('تم حفظ بياناتك بنجاح', 'Your details are saved')}</p>}<div className="form-actions"><span className="muted">{t('بياناتك ظاهرة لك فقط.', 'Your profile is visible only to you.')}</span><button className="primary" disabled={busy || !account}>{busy ? t('جاري الحفظ…', 'Saving…') : t('حفظ التغييرات', 'Save changes')}</button></div></form>{account && <><Orders accountUid={user.uid} t={t} money={money} compact onOpen={onOpen} onBrowse={onBrowse}/><button className="text-button full" onClick={onOrders}>{t('عرض سجل الطلبات بالكامل', 'View your complete order history')} ↗</button></>}</div></div></section>;
}
