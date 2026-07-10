import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mountFixture } from './helpers/domFixture.js';
import { saveRankingEntry } from '../src/storageManager.js';

const MINI_BOARD = {
  id: 'mini',
  name: 'Mini',
  description: 'One piece per side, for deterministic E2E testing.',
  playerLayout: ['R'],
  aiLayout: ['B'],
};

function cell(row, col) {
  return document.querySelector(`#board [data-row="${row}"][data-col="${col}"]`);
}

function mockFetchOk(boards, ranking = []) {
  global.fetch = vi.fn(url => {
    if (String(url).includes('boards.json')) {
      return Promise.resolve({ ok: true, json: () => Promise.resolve(boards) });
    }
    if (String(url).includes('ranking.json')) {
      return Promise.resolve({ ok: true, json: () => Promise.resolve(ranking) });
    }
    return Promise.reject(new Error('unexpected fetch: ' + url));
  });
}

async function flushMicrotasks() {
  for (let i = 0; i < 5; i++) await Promise.resolve();
}

function waitMs(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Polls until it's the human's turn again. The AI's action is gated behind
 * a queued move-animation fallback (250ms, since jsdom never fires a real
 * 'animationend') plus the game loop's own 300ms "thinking" delay — polling
 * the turn indicator is more robust than guessing a fixed sleep duration.
 */
async function waitForHumanTurn() {
  const label = document.getElementById('turn-player-label');
  for (let i = 0; i < 40; i++) {
    if (label.textContent === 'Tu turno') return;
    await waitMs(50);
  }
  throw new Error('Timed out waiting for the human turn');
}

async function bootApp() {
  vi.resetModules();
  await import('../src/main.js');
  await flushMicrotasks();
}

beforeEach(() => {
  localStorage.clear();
  mountFixture();
});

describe('main() boot wiring', () => {
  it('populates the board select from the fetched configs and enables the start button', async () => {
    mockFetchOk([MINI_BOARD]);
    await bootApp();

    const options = document.getElementById('board-select').querySelectorAll('option');
    expect(options).toHaveLength(1);
    expect(options[0].textContent).toContain('Mini');
    expect(document.getElementById('start-btn').disabled).toBe(false);
  });

  it('falls back to the default board and warns when the board fetch fails', async () => {
    global.fetch = vi.fn(url => {
      if (String(url).includes('boards.json')) return Promise.resolve({ ok: false, status: 500 });
      return Promise.resolve({ ok: true, json: () => Promise.resolve([]) });
    });
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    await bootApp();

    const options = document.getElementById('board-select').querySelectorAll('option');
    expect(options).toHaveLength(1); // DEFAULT_BOARD
    expect(warnSpy).toHaveBeenCalled();
  });

  it('seeds the local ranking from the fetched remote ranking when local is empty', async () => {
    mockFetchOk([MINI_BOARD], [{ playerName: 'Seed', playerScore: 99 }]);
    await bootApp();

    expect(JSON.parse(localStorage.getItem('dominio_ranking'))).toEqual([{ playerName: 'Seed', playerScore: 99 }]);
  });

  it('renders "no games" when the ranking is empty', async () => {
    mockFetchOk([MINI_BOARD]);
    await bootApp();

    document.getElementById('open-ranking-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(document.getElementById('ranking-modal').open).toBe(true);
    expect(document.getElementById('ranking-body').textContent).toContain('Sin partidas registradas');

    document.getElementById('close-ranking-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(document.getElementById('ranking-modal').open).toBe(false);
  });

  it('renders stored ranking entries with player/ai/draw icons in the modal', async () => {
    saveRankingEntry({ playerName: 'A', playerScore: 50, turns: 12, winner: 'player', date: new Date().toISOString() });
    saveRankingEntry({ playerName: 'B', playerScore: 30, turns: 20, winner: 'ai', date: new Date().toISOString() });
    saveRankingEntry({ playerName: 'C', playerScore: 10, turns: 5, winner: 'draw', date: new Date().toISOString() });
    mockFetchOk([MINI_BOARD]);
    await bootApp();

    document.getElementById('open-ranking-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    const body = document.getElementById('ranking-body').textContent;
    expect(body).toContain('🏆');
    expect(body).toContain('🤖');
    expect(body).toContain('🤝');
  });

  it('warns when the ranking fetch fails but still boots normally', async () => {
    global.fetch = vi.fn(url => {
      if (String(url).includes('boards.json')) return Promise.resolve({ ok: true, json: () => Promise.resolve([MINI_BOARD]) });
      return Promise.resolve({ ok: false, status: 500 });
    });
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    await bootApp();
    expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('Ranking'), expect.any(String));
  });

  it('closes the ranking modal on a genuine backdrop click but not on an inner click', async () => {
    mockFetchOk([MINI_BOARD]);
    await bootApp();

    document.getElementById('open-ranking-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    const modal = document.getElementById('ranking-modal');

    const innerClick = new MouseEvent('click', { bubbles: true });
    Object.defineProperty(innerClick, 'target', { value: document.getElementById('ranking-modal-title') });
    modal.dispatchEvent(innerClick);
    expect(modal.open).toBe(true);

    const backdropClick = new MouseEvent('click', { bubbles: true });
    Object.defineProperty(backdropClick, 'target', { value: modal });
    modal.dispatchEvent(backdropClick);
    expect(modal.open).toBe(false);
  });

  it('defaults to "Jugador" when the name field is left blank and honors the "normal" difficulty', async () => {
    mockFetchOk([MINI_BOARD]);
    await bootApp();

    document.getElementById('player-name').value = '';
    document.querySelector('input[name="difficulty"][value="normal"]').checked = true;
    document.getElementById('setup-form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await flushMicrotasks();

    expect(document.getElementById('player-hud-name').textContent).toBe('Jugador');
  });
});

describe('full game playthrough via the public setup form', () => {
  it('plays a deterministic 1v1 game to a human win and records the ranking entry', async () => {
    mockFetchOk([MINI_BOARD]);
    await bootApp();

    // Force RandomAI's pickRandom to always choose index 0 ("up" is checked
    // first in aiEngine's DIRS order), making the AI's moves fully predictable.
    const randomSpy = vi.spyOn(Math, 'random').mockReturnValue(0);

    document.getElementById('player-name').value = 'Tester';
    document.getElementById('setup-form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await flushMicrotasks();

    expect(document.getElementById('setup-screen').hidden).toBe(true);
    expect(document.getElementById('game-screen').hidden).toBe(false);
    expect(document.getElementById('player-hud-name').textContent).toBe('Tester');

    // Human piece starts at (0,0) [Rock], AI piece at (7,0) [Mist]. With the
    // AI always moving "up" (toward row 0), both close the gap along column 0.
    // Human moves down 3 times; by then the AI (moving up 3 times) is at
    // (4,0), adjacent to the human at (3,0) — the 4th action is the winning attack.
    for (let step = 0; step < 3; step++) {
      const from = cell(step, 0);
      const to = cell(step + 1, 0);
      from.dispatchEvent(new MouseEvent('click', { bubbles: true }));
      to.dispatchEvent(new MouseEvent('click', { bubbles: true }));
      await waitForHumanTurn();
    }

    expect(document.getElementById('ai-pieces').textContent).toBe('1');
    expect(cell(4, 0).querySelector('[data-piece-id]')).not.toBeNull(); // AI piece landed here

    cell(3, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(4, 0).dispatchEvent(new MouseEvent('click', { bubbles: true })); // Rock attacks Mist
    await flushMicrotasks();

    expect(document.getElementById('gameover-overlay').hidden).toBe(false);
    expect(document.getElementById('gameover-title').textContent).toBe('¡Ganaste!');

    const ranking = JSON.parse(localStorage.getItem('dominio_ranking'));
    expect(ranking).toHaveLength(1);
    expect(ranking[0]).toMatchObject({ playerName: 'Tester', winner: 'player', playerScore: 10 });

    randomSpy.mockRestore();
  }, 10000);

  it('play-again returns to the setup screen and tears down the finished game', async () => {
    mockFetchOk([MINI_BOARD]);
    await bootApp();
    vi.spyOn(Math, 'random').mockReturnValue(0);

    document.getElementById('setup-form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await flushMicrotasks();

    for (let step = 0; step < 3; step++) {
      cell(step, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
      cell(step + 1, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
      await waitForHumanTurn();
    }
    cell(3, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(4, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await flushMicrotasks();

    // Sanity check: the game must have actually ended before play-again is meaningful.
    expect(document.getElementById('gameover-overlay').hidden).toBe(false);

    document.getElementById('play-again-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));

    expect(document.getElementById('gameover-overlay').hidden).toBe(true);
    expect(document.getElementById('game-screen').hidden).toBe(true);
    expect(document.getElementById('setup-screen').hidden).toBe(false);

    vi.restoreAllMocks();
  }, 10000);
});
