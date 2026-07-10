import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  save,
  load,
  savePlayerName,
  loadPlayerName,
  saveRankingEntry,
  loadRanking,
  seedRankingIfEmpty,
} from '../src/storageManager.js';

beforeEach(() => {
  localStorage.clear();
});

describe('save / load', () => {
  it('round-trips a JSON-serializable value', () => {
    save('k', { a: 1 });
    expect(load('k')).toEqual({ a: 1 });
  });

  it('returns the default value when the key is missing', () => {
    expect(load('missing', 'fallback')).toBe('fallback');
  });

  it('returns the default value when stored JSON is corrupt', () => {
    localStorage.setItem('bad', '{not json');
    expect(load('bad', 'fallback')).toBe('fallback');
  });

  it('silently ignores a quota-exceeded write', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new DOMException('quota', 'QuotaExceededError');
    });
    expect(() => save('k', 'v')).not.toThrow();
    spy.mockRestore();
  });
});

describe('player name persistence', () => {
  it('saves and loads the player name', () => {
    savePlayerName('Martín');
    expect(loadPlayerName()).toBe('Martín');
  });

  it('falls back to the default when nothing is stored', () => {
    expect(loadPlayerName('Jugador')).toBe('Jugador');
  });

  it('falls back when the stored name is empty', () => {
    savePlayerName('');
    expect(loadPlayerName('Jugador')).toBe('Jugador');
  });
});

describe('saveRankingEntry', () => {
  it('inserts a new entry and sorts descending by score', () => {
    saveRankingEntry({ playerName: 'A', playerScore: 10 });
    saveRankingEntry({ playerName: 'B', playerScore: 50 });
    const ranking = loadRanking();
    expect(ranking.map(e => e.playerName)).toEqual(['B', 'A']);
  });

  it('keeps the higher score when the same player plays again', () => {
    saveRankingEntry({ playerName: 'A', playerScore: 10 });
    saveRankingEntry({ playerName: 'A', playerScore: 5 });
    const ranking = loadRanking();
    expect(ranking).toHaveLength(1);
    expect(ranking[0].playerScore).toBe(10);
  });

  it('replaces the entry when the new score is higher', () => {
    saveRankingEntry({ playerName: 'A', playerScore: 5 });
    saveRankingEntry({ playerName: 'A', playerScore: 20 });
    const ranking = loadRanking();
    expect(ranking).toHaveLength(1);
    expect(ranking[0].playerScore).toBe(20);
  });

  it('caps the list at 50 entries', () => {
    for (let i = 0; i < 55; i++) {
      saveRankingEntry({ playerName: `p${i}`, playerScore: i });
    }
    expect(loadRanking()).toHaveLength(50);
  });

  it('recovers when the stored ranking is not an array', () => {
    save('dominio_ranking', { corrupt: true });
    saveRankingEntry({ playerName: 'A', playerScore: 10 });
    expect(loadRanking()).toHaveLength(1);
  });
});

describe('loadRanking', () => {
  it('returns an empty array when nothing is stored', () => {
    expect(loadRanking()).toEqual([]);
  });

  it('returns an empty array when stored data is not an array', () => {
    save('dominio_ranking', 'not-an-array');
    expect(loadRanking()).toEqual([]);
  });
});

describe('seedRankingIfEmpty', () => {
  it('seeds local ranking from remote entries when local is empty', () => {
    seedRankingIfEmpty([{ playerName: 'Seed', playerScore: 1 }]);
    expect(loadRanking()).toEqual([{ playerName: 'Seed', playerScore: 1 }]);
  });

  it('does not overwrite existing local ranking', () => {
    saveRankingEntry({ playerName: 'A', playerScore: 10 });
    seedRankingIfEmpty([{ playerName: 'Seed', playerScore: 1 }]);
    expect(loadRanking()).toHaveLength(1);
    expect(loadRanking()[0].playerName).toBe('A');
  });

  it('does nothing when remote entries are not an array', () => {
    seedRankingIfEmpty(null);
    expect(loadRanking()).toEqual([]);
  });

  it('caps seeded entries at 50', () => {
    const remote = Array.from({ length: 60 }, (_, i) => ({ playerName: `p${i}`, playerScore: i }));
    seedRankingIfEmpty(remote);
    expect(loadRanking()).toHaveLength(50);
  });
});
