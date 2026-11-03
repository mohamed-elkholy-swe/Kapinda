import { useEffect, useState, type FormEvent } from 'react';
import { api } from './api';
import type { Translate } from './Orders';

type Area = { id: number; name: string; fee: number };

function AreaRow({ area, t, saving, saved, onSave }: {
  area: Area;
  t: Translate;
  saving: boolean;
  saved: boolean;
  onSave: (event: FormEvent<HTMLFormElement>, id: number) => Promise<void>;
}) {
  const [name, setName] = useState(area.name);
  const [fee, setFee] = useState(String(area.fee));
  const changed = name.trim() !== area.name || fee === '' || Number(fee) !== area.fee;
  const formId = `delivery-area-${area.id}`;

  return <tr>
    <td><input form={formId} name="name" aria-label={t('اسم المنطقة', 'Area name')} value={name} onChange={event => setName(event.target.value)} disabled={saving} maxLength={80} required/></td>
    <td><input form={formId} name="fee" aria-label={t('رسوم التوصيل بالجنيه', 'Delivery fee (EGP)')} type="number" min="0" max="999999" step="1" value={fee} onChange={event => setFee(event.target.value)} disabled={saving} required/></td>
    <td>
      <form id={formId} onSubmit={event => {
        event.preventDefault();
        if (changed && !saving) void onSave(event, area.id);
      }}>
        {changed && <button className="primary" disabled={saving || !name.trim()}>{t('حفظ', 'Save')}</button>}
        {!changed && saved && <span role="status">{t('تم الحفظ', 'Saved')}</span>}
      </form>
    </td>
  </tr>;
}

export default function DeliveryAreas({ t }: { t: Translate }) {
  const [areas, setAreas] = useState<Area[]>([]);
  const [saving, setSaving] = useState<number>();
  const [error, setError] = useState('');
  const [saved, setSaved] = useState<number>();

  async function load() {
    setError('');
    try {
      setAreas(await api<Area[]>('/delivery-areas', { cache: 'no-store' }));
    } catch (err) {
      setError((err as Error).message);
    }
  }

  useEffect(() => { void load(); }, []);

  async function save(event: FormEvent<HTMLFormElement>, id: number) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setSaving(id);
    setSaved(undefined);
    setError('');
    try {
      const area = await api<Area>(`/admin/delivery-areas/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ name: String(data.get('name')).trim(), fee: Number(data.get('fee')) }),
      }, true);
      setAreas(current => current.map(entry => entry.id === id ? area : entry));
      setSaved(id);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setSaving(undefined);
    }
  }

  return <section className="panel">
    <p>{t('تعديلات رسوم التوصيل تسري على الطلبات الجديدة.', 'Changes apply to new orders.')}</p>
    {error && <p className="alert" role="alert">{error}</p>}
    {error && !areas.length && <button onClick={() => void load()}>{t('أعد المحاولة', 'Retry')}</button>}
    {!error && !areas.length && <p role="status">{t('جاري تحميل المناطق…', 'Loading areas…')}</p>}
    {!!areas.length && <table className="delivery-areas-table">
      <thead><tr>
        <th scope="col">{t('المنطقة', 'Area')}</th>
        <th scope="col">{t('التوصيل (ج.م.)', 'Fee (EGP)')}</th>
        <th scope="col">{t('الإجراء', 'Action')}</th>
      </tr></thead>
      <tbody>{areas.map(area => <AreaRow key={JSON.stringify(area)} area={area} t={t} saving={saving !== undefined} saved={saved === area.id} onSave={save}/>)}</tbody>
    </table>}
  </section>;
}
