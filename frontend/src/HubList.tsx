import { useEffect, useState } from 'react';
import { fetchHubs, type Hub } from './api';

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
          <strong>{hub.name}</strong> — bloco {hub.block}, capacidade {hub.capacity}
        </li>
      ))}
    </ul>
  );
}
