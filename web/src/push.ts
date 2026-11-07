import { api, auth, firebaseApp } from './api';
import { pushChangeEvent, rememberPushPreference } from './pushPreference';

const storageKey = 'kapinda.push';
type Registration = { uid: string; token: string };
function saved(): Registration | null {
  try { return JSON.parse(localStorage.getItem(storageKey) || 'null'); } catch { return null; }
}
let pending: Promise<unknown> = Promise.resolve();
function serialize<T>(task: () => Promise<T>): Promise<T> {
  const next = pending.then(task, task); pending = next.catch(() => {}); return next;
}
export function pushEnabled(uid: string) { return 'Notification' in window && saved()?.uid === uid && Notification.permission === 'granted'; }
export async function pushSupported() {
  if (!window.isSecureContext || !('Notification' in window) || !('serviceWorker' in navigator)) return false;
  return (await import('firebase/messaging')).isSupported();
}
export function enablePush(uid: string, ask = true) {
  // Invoke the permission prompt directly from the user's click, especially on iOS.
  const permission = ask ? Notification.requestPermission() : Promise.resolve(Notification.permission);
  return serialize(async () => {
    if (await permission !== 'granted') throw new Error('اسمح بالإشعارات من إعدادات المتصفح · Allow notifications in browser settings.');
    if (!firebaseApp || !import.meta.env.VITE_FIREBASE_VAPID_KEY || !import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID)
      throw new Error('إعداد الإشعارات غير مكتمل · Notification configuration is missing.');
    if (auth?.currentUser?.uid !== uid) return;
    const { getMessaging, getToken } = await import('firebase/messaging');
    const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
    await navigator.serviceWorker.ready;
    const token = await getToken(getMessaging(firebaseApp), { vapidKey: import.meta.env.VITE_FIREBASE_VAPID_KEY, serviceWorkerRegistration: registration });
    if (!token) throw new Error('تعذر تسجيل الجهاز · Could not register this device.');
    if (auth?.currentUser?.uid !== uid) return;
    const previous = saved();
    if (previous?.uid === uid && previous.token !== token)
      await api('/notifications/device', { method: 'DELETE', body: JSON.stringify({ token: previous.token }) }, true);
    await api('/notifications/device', { method: 'PUT', body: JSON.stringify({ token }) }, true);
    localStorage.setItem(storageKey, JSON.stringify({ uid, token }));
    rememberPushPreference(uid, 'enabled');
  });
}
export function disablePush(remember = true) {
  return serialize(async () => {
    const previous = saved();
    if (previous && auth?.currentUser?.uid === previous.uid)
      await api('/notifications/device', { method: 'DELETE', body: JSON.stringify({ token: previous.token }) }, true);
    localStorage.removeItem(storageKey);
    if (remember && auth?.currentUser) rememberPushPreference(auth.currentUser.uid, 'dismissed');
    window.dispatchEvent(new Event(pushChangeEvent));
    if (firebaseApp && await pushSupported()) {
      const { getMessaging, deleteToken } = await import('firebase/messaging');
      await deleteToken(getMessaging(firebaseApp));
    }
  });
}
