import { useState } from 'react';
export default function RestaurantImage({ src, name, fallback }: { src?: string | null; name: string; fallback: string }) {
  const [failedSrc, setFailedSrc] = useState<string | null>(null);
  return src && failedSrc !== src ? <img className="restaurant-photo" src={src} alt={name} loading="lazy" referrerPolicy="no-referrer" onError={() => setFailedSrc(src)}/> : <span aria-hidden="true">{fallback}</span>;
}
