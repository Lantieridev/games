const RANKING_KEY      = 'dominio_ranking';
const PLAYER_NAME_KEY  = 'dominio_player_name';
const RANKING_CAP      = 50;

export function save(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch { /* quota exceeded — silently skip */ }
}

export function load(key, defaultValue = null) {
  try {
    const raw = localStorage.getItem(key);
    return raw !== null ? JSON.parse(raw) : defaultValue;
  } catch {
    return defaultValue;
  }
}

export function savePlayerName(name) {
  save(PLAYER_NAME_KEY, name);
}

export function loadPlayerName(fallback = 'Jugador') {
  return load(PLAYER_NAME_KEY, fallback) || fallback;
}

/**
 * Persists a ranking entry.
 * - Deduplicates by playerName: keeps the higher score.
 * - Sorts descending by playerScore.
 * - Caps the list at RANKING_CAP (50).
 */
export function saveRankingEntry(entry) {
  let ranking = load(RANKING_KEY, []);
  if (!Array.isArray(ranking)) ranking = [];

  const existingIdx = ranking.findIndex(e => e.playerName === entry.playerName);

  if (existingIdx >= 0) {
    if (ranking[existingIdx].playerScore >= entry.playerScore) return; // existing score is better
    ranking.splice(existingIdx, 1); // remove lower score before inserting new
  }

  ranking.push(entry);
  ranking.sort((a, b) => b.playerScore - a.playerScore);
  ranking = ranking.slice(0, RANKING_CAP);

  save(RANKING_KEY, ranking);
}

/**
 * Returns all stored ranking entries sorted by playerScore descending.
 */
export function loadRanking() {
  const data = load(RANKING_KEY, []);
  return Array.isArray(data) ? data : [];
}

/** Seeds localStorage with remote ranking data if local ranking is empty. */
export function seedRankingIfEmpty(remoteEntries) {
  const local = loadRanking();
  if (local.length === 0 && Array.isArray(remoteEntries)) {
    save(RANKING_KEY, remoteEntries.slice(0, RANKING_CAP));
  }
}
