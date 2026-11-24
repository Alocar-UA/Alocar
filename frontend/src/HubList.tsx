import { useEffect, useState } from 'react';
import { fetchHubs, type Hub } from './api';

function openingHours(hub: Hub): string {
  if (hub.openingTime === null || hub.closingTime === null) {
    return 'aberto 24 horas';
  }
  return `aberto das ${hub.openingTime.slice(0, 5)} às ${hub.closingTime.slice(0, 5)}`;
}

function freeScooters(count: number): string {
  return count === 1 ? '1 trotinete livre' : `${count} trotinetes livres`;
}

type State =
  { status: 'loading' } | { status: 'error'; message: string } | { status: 'loaded'; hubs: Hub[] };

export function HubList() {
  const [state, setState] = useState<State>({ status: 'loading' });

  useEffect(() => {
    fetchHubs()
      .then((hubs) => setState({ status: 'loaded', hubs }))
      .catch((error: Error) => setState({ status: 'error', message: error.message }));
  }, []);

  if (state.status === 'loading') {
    return <p>A carregar hubs…</p>;
  }
  if (state.status === 'error') {
    return <p role="alert">Não foi possível carregar os hubs: {state.message}</p>;
  }
  if (state.hubs.length === 0) {
    return <p>Ainda não existem hubs registados.</p>;
  }
  return (
    <ul aria-label="Hubs">
      {state.hubs.map((hub) => (
        <li key={hub.id}>
          <strong>{hub.name}</strong> — {freeScooters(hub.availableScooters)}, {openingHours(hub)}
        </li>
      ))}
    </ul>
  );
}
