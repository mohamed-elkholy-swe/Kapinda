import RetryImage from './RetryImage';
export default function RestaurantImage({ src, name, fallback }: { src?: string | null; name: string; fallback: string }) {
  const placeholder = <span aria-hidden="true">{fallback}</span>;
  return src ? <RetryImage className="restaurant-photo" src={src} alt={name} loading="lazy" referrerPolicy="no-referrer" fallback={() => placeholder}/> : placeholder;
}
