import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { HubList } from './HubList';

function mockFetch(response: Partial<Response>) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response));
}

describe('HubList', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('lists the hubs returned by the API', async () => {
    mockFetch({
      ok: true,
      json: () =>
        Promise.resolve([
          {
            id: 1,
            name: 'Hub Bloco A',
            block: 'A',
            capacity: 8,
            openingTime: '07:00:00',
            closingTime: '23:00:00',
            availableScooters: 2,
          },
          {
            id: 2,
            name: 'Hub Garagem Central',
            block: 'Garagem',
            capacity: 12,
            openingTime: null,
            closingTime: null,
            availableScooters: 1,
          },
        ]),
    });

    render(<HubList />);

    expect(await screen.findByText('Hub Bloco A')).toBeInTheDocument();
    expect(screen.getByText('Hub Garagem Central')).toBeInTheDocument();
  });

  it('shows free scooters and opening hours of each hub (US02)', async () => {
    mockFetch({
      ok: true,
      json: () =>
        Promise.resolve([
          {
            id: 1,
            name: 'Hub Bloco A',
            block: 'A',
            capacity: 8,
            openingTime: '07:00:00',
            closingTime: '23:00:00',
            availableScooters: 2,
          },
          {
            id: 2,
            name: 'Hub Garagem Central',
            block: 'Garagem',
            capacity: 12,
            openingTime: null,
            closingTime: null,
            availableScooters: 1,
          },
        ]),
    });

    render(<HubList />);

    const items = await screen.findAllByRole('listitem');
    expect(items[0]).toHaveTextContent('2 trotinetes livres, aberto das 07:00 às 23:00');
    expect(items[1]).toHaveTextContent('1 trotinete livre, aberto 24 horas');
  });

  it('shows an empty message when there are no hubs', async () => {
    mockFetch({ ok: true, json: () => Promise.resolve([]) });

    render(<HubList />);

    expect(await screen.findByText('Ainda não existem hubs registados.')).toBeInTheDocument();
  });

  it('shows an error when the API fails', async () => {
    mockFetch({ ok: false, status: 500 });

    render(<HubList />);

    expect(await screen.findByRole('alert')).toHaveTextContent('HTTP 500');
  });
});
