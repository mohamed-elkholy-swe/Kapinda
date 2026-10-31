import { useEffect, useRef, useState } from 'react';
import type { Translate } from './Orders';

export default function PhotoField({ t, initialUrl }: { t: Translate; initialUrl?: string | null }) {
  const [url, setUrl] = useState(initialUrl || '');
  const [failed, setFailed] = useState(false);
  useEffect(() => { setUrl(initialUrl || ''); setFailed(false); }, [initialUrl]);
  const input = useRef<HTMLInputElement>(null);
  useEffect(() => {
    const form = input.current?.form;
    const reset = () => { setUrl(initialUrl || ''); setFailed(false); };
    form?.addEventListener('reset', reset);
    return () => form?.removeEventListener('reset', reset);
  }, [initialUrl]);
  return <div className="photo-field">
    <label>{t('رابط الصورة', 'Photo link')}
      <input ref={input} name="photoUrl" type="url" pattern="https://.*" maxLength={2000} dir="ltr" placeholder="https://…" value={url} onChange={e => { setUrl(e.target.value); setFailed(false); }}/>
    </label>
    <small className="muted">{t('أضف رابط صورة HTTPS. اترك الحقل فارغًا لإزالة الصورة.', 'Add an HTTPS image link. Leave empty to remove the photo.')}</small>
    {url.startsWith('https://') && !failed && <img className="photo-field-preview" src={url} alt={t('معاينة الصورة', 'Photo preview')} referrerPolicy="no-referrer" onError={() => setFailed(true)}/>}
    {failed && <p className="muted" role="status">{t('تعذر عرض الصورة. تحقق من الرابط.', 'Could not preview this photo. Check the link.')}</p>}
  </div>;
}
