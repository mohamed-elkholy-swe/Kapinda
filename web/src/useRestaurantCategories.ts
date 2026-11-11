import { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import type { RestaurantCategory } from './restaurantCategories';
export default function useRestaurantCategories() {
  const [categories, setCategories] = useState<RestaurantCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const reload = useCallback(async () => {
    setLoading(true); setError(false);
    try { setCategories(await api<RestaurantCategory[]>('/restaurant-categories')); }
    catch { setError(true); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void reload(); }, [reload]);
  return { categories, loading, error, reload, setCategories };
}
