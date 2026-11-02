import { getApps, initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { diagnostic, errorCode } from './diagnostics';
const env = import.meta.env;
const config = { apiKey: env.VITE_FIREBASE_API_KEY, authDomain: env.VITE_FIREBASE_AUTH_DOMAIN, projectId: env.VITE_FIREBASE_PROJECT_ID, appId: env.VITE_FIREBASE_APP_ID };
export const auth = Object.values(config).every(Boolean) ? getAuth(getApps()[0] ?? initializeApp({ ...config, messagingSenderId: env.VITE_FIREBASE_MESSAGING_SENDER_ID })) : null;
diagnostic(auth ? 'auth.config.ready' : 'auth.config.missing');
export async function api<T>(path: string, init: RequestInit = {}, protectedRequest = false): Promise<T> {
  const started = performance.now();
  const request = { method: (init.method || 'GET').toUpperCase(), path: path.split('?')[0] };
  diagnostic('api.started', request);
  const headers = new Headers(init.headers);
  if (init.body) headers.set('Content-Type', 'application/json');
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  let status: number | undefined;
  try {
    const principal = auth?.currentUser;
    if (protectedRequest) {
      if (!principal) throw new Error('Please sign in / يرجى تسجيل الدخول');
      headers.set('Authorization', `Bearer ${await principal.getIdToken()}`);
      if (auth?.currentUser?.uid !== principal.uid) throw new Error('Account changed. Please try again. / تم تغيير الحساب. حاول مرة أخرى.');
    }
    if (controller.signal.aborted) throw new DOMException('Timed out', 'AbortError');
    const response = await fetch(`${(env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')}${path}`, { ...init, cache: protectedRequest ? 'no-store' : init.cache, headers, signal: controller.signal });
    status = response.status;
    if (!response.ok) {
      const body = await response.json().catch(() => null);
      throw new Error(body?.message || body?.error || (response.status >= 500 ? 'الخدمة غير متاحة حالياً. حاول مرة أخرى. / Service unavailable. Please try again.' : `Request failed (${response.status}) / تعذر إتمام الطلب`));
    }
    const result = response.status === 204 ? undefined as T : await response.json();
    if (protectedRequest && auth?.currentUser?.uid !== principal?.uid) throw new Error('Account changed. Please try again. / تم تغيير الحساب. حاول مرة أخرى.');
    diagnostic('api.succeeded', { ...request, status, elapsedMs: performance.now() - started });
    return result;
  } catch (error) {
    diagnostic('api.failed', { ...request, status, code: errorCode(error), elapsedMs: performance.now() - started });
    if (error instanceof DOMException && error.name === 'AbortError') throw new Error('Connection timed out / انتهت مهلة الاتصال');
    throw error;
  } finally { clearTimeout(timeout); }
}
