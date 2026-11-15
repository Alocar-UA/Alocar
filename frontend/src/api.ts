export interface Hub {
  id: number;
  name: string;
  block: string;
  capacity: number;
}

export async function fetchHubs(): Promise<Hub[]> {
  const response = await fetch('/api/v1/hubs');
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return response.json();
}
