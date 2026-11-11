import type { Translate } from './Orders';

const messages: Record<string, [string, string]> = {
  'invalid-email': ['البريد الإلكتروني غير صحيح. تأكد من كتابته بشكل صحيح.', 'Enter a valid email address.'],
  'missing-email': ['اكتب بريدك الإلكتروني للمتابعة.', 'Enter your email address to continue.'],
  'missing-password': ['اكتب كلمة المرور للمتابعة.', 'Enter your password to continue.'],
  'invalid-credential': ['البريد الإلكتروني أو كلمة المرور غير صحيحة. راجع البيانات وحاول مرة أخرى.', 'The email or password is incorrect. Check your details and try again.'],
  'email-already-in-use': ['البريد الإلكتروني ده مسجل بالفعل. سجّل الدخول بحسابك أو استخدم بريدًا آخر.', 'This email is already registered. Sign in to your account or use another email.'],
  'weak-password': ['كلمة المرور ضعيفة. استخدم 6 أحرف على الأقل واختر كلمة مرور أقوى.', 'Your password is too weak. Use at least 6 characters and choose a stronger password.'],
  'password-does-not-meet-requirements': ['كلمة المرور لا تستوفي متطلبات الأمان. جرّب كلمة أطول تحتوي على حروف كبيرة وصغيرة وأرقام ورموز.', 'Your password does not meet the security requirements. Try a longer password with uppercase and lowercase letters, numbers, and symbols.'],
  'user-disabled': ['تم تعطيل هذا الحساب. تواصل مع الدعم للمساعدة.', 'This account has been disabled. Contact support for help.'],
  'too-many-requests': ['حاولت مرات كثيرة في وقت قصير. انتظر قليلًا ثم حاول مرة أخرى.', 'There have been too many attempts. Please wait a while and try again.'],
  'network-request-failed': ['تعذر الاتصال. تأكد من اتصالك بالإنترنت وحاول مرة أخرى.', 'Could not connect. Check your internet connection and try again.'],
  'timeout': ['استغرقت المحاولة وقتًا طويلًا. تحقق من اتصالك وحاول مرة أخرى.', 'The request timed out. Check your connection and try again.'],
  'popup-blocked': ['المتصفح منع نافذة تسجيل الدخول. اسمح بالنوافذ المنبثقة لهذا الموقع وحاول مرة أخرى.', 'Your browser blocked the sign-in window. Allow popups for this site and try again.'],
  'popup-closed-by-user': ['تم إغلاق نافذة تسجيل الدخول قبل إكمال العملية. حاول مرة أخرى وأكمل تسجيل الدخول.', 'The sign-in window was closed before finishing. Try again and complete sign-in.'],
  'cancelled-popup-request': ['توجد محاولة تسجيل دخول أخرى. أكملها أو أغلقها ثم حاول مرة أخرى.', 'Another sign-in attempt is open. Complete or close it before trying again.'],
  'account-exists-with-different-credential': ['هذا البريد مرتبط بطريقة تسجيل دخول أخرى. استخدم الطريقة التي أنشأت بها حسابك.', 'This email uses a different sign-in method. Use the method you originally used to create your account.'],
  'credential-already-in-use': ['بيانات تسجيل الدخول دي مرتبطة بحساب آخر. استخدم طريقة تسجيل الدخول الخاصة بحسابك.', 'These sign-in details are linked to another account. Use your account’s original sign-in method.'],
};

export function authErrorMessage(error: unknown, t: Translate): string {
  const code = typeof error === 'object' && error !== null && 'code' in error && typeof error.code === 'string'
    ? error.code.replace(/^auth\//, '') : '';
  // Keep legacy credential errors equally general to avoid exposing account existence.
  const key = code === 'user-not-found' || code === 'wrong-password' ? 'invalid-credential' : code;
  const pair = messages[key] ?? ['تعذر إتمام العملية حاليًا. حاول مرة أخرى بعد قليل. لو المشكلة مستمرة، تواصل مع الدعم.', 'We could not complete your request right now. Please try again shortly. If the problem continues, contact support.'];
  return t(...pair);
}
