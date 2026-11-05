import LaunchCountdown from './LaunchCountdown';
import HomeBrowse from './HomeBrowse';
import Icon from './Icon';
import RestaurantOrders from './RestaurantOrders';
import NotificationBell from './NotificationBell';
import useHistoryState from './useHistoryState';
import useAutoRefresh from './useAutoRefresh';
import RestaurantImage from './RestaurantImage';
import { useEffect, useRef, useState, type FormEvent } from 'react';
import { GoogleAuthProvider, createUserWithEmailAndPassword, onAuthStateChanged, signInWithEmailAndPassword, signInWithPopup, signOut, type User } from 'firebase/auth';
import { api, auth } from './api';
import { statuses, type Account, type Line, type Order, type Restaurant } from './types';
import Admin from './Admin';
import Avatar from './Avatar';
import BrandMark from './BrandMark';

import { isAdministrator, canUseDelivery, canManageRestaurants } from './permissions';
import { foodEmoji } from './visuals';
import Profile from './Profile';
import Orders from './Orders';
import RestaurantPage from './RestaurantPage';
import Delivery from './Delivery';
import RestaurantWorkspace from './RestaurantWorkspace';
import { diagnostic, errorCode } from './diagnostics';

export default function App() {
  const [adminSection,setAdminSection]=useHistoryState<string>('admin','overview');
  const [,setDeliveryTab]=useHistoryState<string>('deliveries','available');
  const [lang, setLang] = useState<'ar' | 'en'>('ar');
  const t = (ar: string, en: string) => lang === 'ar' ? ar : en;
  const money = (n: number) => new Intl.NumberFormat(lang === 'ar' ? 'ar-EG' : 'en-EG', { style: 'currency', currency: 'EGP', maximumFractionDigits: 0 }).format(n);
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [restaurantId, setRestaurantId] = useHistoryState<string>('restaurant', '');
  const selected = restaurants.find(r => r.id === restaurantId) || null;
  const setSelected = (restaurant: Restaurant | null) => setRestaurantId(restaurant?.id || '');
  const [cart, setCart] = useState<Line[]>([]);
  const [guestOrder, setGuestOrder] = useState<Order | null>(null);
  const [view, setView] = useHistoryState<'browse' | 'cart' | 'track' | 'admin' | 'profile' | 'orders' | 'restaurant' | 'delivery' | 'workspace' | 'restaurantOrders'>('page', 'browse');
  useEffect(()=>{window.scrollTo(0,0);},[view]);
  const [login, setLogin] = useState(false);
  const [register, setRegister] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [accountError, setAccountError] = useState('');
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState('');
  const submitting = useRef(false);
  useEffect(() => {
    if (!login) return;
    const previous = document.activeElement as HTMLElement | null;
    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    const dialog = document.querySelector<HTMLElement>('[role="dialog"]');
    const focusable = () => Array.from(dialog?.querySelectorAll<HTMLElement>('button:not(:disabled), input:not(:disabled), textarea:not(:disabled), select:not(:disabled), a[href]') || []);
    focusable()[0]?.focus();
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') { setLogin(false); setSelected(null); }
      if (event.key !== 'Tab') return;
      const items = focusable(), first = items[0], last = items[items.length - 1];
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last?.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first?.focus(); }
    };
    document.addEventListener('keydown', handleKey);
    return () => { document.body.style.overflow = originalOverflow; document.removeEventListener('keydown', handleKey); previous?.focus(); };
  }, [login]);
  useEffect(() => { document.documentElement.lang = lang; document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr'; }, [lang]);
  async function loadRestaurants() {
    setLoading(true); setError('');
    try { setRestaurants(await api<Restaurant[]>('/restaurants')); }
    catch (e) { setError(message(e)); } finally { setLoading(false); }
  }
  useEffect(() => { void loadRestaurants(); }, []);
  useAutoRefresh(async () => { setRestaurants(await api<Restaurant[]>('/restaurants')); });
  useEffect(() => {
    if (!isAdministrator(account?.role)) return;
    if (view === 'workspace' || view === 'restaurantOrders' || view === 'delivery') {
      setAdminSection(view === 'workspace' ? 'restaurants' : 'orders');
      setView('admin');
    }
  }, [account?.role, view]);
  async function syncAccount(initial = false) {
    setAccountError('');
    diagnostic('account.sync.started');
    const uid = auth?.currentUser?.uid;
    try { const result = await api<Account>('/account/me', {}, true); if (uid === auth?.currentUser?.uid) { setAccount(result); setView(current => initial && isAdministrator(result.role) && current === 'browse' ? 'admin' : current === 'admin' && !isAdministrator(result.role) || (current === 'workspace' || current === 'restaurantOrders') && !canManageRestaurants(result.role) || current === 'delivery' && !canUseDelivery(result.role) ? 'browse' : current); diagnostic('account.sync.succeeded'); } }
    catch (e) { if (uid === auth?.currentUser?.uid) { setAccountError(message(e)); diagnostic('account.sync.failed', { code: errorCode(e) }); } }
  }
  useEffect(() => {
    if (!auth) return;
    let hadUser = false;
    return onAuthStateChanged(auth, next => {
      diagnostic(next ? 'auth.state.signed-in' : 'auth.state.signed-out');
      setUser(next); setAccount(null); setAccountError('');
      if (next) { hadUser = true; setLogin(false); void syncAccount(true); }
      else { if (hadUser) { setView('browse'); setSelected(null); } hadUser = false; setCart([]); }
    }, e => { diagnostic('auth.state.failed', { code: errorCode(e) }); setAccountError(message(e)); });
  }, []);
  useEffect(() => {
    if (!user) return;
    const refresh = () => { void syncAccount(); };
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [user?.uid]);
  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(''), 3500);
    return () => clearTimeout(timer);
  }, [notice]);
  async function action(task: () => Promise<void>) {
    setBusy(true); setError('');
    try { await task(); } catch (e) { diagnostic('ui.action.failed', { code: errorCode(e) }); setError(message(e)); } finally { setBusy(false); }
  }
  async function googleSignIn() {
    diagnostic('auth.google.started');
    try {
      if (!auth) throw new Error('Firebase is not configured / إعداد Firebase غير مكتمل');
      await signInWithPopup(auth, new GoogleAuthProvider());
      diagnostic('auth.google.succeeded');
    } catch (e) {
      diagnostic('auth.google.failed', { code: errorCode(e) });
      if (errorCode(e) === 'auth/internal-error') throw new Error(t(
        'تعذر إتمام تسجيل الدخول باستخدام Google (auth/internal-error). جرّب Chrome أو Edge، وراجع إعدادات Google في Firebase. تفاصيل المرحلة في سجل المتصفح.',
        'Google sign-in could not complete (auth/internal-error). Try Chrome or Edge and check the Google provider in Firebase. See the browser console for the failing stage.'
      ));
      throw e;
    }
  }
  async function authenticate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); const data = new FormData(event.currentTarget);
    await action(async () => {
      if (!auth) throw new Error(t('أضف إعدادات Firebase أولاً. راجع ملف README داخل web.', 'Add Firebase settings first. See web/README.md.'));
      const email = String(data.get('email')).trim(), password = String(data.get('password'));
      diagnostic('auth.email.started');
      try {
        if (register) await createUserWithEmailAndPassword(auth, email, password);
        else await signInWithEmailAndPassword(auth, email, password);
        diagnostic('auth.email.succeeded');
      } catch (e) { diagnostic('auth.email.failed', { code: errorCode(e) }); throw e; }
    });
  }
  function add(restaurant: Restaurant, item: Restaurant['menu'][number]) {
    setCart(current => {
      const found = current.find(line => line.restaurantId === restaurant.id && line.menuItemId === item.id);
      return found ? current.map(line => line === found ? { ...line, quantity: line.quantity + 1 } : line) : [...current, { restaurantId: restaurant.id, restaurantName: restaurant.name, menuItemId: item.id, name: item.name, price: item.price, quantity: 1 }];
    });
    setNotice(t('تمت الإضافة إلى السلة', 'Added to your basket'));
  }
  const [deliveryAreas, setDeliveryAreas] = useState<{ name: string; fee: number }[]>([]);
  const [deliveryArea, setDeliveryArea] = useState('');
  const [deliveryAreasFailed, setDeliveryAreasFailed] = useState(false);

  async function loadDeliveryAreas() {
    setDeliveryAreasFailed(false);
    try {
      setDeliveryAreas(await api<{ name: string; fee: number }[]>('/delivery-areas', { cache: 'no-store' }));
    } catch {
      setDeliveryAreasFailed(true);
    }
  }

  useEffect(() => {
    if (view === 'cart') void loadDeliveryAreas();
  }, [view]);

  const deliveryFee = deliveryAreas.find(area => area.name === deliveryArea)?.fee;
  const subtotal = cart.reduce((sum, line) => sum + line.price * line.quantity, 0);
  const count = cart.reduce((sum, line) => sum + line.quantity, 0);
  async function checkout(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); if (submitting.current || !cart.length) return;
    const data = new FormData(event.currentTarget);
    submitting.current = true;
    await action(async () => {
      const order = await api<Order>('/orders', { method: 'POST', body: JSON.stringify({ customerPhone: String(data.get('phone')).trim(), customerAddress: String(data.get('address')).trim(), area: String(data.get('area')).trim(), items: cart.map(({ restaurantId, menuItemId, quantity }) => ({ restaurantId, menuItemId, quantity })) }) }, !!user);
      setCart([]); setDeliveryArea(''); setGuestOrder(user ? null : order); setView(user ? 'orders' : 'track'); setNotice(t('تم تأكيد طلبك بنجاح', 'Your order is confirmed'));
    });
    submitting.current = false;
  }
  const visible = restaurants.filter(r => (!category || r.category === category) && `${r.name} ${r.category} ${r.menu.map(m => m.name).join(' ')}`.toLowerCase().includes(search.toLowerCase()));
  return <>
    <LaunchCountdown t={t}/>
    <div className="topline">{t('من مطاعم مدينتك، لحد باب بيتك', 'From your local favourites, straight to your door')} <span>القناطر الخيرية · Al Qanater El Khayria</span></div>
    <header className="header"><button className="brand" onClick={() => { setView('browse'); setSelected(null); }}><BrandMark/><span>{t('كابيندا', 'Kapinda')}<small>{t('طازة ومحلي', 'FRESH & LOCAL')}</small></span></button>
      <nav className={account && account.role!=='CUSTOMER'?'staff-navigation':''} aria-label={t('القائمة الرئيسية','Main navigation')}><button className={view === 'browse' ? 'active' : ''} onClick={() => setView('browse')}>{t('المطاعم','Restaurants')}</button>{account?.role!=='RESTAURANT' && !isAdministrator(account?.role) && <button className={view === 'orders' || view === 'track' ? 'active' : ''} onClick={() => { if (user) setView(account?.role==='RESTAURANT'?'restaurantOrders':'orders'); else { setError(''); setLogin(true); } }}>{t('تابع طلباتك','Follow your orders')}</button>}{account?.role==='DELIVERY' && <button onClick={() => setView('delivery')}>{t('طلبات التوصيل', 'Delivery desk')}</button>}{account?.role==='RESTAURANT' && <button onClick={()=>setView('restaurantOrders')}>{t('طلبات المطعم','Restaurant orders')}</button>}{account?.role==='RESTAURANT' && <button onClick={() => setView('workspace')}>{isAdministrator(account?.role) ? t('إدارة المطاعم', 'Restaurant workspace') : t('إدارة مطاعمي', 'My restaurants')}</button>}{isAdministrator(account?.role) && <button onClick={() => setView('admin')}>{t('الإدارة','Admin')}</button>}</nav>
      <div className="header-actions"><div className="header-location"><Icon name="pin"/><span>{t('القناطر الخيرية','Al Qanater')}</span></div>{user && account && <NotificationBell key={user.uid+account.role+(account.restaurantIds||[]).join(',')} t={t} onOpen={(target,status)=>{setView(target);if(target==='admin')setAdminSection('orders');if(target==='delivery')setDeliveryTab(status==='READY'?'available':'mine');}} />} <button onClick={() => setLang(lang === 'ar' ? 'en' : 'ar')}>{lang === 'ar' ? 'English' : 'العربية'}</button>{user ? <button className="profile-trigger" aria-label={t('فتح حسابي', 'Open my account')} onClick={() => setView('profile')}><Avatar key={user.uid} uid={user.uid}/><span>{account?.displayName?.split(' ')[0] || user.displayName?.split(' ')[0] || t('حسابي', 'My account')}<small>{t('حسابي', 'My account')}</small></span></button> : <button onClick={() => { setError(''); setLogin(true); }}>{t('دخول','Sign in')}</button>}<button aria-label={t('فتح السلة','Open basket')} className="primary basket-button" onClick={() => setView('cart')}><Icon name="bag"/><span className="basket-label">{t('السلة','Basket')}</span> <b>{count}</b></button></div>
    </header>
    {isAdministrator(account?.role) && <div className="mobile-admin-context"><span><Icon name="settings" size={17}/>{account?.role==='SUB_ADMIN'?t('مساحة المدير المساعد','Sub Admin workspace'):t('مساحة الإدارة','Admin workspace')}</span><button onClick={()=>{if(view==='admin')setView('browse');else{setAdminSection('overview');setView('admin');}}}>{view==='admin'?t('تصفح كعميل','Customer view'):t('فتح الإدارة','Open dashboard')}</button></div>}
    <main>
      {error && <div className="alert" role="alert">{error}<button aria-label={t('إغلاق','Dismiss')} onClick={() => setError('')}>×</button></div>}
      {accountError && <div className="alert" role="alert">{t('تعذر تحميل حسابك.','Could not load your account.')} {accountError} <button onClick={() => void syncAccount()}>{t('إعادة المحاولة','Retry')}</button></div>}
      {view === 'browse' && <HomeBrowse restaurants={restaurants} loading={loading} t={t} money={money} search={search} setSearch={setSearch} category={category} setCategory={setCategory} onOpen={r=>{setSelected(r);setView('restaurant');}} onRetry={()=>void loadRestaurants()}/>}
      {view === 'cart' && <section className="page"><span className="eyebrow">{t('باقي خطوة واحدة','ALMOST AT YOUR DOOR')}</span><h1>{t('سلة طلباتك','Your basket')}</h1>{!cart.length ? <div className="empty"><h2>{t('السلة لسه فاضية','Your basket is empty')}</h2><button className="primary" onClick={() => setView('browse')}>{t('اكتشف المطاعم','Explore restaurants')}</button></div> : <div className="checkout-grid"><div className="panel">{cart.map((line, index) => <div className="cart-line" key={`${line.restaurantId}-${line.menuItemId}`}><div><h3>{line.name}</h3><small>{line.restaurantName}</small><p>{money(line.price * line.quantity)}</p></div><div className="stepper"><button aria-label={t('تقليل الكمية','Decrease quantity')} onClick={() => setCart(cart.flatMap((l, i) => i !== index ? [l] : l.quantity > 1 ? [{ ...l, quantity: l.quantity - 1 }] : []))}>−</button><span>{line.quantity}</span><button aria-label={t('زيادة الكمية','Increase quantity')} onClick={() => setCart(cart.map((l, i) => i === index ? { ...l, quantity: l.quantity + 1 } : l))}>+</button></div></div>)}<div className="total-row"><span>{t('المجموع الفرعي','Subtotal')}</span><strong>{money(subtotal)}</strong></div>{deliveryFee === undefined ? <p className="muted">{t('اختر المنطقة لحساب رسوم التوصيل.', 'Select an area to calculate delivery.')}</p> : <><div className="total-row"><span>{t('رسوم التوصيل', 'Delivery fee')}</span><strong>{money(deliveryFee)}</strong></div><div className="total-row"><span>{t('الإجمالي', 'Total')}</span><strong>{money(subtotal + deliveryFee)}</strong></div></>}</div><form className="panel form" onSubmit={checkout}><h2>{t('بيانات التوصيل','Delivery details')}</h2><label>{t('رقم الموبايل','Phone number')}<input name="phone" type="tel" defaultValue={account?.phoneNumber || user?.phoneNumber || ''} required minLength={10} maxLength={20} pattern="[+0-9 ()-]{10,20}" autoComplete="tel"/></label><label>{t('المنطقة','Area')}<select name="area" required value={deliveryArea} disabled={!deliveryAreas.length || deliveryAreasFailed} onChange={event => setDeliveryArea(event.target.value)}><option value="">{deliveryAreasFailed ? t('تعذر تحميل المناطق', 'Could not load areas') : deliveryAreas.length ? t('اختر المنطقة', 'Select your area') : t('جاري تحميل المناطق…', 'Loading areas…')}</option>{deliveryAreas.map(area => <option key={area.name} value={area.name}>{area.name}: {money(area.fee)}</option>)}</select></label>{deliveryAreasFailed && <button type="button" onClick={() => void loadDeliveryAreas()}>{t('أعد المحاولة', 'Retry')}</button>}<label>{t('العنوان بالتفصيل','Full address')}<textarea name="address" required minLength={8} autoComplete="street-address" placeholder={t('الشارع، العمارة، الدور، وعلامة مميزة','Street, building, floor, and a nearby landmark')}/></label><div className="payment">◉ {t('الدفع نقداً عند الاستلام','Cash on delivery')}</div>{<button className="primary" disabled={busy || deliveryFee === undefined || (!!user && !account)}>{busy ? t('جاري إرسال الطلب…','Placing order…') : t('تأكيد الطلب','Place order')}</button>}</form></div>}</section>}
      {view === 'track' && !user && guestOrder && <section className="page"><div className="panel"><h1>{t('تم تأكيد طلبك', 'Your order is confirmed')}</h1><p>{t('رقم الطلب', 'Order number')}: <strong>#{guestOrder.id}</strong></p><p>{t('الإجمالي', 'Total')}: {money(guestOrder.total)}</p><p>{t('الدفع نقداً عند الاستلام. احتفظ برقم الطلب للمتابعة.', 'Cash on delivery. Keep your order number for reference.')}</p><button className="primary" onClick={() => setView('browse')}>{t('اكتشف المطاعم', 'Explore restaurants')}</button></div></section>}
      {view === 'restaurant' && selected && <RestaurantPage restaurant={selected} cart={cart} t={t} money={money} onBack={() => { setSelected(null); setView('browse'); }} onAdd={item => add(selected, item)} onQuantity={(itemId, delta) => setCart(current => current.flatMap(line => line.restaurantId === selected.id && line.menuItemId === itemId ? line.quantity + delta > 0 ? [{ ...line, quantity: line.quantity + delta }] : [] : [line]))} onCheckout={() => setView('cart')}/>}
      {view === 'delivery' && user && canUseDelivery(account?.role) && <Delivery supervisor={isAdministrator(account?.role)} key={user.uid} t={t} money={money}/>}
      {view === 'workspace' && user && canManageRestaurants(account?.role) && <RestaurantWorkspace supervisor={isAdministrator(account?.role)} key={user.uid} t={t} money={money} onChanged={() => void loadRestaurants()}/>}
      {view === 'profile' && user && <Profile key={user.uid} user={user} account={account} t={t} money={money} onSaved={setAccount} onOrders={() => setView('orders')} onBrowse={() => setView('browse')} onOpen={() => setView('orders')} onLogout={() => void action(async () => { await signOut(auth!); })}/>}
      {view === 'restaurantOrders' && user && canManageRestaurants(account?.role) && <RestaurantOrders key={user.uid+(account?.restaurantIds||[]).join(',')} t={t} money={money}/>}
      {(view === 'orders' || view === 'track') && user && account && <Orders accountUid={user.uid} key={user.uid} t={t} money={money} onBrowse={() => setView('browse')} onOpen={() => setView('orders')}/>}
      {view === 'admin' && account && isAdministrator(account.role) && <Admin key={user?.uid} t={t} money={money} actorUid={user?.uid} actorRole={account.role} onChange={() => void loadRestaurants()}/>}
    </main>
    {count>0 && view!=='cart' && <button className="fresh-floating-basket" onClick={()=>setView('cart')}><Icon name="bag"/><strong>{count} {t('أصناف','items')} · {money(subtotal)}</strong><span>{t('عرض السلة','View basket')} <Icon name="arrow" size={19}/></span></button>}
    {isAdministrator(account?.role) ? <nav className="mobile-bottom-nav admin-mobile-nav" aria-label={t('تنقل الإدارة','Administration navigation')}>{([
      ['overview','home',t('الإدارة','Dashboard')],['orders','orders',t('الطلبات','Orders')],['restaurants','bag',t('المطاعم','Restaurants')],['users','user',t('المستخدمون','Users')],['delivery-areas','settings',t('مناطق التوصيل','Areas')],['settings','settings',t('الإعدادات','Settings')]
    ] as const).map(([section,icon,label])=><button key={section} className={view==='admin'&&(adminSection===section||(section==='restaurants'&&['menu','restaurant-form'].includes(adminSection)))?'active':''} aria-current={view==='admin'&&adminSection===section?'page':undefined} onClick={()=>{setAdminSection(section);setView('admin');}}><Icon name={icon}/><span>{label}</span></button>)}</nav> : <><nav className="mobile-bottom-nav" aria-label={t('التنقل السريع','Quick navigation')}><button className={view==='browse'?'active':''} onClick={()=>setView('browse')}><Icon name="home"/><span>{t('الرئيسية','Home')}</span></button><button onClick={()=>{setView('browse');setTimeout(()=>{document.getElementById('mobile-food-search')?.focus();},100);}}><Icon name="search"/><span>{t('بحث','Search')}</span></button><button className={['orders','restaurantOrders','delivery','admin'].includes(view)?'active':''} onClick={()=>{if(!user){setLogin(true);return;}if(isAdministrator(account?.role)){setAdminSection('orders');setView('admin');}else setView(account?.role==='RESTAURANT'?'restaurantOrders':account?.role==='DELIVERY'?'delivery':'orders');}}><Icon name="orders"/><span>{t('الطلبات','Orders')}</span></button><button className={view==='profile'?'active':''} onClick={()=>user?setView('profile'):setLogin(true)}><Icon name="user"/><span>{t('حسابي','Profile')}</span></button></nav></>}
    <footer><div className="brand">{t('كابيندا','Kapinda')} <span>♡</span></div><p>{t('طعم محلي، وناس من عندنا.','Local flavours. Familiar faces.')}</p><small>القناطر الخيرية · Al Qanater El Khayria</small><p><a className="footer-facebook" href="https://web.facebook.com/profile.php?id=61595233080545" target="_blank" rel="noopener noreferrer" aria-label={t('كابيندا على فيسبوك', 'Kapinda on Facebook')}><svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M24 12a12 12 0 1 0-13.875 11.854v-8.385H7.078V12h3.047V9.356c0-3.008 1.792-4.67 4.533-4.67 1.313 0 2.686.234 2.686.234v2.953h-1.513c-1.49 0-1.956.925-1.956 1.874V12h3.328l-.532 3.469h-2.796v8.385A12 12 0 0 0 24 12Z"/></svg></a></p></footer>
    {notice && <div className="toast" role="status">✓ {notice}</div>}
    {login && <div className="overlay" onClick={() => setLogin(false)}><section className="modal login-modal" role="dialog" aria-modal="true" aria-label={t('تسجيل الدخول','Sign in')} onClick={e => e.stopPropagation()} onKeyDown={e => { if (e.key === 'Escape') setLogin(false); }}><button className="close" aria-label={t('إغلاق','Close')} onClick={() => setLogin(false)}>×</button><span className="eyebrow">{t('أهلاً بيك','WELCOME TO THE NEIGHBOURHOOD')}</span><h2>{register ? t('حساب جديد','Create account') : t('نورت كابيندا','Welcome back')}</h2>{!auth && <p className="alert">{t('تسجيل الدخول يحتاج إعداد Firebase. راجع web/README.md.','Sign-in needs Firebase configuration. See web/README.md.')}</p>}{error && <p className="alert" role="alert">{error}</p>}<form className="form" onSubmit={authenticate}><label>{t('البريد الإلكتروني','Email')}<input autoFocus name="email" type="email" required autoComplete="email"/></label><label>{t('كلمة المرور','Password')}<input name="password" type="password" required minLength={6} autoComplete={register ? 'new-password' : 'current-password'}/></label><button className="primary" disabled={busy || !auth}>{register ? t('إنشاء حساب','Create account') : t('تسجيل الدخول','Sign in')}</button></form><button className="google full" disabled={busy || !auth} onClick={() => void action(googleSignIn)}>{t('المتابعة باستخدام Google','Continue with Google')}</button><button className="text-button full" onClick={() => { setRegister(!register); setError(''); }}>{register ? t('عندك حساب؟ سجل دخول','Have an account? Sign in') : t('جديد هنا؟ أنشئ حساب','New here? Create an account')}</button></section></div>}
  </>;
}
export function message(error: unknown) { return error instanceof Error ? error.message : 'Something went wrong / حدث خطأ'; }
