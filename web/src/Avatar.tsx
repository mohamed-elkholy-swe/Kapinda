import { useState } from 'react';

export default function Avatar({ uid, large = false }: { uid: string; large?: boolean }) {
  const [seed] = useState(() => {
    const generated = crypto.getRandomValues(new Uint32Array(1))[0];
    try {
      const key = `qanater-avatar-${uid}`, saved = localStorage.getItem(key);
      if (saved && /^\d{1,10}$/.test(saved)) return Number(saved);
      localStorage.setItem(key, String(generated));
    } catch { /* A private browser can block storage; the avatar still renders. */ }
    return generated;
  });
  const colors = ['#d7e8c4', '#f6d6b1', '#dfd5ee', '#bededb', '#f2d2d6', '#e6ddb8'];
  return <svg className={`avatar ${large ? 'avatar-large' : ''}`} viewBox="0 0 80 80" aria-hidden="true">
    <rect width="80" height="80" rx="28" fill={colors[seed % colors.length]}/>
    <path d="M9 80c1-24 17-29 31-29s30 5 31 29" fill={seed % 2 ? '#244d40' : '#ab6844'}/>
    <ellipse cx="40" cy="34" rx="20" ry="23" fill="#f1b98e"/>
    <path d={seed % 2 ? 'M20 31C12 1 66-2 61 34L51 17 20 31' : 'M19 30C16 2 64 0 61 30L43 17 19 30'} fill="#263c32"/>
    <circle cx="33" cy="34" r="2" fill="#263c32"/><circle cx="47" cy="34" r="2" fill="#263c32"/>
    <path d="M34 44q6 7 12 0" stroke="#874d39" strokeWidth="2.5" fill="none" strokeLinecap="round"/>
    {seed % 3 === 0 && <g fill="none" stroke="#263c32" strokeWidth="1.8"><rect x="25" y="28" width="14" height="12" rx="5"/><rect x="41" y="28" width="14" height="12" rx="5"/><path d="M39 32h2"/></g>}
  </svg>;
}
