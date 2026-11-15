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
          { id: 1, name: 'Hub Bloco A', block: 'A', capacity: 8 },
          { id: 2, name: 'Hub Bloco B', block: 'B', capacity: 6 },
        ]),
    });

    render(<HubList />);

    expect(await screen.findByText('Hub Bloco A')).toBeInTheDocument();
    expect(screen.getByText('Hub Bloco B')).toBeInTheDocument();
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
