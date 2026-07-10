/** @vitest-environment jsdom */
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

const localStorageMock = (() => {
    let store = {};
    return {
        getItem: (key) => store[key] || null,
        setItem: (key, value) => { store[key] = value.toString(); },
        clear: () => { store = {}; },
    };
})();
Object.defineProperty(window, 'localStorage', { value: localStorageMock, configurable: true });

const { state, initGame, attemptLetter, CATEGORIES } = require('./index.js');

const FULL_DOM = `
    <div id="stats"><span id="win-count"></span></div>
    <span id="category-display"></span>
    <div id="word-display"></div>
    <div id="attempts-display"></div>
    <input id="letter-input" />
    <button id="try-button"></button>
    <button id="reset-button" style="display: none;"></button>
    <p id="message"></p>
    <div id="guessed-letters-section" style="display: none;">
        <div id="guessed-letters"></div>
    </div>
`;

function setLetter(letter) {
    document.getElementById('letter-input').value = letter;
}

/**
 * Guesses every unique letter of the current word except one, and returns
 * that final letter. Using the raw character array (word.split('').slice(0,-1))
 * instead breaks for words with a repeated final letter — e.g. "ENCRYPTION"
 * ends in "N", which also appears earlier, so slicing it off still leaves an
 * "N" in the guessed set and the last real guess becomes a no-op duplicate.
 */
function setUpOneLetterFromVictory() {
    const uniqueLetters = [...new Set(state.word.split(''))];
    state.guessedLetters = uniqueLetters.slice(0, -1);
    return uniqueLetters[uniqueLetters.length - 1];
}

beforeEach(() => {
    localStorage.clear();
    document.body.innerHTML = FULL_DOM;
    state.wins = 0; // state is a module-level singleton shared across tests
    initGame();
});

afterEach(() => {
    vi.restoreAllMocks();
});

describe('initGame', () => {
    it('picks a word deterministically based on the category/word rng draws', () => {
        vi.spyOn(Math, 'random').mockReturnValue(0);
        initGame();
        const firstCategory = Object.keys(CATEGORIES)[0];
        expect(state.category).toBe(firstCategory);
        expect(state.word).toBe(CATEGORIES[firstCategory][0]);
    });

    it('resets attempts, guessed letters, and game-over state', () => {
        state.guessedLetters = ['A', 'B'];
        state.remainingAttempts = 1;
        state.isGameOver = true;
        initGame();
        expect(state.remainingAttempts).toBe(6);
        expect(state.guessedLetters).toEqual([]);
        expect(state.isGameOver).toBe(false);
    });

    it('resets the DOM: input enabled and cleared, button enabled, reset button hidden, default message', () => {
        const input = document.getElementById('letter-input');
        const btn = document.getElementById('try-button');
        const resetBtn = document.getElementById('reset-button');
        input.disabled = true;
        input.value = 'X';
        btn.disabled = true;
        resetBtn.style.display = 'block';
        document.getElementById('message').className = 'win';

        initGame();

        expect(input.disabled).toBe(false);
        expect(input.value).toBe('');
        expect(btn.disabled).toBe(false);
        expect(resetBtn.style.display).toBe('none');
        expect(document.getElementById('message').textContent).toBe('Welcome. Guess the word to survive.');
        expect(document.getElementById('message').className).toBe('');
    });

    it('does nothing and does not throw when the DOM is unavailable', () => {
        vi.resetModules();
        vi.stubGlobal('document', undefined);
        expect(() => {
            const fresh = require('./index.js');
            fresh.initGame();
        }).not.toThrow();
        vi.unstubAllGlobals();
    });

    it('tolerates a page missing the reset/input/button/message elements', () => {
        document.body.innerHTML = '<div id="word-display"></div>';
        expect(() => initGame()).not.toThrow();
    });
});

describe('attemptLetter — validation', () => {
    it('rejects an empty letter', () => {
        setLetter('');
        attemptLetter();
        expect(document.getElementById('message').textContent).toBe('Please enter a valid letter (A-Z).');
        expect(state.guessedLetters).toEqual([]);
    });

    it('rejects a digit', () => {
        setLetter('5');
        attemptLetter();
        expect(document.getElementById('message').textContent).toBe('Please enter a valid letter (A-Z).');
    });

    it('rejects multiple characters', () => {
        setLetter('AB');
        attemptLetter();
        expect(document.getElementById('message').textContent).toBe('Please enter a valid letter (A-Z).');
    });

    it('rejects a letter already guessed without penalizing attempts', () => {
        const letter = state.word[0];
        setLetter(letter);
        attemptLetter();
        const attemptsAfterFirst = state.remainingAttempts;

        setLetter(letter);
        attemptLetter();

        expect(document.getElementById('message').textContent).toBe(`You already tried "${letter}".`);
        expect(state.remainingAttempts).toBe(attemptsAfterFirst);
        expect(state.guessedLetters.filter(l => l === letter)).toHaveLength(1);
    });

    it('does nothing once the game is already over', () => {
        state.isGameOver = true;
        const before = { ...state, guessedLetters: [...state.guessedLetters] };
        setLetter('A');
        attemptLetter();
        expect(state.guessedLetters).toEqual(before.guessedLetters);
    });

    it('does nothing when the input element is missing', () => {
        document.getElementById('letter-input').remove();
        expect(() => attemptLetter()).not.toThrow();
        expect(state.guessedLetters).toEqual([]);
    });
});

describe('attemptLetter — correct/incorrect guesses', () => {
    it('accepts a correct letter without losing an attempt', () => {
        const letter = state.word[0];
        setLetter(letter);
        attemptLetter();

        expect(state.guessedLetters).toContain(letter);
        expect(state.remainingAttempts).toBe(6);
        expect(document.getElementById('message').textContent).toBe(`Good job! "${letter}" is correct.`);
        expect(document.getElementById('word-display').textContent).toContain(letter);
    });

    it('penalizes a wrong letter', () => {
        const wrongLetter = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('').find(l => !state.word.includes(l));
        setLetter(wrongLetter);
        attemptLetter();

        expect(state.guessedLetters).toContain(wrongLetter);
        expect(state.remainingAttempts).toBe(5);
        expect(document.getElementById('message').textContent).toBe(`Wrong! "${wrongLetter}" is not in the word.`);
    });

    it('handles words with repeated letters without requiring the letter to be guessed twice', () => {
        // ENCRYPTION contains a repeated "N".
        vi.spyOn(Math, 'random').mockReturnValue(0);
        state.category = 'CYBERSECURITY';
        state.word = 'ENCRYPTION';
        state.guessedLetters = [];
        state.remainingAttempts = 6;
        state.isGameOver = false;

        for (const letter of new Set('ENCRYPTION'.split(''))) {
            setLetter(letter);
            attemptLetter();
        }

        expect(state.isGameOver).toBe(true);
        expect(document.getElementById('message').className).toBe('win');
    });
});

describe('win/loss flow', () => {
    it('declares victory on the final correct letter and persists a win to localStorage', () => {
        const lastLetter = setUpOneLetterFromVictory();
        setLetter(lastLetter);
        attemptLetter();

        expect(state.isGameOver).toBe(true);
        expect(document.getElementById('message').textContent).toBe('Victory! You\'ve survived.');
        expect(document.getElementById('message').className).toBe('win');
        expect(localStorage.getItem('hangman_wins')).toBe('1');
        expect(document.getElementById('letter-input').disabled).toBe(true);
        expect(document.getElementById('try-button').disabled).toBe(true);
        expect(document.getElementById('reset-button').style.display).toBe('block');
    });

    it('accumulates wins across multiple victories', () => {
        setLetter(setUpOneLetterFromVictory());
        attemptLetter();
        expect(state.wins).toBe(1);

        initGame();
        setLetter(setUpOneLetterFromVictory());
        attemptLetter();
        expect(state.wins).toBe(2);
        expect(document.getElementById('win-count').textContent).toBe('2');
    });

    it('declares defeat when attempts run out and reveals the word', () => {
        state.remainingAttempts = 1;
        const wrongLetter = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('').find(l => !state.word.includes(l));
        setLetter(wrongLetter);
        attemptLetter();

        expect(state.isGameOver).toBe(true);
        expect(state.remainingAttempts).toBe(0);
        expect(document.getElementById('message').textContent).toBe(`Defeat. The word was: ${state.word}`);
        expect(document.getElementById('message').className).toBe('loss');
        expect(localStorage.getItem('hangman_wins')).toBeNull();
    });

    it('tolerates a missing reset/input/button on game over', () => {
        document.getElementById('reset-button').remove();
        setLetter(setUpOneLetterFromVictory());
        expect(() => attemptLetter()).not.toThrow();
    });
});

describe('updateUI — attempts pips', () => {
    it('renders 6 filled pips at the start of a game', () => {
        const pips = document.querySelectorAll('#attempts-display .attempts-pip');
        expect(pips).toHaveLength(6);
        expect(document.querySelectorAll('#attempts-display .attempts-pip.used')).toHaveLength(0);
        expect(document.getElementById('attempts-display').getAttribute('aria-valuenow')).toBe('6');
    });

    it('marks a pip as used after each wrong guess', () => {
        const wrongLetter = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('').find(l => !state.word.includes(l));
        setLetter(wrongLetter);
        attemptLetter();
        expect(document.querySelectorAll('#attempts-display .attempts-pip.used')).toHaveLength(1);
        expect(document.getElementById('attempts-display').getAttribute('aria-valuenow')).toBe('5');
    });

    it('does not touch the pips when the element is missing', () => {
        document.getElementById('attempts-display').remove();
        expect(() => initGame()).not.toThrow();
    });
});

describe('updateUI — guessed letters panel', () => {
    it('hides the guessed-letters section when nothing has been guessed yet', () => {
        expect(document.getElementById('guessed-letters-section').style.display).toBe('none');
        expect(document.getElementById('guessed-letters').innerHTML).toBe('');
    });

    it('shows and populates the guessed-letters section once a letter is tried', () => {
        setLetter(state.word[0]);
        attemptLetter();
        expect(document.getElementById('guessed-letters-section').style.display).toBe('block');
        expect(document.getElementById('guessed-letters').innerHTML).toContain(state.word[0]);
    });

    it('does nothing when the word display element is missing', () => {
        document.getElementById('word-display').remove();
        expect(() => {
            setLetter(state.word[0]);
            attemptLetter();
        }).not.toThrow();
    });

    it('updates the guessed letters list without a section wrapper present', () => {
        const section = document.getElementById('guessed-letters-section');
        section.replaceWith(document.getElementById('guessed-letters')); // detach the wrapper, keep the child
        setLetter(state.word[0]);
        expect(() => attemptLetter()).not.toThrow();
        expect(document.getElementById('guessed-letters').innerHTML).toContain(state.word[0]);
    });

    it('skips the guessed-letters block entirely when that element is missing', () => {
        document.getElementById('guessed-letters').remove();
        setLetter(state.word[0]);
        expect(() => attemptLetter()).not.toThrow();
    });

    it('tolerates a missing category display or win counter', () => {
        document.getElementById('category-display').remove();
        document.getElementById('win-count').remove();
        expect(() => initGame()).not.toThrow();
    });
});

describe('DOM wiring (DOMContentLoaded)', () => {
    it('wires the try button, reset button, and Enter key to the right handlers', () => {
        document.body.innerHTML = FULL_DOM;
        document.dispatchEvent(new Event('DOMContentLoaded'));

        const wordBefore = state.word;
        setLetter(wordBefore[0]);
        document.getElementById('try-button').click();
        expect(state.guessedLetters).toContain(wordBefore[0]);

        document.getElementById('letter-input').dispatchEvent(new KeyboardEvent('keypress', { key: 'A', bubbles: true }));
        // A non-Enter keypress must not trigger a second attempt for the same letter.
        expect(state.guessedLetters.filter(l => l === wordBefore[0])).toHaveLength(1);

        const secondLetter = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('').find(l => !state.word.includes(l) && l !== wordBefore[0]);
        setLetter(secondLetter);
        document.getElementById('letter-input').dispatchEvent(new KeyboardEvent('keypress', { key: 'Enter', bubbles: true }));
        expect(state.guessedLetters).toContain(secondLetter);

        const wordBeforeReset = state.word;
        document.getElementById('reset-button').click();
        // A fresh game resets guessed letters even if the same word is redrawn.
        expect(state.guessedLetters).toEqual([]);
    });
});
