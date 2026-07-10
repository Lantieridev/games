const DEFAULT_BOARD = {
  id: 'classic',
  name: 'Clásico',
  description: 'Ambos jugadores comienzan agrupados por tipo.',
  playerLayout: ['R', 'R', 'R', 'E', 'E', 'E', 'B', 'B'],
  aiLayout:     ['R', 'R', 'R', 'E', 'E', 'E', 'B', 'B'],
};

export { DEFAULT_BOARD };

/**
 * Fetches all board variant configs from /data/boards.json.
 * @returns {Promise<{configs: Array, error: string|null}>}
 */
export async function fetchBoardConfigs() {
  try {
    const res = await fetch('/data/boards.json');
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    if (!Array.isArray(data) || data.length === 0) throw new Error('empty payload');
    return { configs: data, error: null };
  } catch (err) {
    return { configs: [DEFAULT_BOARD], error: err.message };
  }
}

/**
 * Fetches seed ranking entries from /data/ranking.json.
 * @returns {Promise<{entries: Array, error: string|null}>}
 */
export async function fetchRanking() {
  try {
    const res = await fetch('/data/ranking.json');
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return { entries: await res.json(), error: null };
  } catch (err) {
    return { entries: [], error: err.message };
  }
}
