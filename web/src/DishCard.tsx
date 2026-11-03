import type { ReactNode } from 'react';
import RestaurantImage from './RestaurantImage';
import type { MenuItem } from './types';

export default function DishCard({ item, index, fallback, price, children }: {
  item: MenuItem;
  index: number;
  fallback: string;
  price: string;
  children: ReactNode;
}) {
  return <article className="dish-card">
    <div className={`dish-art dish-art-${index % 3}`}>
      <RestaurantImage src={item.photoUrl} name={item.name} fallback={fallback}/>
    </div>
    <div className="dish-copy">
      <h3>{item.name}</h3>
      <p>{item.description}</p>
      <div className="dish-footer"><strong>{price}</strong>{children}</div>
    </div>
  </article>;
}
