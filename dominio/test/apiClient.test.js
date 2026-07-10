import { describe, it, expect, beforeEach, vi } from 'vitest';
import { fetchBoardConfigs, fetchRanking, DEFAULT_BOARD } from '../src/apiClient.js';

beforeEach(() => {
  vi.restoreAllMocks();
});

describe('fetchBoardConfigs', () => {
  it('returns the parsed configs on success', async () => {
    const configs = [{ id: 'classic' }];
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve(configs) });
    const result = await fetchBoardConfigs();
    expect(result).toEqual({ configs, error: null });
  });

  it('falls back to the default board on a non-ok response', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: false, status: 500 });
    const result = await fetchBoardConfigs();
    expect(result.configs).toEqual([DEFAULT_BOARD]);
    expect(result.error).toBe('HTTP 500');
  });

  it('falls back to the default board on an empty payload', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve([]) });
    const result = await fetchBoardConfigs();
    expect(result.configs).toEqual([DEFAULT_BOARD]);
    expect(result.error).toBe('empty payload');
  });

  it('falls back to the default board when fetch rejects', async () => {
    global.fetch = vi.fn().mockRejectedValue(new Error('network down'));
    const result = await fetchBoardConfigs();
    expect(result.configs).toEqual([DEFAULT_BOARD]);
    expect(result.error).toBe('network down');
  });
});

describe('fetchRanking', () => {
  it('returns parsed entries on success', async () => {
    const entries = [{ playerName: 'A' }];
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve(entries) });
    const result = await fetchRanking();
    expect(result).toEqual({ entries, error: null });
  });

  it('returns an empty list and error message on failure', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: false, status: 404 });
    const result = await fetchRanking();
    expect(result).toEqual({ entries: [], error: 'HTTP 404' });
  });

  it('returns an empty list when fetch rejects', async () => {
    global.fetch = vi.fn().mockRejectedValue(new Error('offline'));
    const result = await fetchRanking();
    expect(result).toEqual({ entries: [], error: 'offline' });
  });
});
