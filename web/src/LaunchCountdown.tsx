import { useEffect, useState } from 'react';
import type { Translate } from './Orders';

// Saturday 7 November, 4 pm Egypt time (UTC+02:00).
const launchAt = Date.parse('2026-11-07T16:00:00+02:00');

export default function LaunchCountdown({ t }: { t: Translate }) {
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);
  const seconds = Math.max(0, Math.floor((launchAt - now) / 1000));
  const units = [Math.floor(seconds / 86400), Math.floor(seconds / 3600) % 24, Math.floor(seconds / 60) % 60, seconds % 60];
  return <>
    {seconds > 0 && <aside className="launch-banner" aria-label={t('موعد انطلاق كابيندا', 'Kapinda launch countdown')}>
      <div><strong>{t('القناطر، مستعدين؟ كابيندا جايّة!', 'Al Qanater, ready? Kapinda is coming!')}</strong><p>{t('السبت ٧ نوفمبر · الساعة ٤ مساءً بتوقيت مصر', 'Saturday 7 November · 4 pm Egypt time')}</p></div>
      <div className="launch-clock" role="timer" aria-live="off">{units.map((value, index) => <div key={index}><b>{String(value).padStart(2, '0')}</b><span>{[t('يوم','Days'), t('ساعة','Hours'), t('دقيقة','Minutes'), t('ثانية','Seconds')][index]}</span></div>)}</div>
    </aside>}
    <div className="demo-notice" role="note"><strong>{t('نسخة تجريبية', 'Demo version')}</strong><span>{t('البيانات المعروضة تجريبية وغير حقيقية. أسماء الأصناف والصور والأسعار للتوضيح فقط ولا تمثل القائمة النهائية.', 'The displayed data is sample data, not real data. Dish names, images and prices are for demonstration only and do not represent the final menu.')}</span></div>
  </>;
}
