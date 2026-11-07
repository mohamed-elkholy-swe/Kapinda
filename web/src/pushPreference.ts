export type PushPreference = 'enabled' | 'dismissed';
export const pushChangeEvent = 'kapinda:push-change';
const choices = new Map<string, PushPreference>();
const key = (uid: string) => `kapinda.push.preference.${uid}`;

export function pushPreference(uid: string): PushPreference | null {
  try {
    const value = localStorage.getItem(key(uid));
    if (value === 'enabled' || value === 'dismissed') return value;
  } catch { /* Keep the choice for this visit if browser storage is unavailable. */ }
  return choices.get(uid) || null;
}
export function rememberPushPreference(uid: string, value: PushPreference) {
  choices.set(uid, value);
  try { localStorage.setItem(key(uid), value); } catch { /* Session memory is sufficient. */ }
  window.dispatchEvent(new Event(pushChangeEvent));
}
export function shouldPromptForPush(supported: boolean, permission: NotificationPermission, enabled: boolean, preference: PushPreference | null) {
  return supported && permission !== 'denied' && !enabled && preference === null;
}
