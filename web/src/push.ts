import { getMessaging, getToken, deleteToken, isSupported, onMessage } from 'firebase/messaging';
import { api, auth } from './api';

const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY;
const tokenKey = 'kapinda.push.token';
export async function pushAvailable() {
  return !!auth && !!vapidKey && !!import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID && window.isSecureContext && await isSupported();
}
export async function enablePush(requestPermission = true) {
  const permissionRequest = requestPermission && 'Notification' in window ? Notification.requestPermission() : Promise.resolve('Notification' in window ? Notification.permission : 'denied');
  if (!await pushAvailable()) throw new Error('Push unavailable');
  // Called directly by the Enable button so Safari sees a user gesture.
  const permission = await permissionRequest;
  if (permission !== 'granted') throw new Error('Permission required');
  const uid = auth!.currentUser?.uid;
  if (!uid) throw new Error('Sign in first');
  const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
  await navigator.serviceWorker.ready;
  const token = await getToken(getMessaging(auth!.app), { vapidKey, serviceWorkerRegistration: registration });
  if (auth!.currentUser?.uid !== uid) throw new Error('Account changed');
  await api('/notifications/devices', { method: 'PUT', body: JSON.stringify({ token }) }, true);
  localStorage.setItem(tokenKey, token);
}
export async function disablePush() {
  const token = localStorage.getItem(tokenKey);
  if (!token) return;
  if (token) await api('/notifications/devices', { method: 'DELETE', body: JSON.stringify({ token }) }, true);
  if (auth && await isSupported()) await deleteToken(getMessaging(auth.app));
  localStorage.removeItem(tokenKey);
}
export function pushEnabled() { return !!localStorage.getItem(tokenKey); }
export async function watchPush(refresh: () => void) {
  if (!await pushAvailable()) return () => {};
  return onMessage(getMessaging(auth!.app), () => refresh());
}
