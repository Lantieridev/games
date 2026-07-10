/**
 * Hanged Man Game Logic
 * Professional Version for Lantieridev Portfolio
 */

const CATEGORIES = {
    "TECHNOLOGY": ["JAVASCRIPT", "PYTHON", "REACT", "ANGULAR", "DOCKER", "KUBERNETES"],
    "PROGRAMMING": ["ALGORITHM", "DATABASE", "FRONTEND", "BACKEND", "FULLSTACK"],
    "CYBERSECURITY": ["SECURITY", "FIREWALL", "ENCRYPTION", "PHISHING", "MALWARE"]
};

const MAX_ATTEMPTS = 6;

let state = {
    word: "",
    category: "",
    guessedLetters: [],
    remainingAttempts: MAX_ATTEMPTS,
    isGameOver: false,
    wins: parseInt(localStorage.getItem('hangman_wins')) || 0
};

/**
 * Initializes a new game
 */
function initGame() {
    const cats = Object.keys(CATEGORIES);
    const randomCat = cats[Math.floor(Math.random() * cats.length)];
    const words = CATEGORIES[randomCat];
    const randomWord = words[Math.floor(Math.random() * words.length)];
    
    state.word = randomWord;
    state.category = randomCat;
    state.guessedLetters = [];
    state.remainingAttempts = MAX_ATTEMPTS;
    state.isGameOver = false;
    
    if (typeof document !== 'undefined') {
        updateUI();
        
        const resetButton = document.getElementById("reset-button");
        if (resetButton) resetButton.style.display = "none";
        
        const input = document.getElementById("letter-input");
        const btn = document.getElementById("try-button");
        const msg = document.getElementById("message");
        if (input) {
            input.disabled = false;
            input.value = "";
            input.focus();
        }
        if (btn) btn.disabled = false;
        if (msg) {
            msg.textContent = "Welcome. Guess the word to survive.";
            msg.className = "";
        }
    }
}

/**
 * Processes a letter attempt
 */
function attemptLetter() {
    if (state.isGameOver) return;

    const input = document.getElementById("letter-input");
    if (!input) return;

    const letter = input.value.toUpperCase();
    input.value = "";
    input.focus();

    if (!letter || !/^[A-Z]$/.test(letter)) {
        showMessage("Please enter a valid letter (A-Z).");
        return;
    }

    if (state.guessedLetters.includes(letter)) {
        showMessage(`You already tried "${letter}".`);
        return;
    }

    state.guessedLetters.push(letter);

    if (!state.word.includes(letter)) {
        state.remainingAttempts--;
        showMessage(`Wrong! "${letter}" is not in the word.`);
    } else {
        showMessage(`Good job! "${letter}" is correct.`);
    }

    checkGameState();
    updateUI();
}

/**
 * Checks if the game has ended
 */
function checkGameState() {
    const hasWon = state.word.split('').every(l => state.guessedLetters.includes(l));
    
    if (hasWon) {
        state.isGameOver = true;
        endGame(true);
    } else if (state.remainingAttempts <= 0) {
        state.isGameOver = true;
        endGame(false);
    }
}

/**
 * Updates the DOM elements based on the current state
 */
function updateUI() {
    const wordDisplay = document.getElementById("word-display");
    const categoryDisplay = document.getElementById("category-display");
    const winCount = document.getElementById("win-count");
    const guessedLettersDisplay = document.getElementById("guessed-letters");
    const attemptsDisplay = document.getElementById("attempts-display");

    if (!wordDisplay) return;

    if (categoryDisplay) categoryDisplay.textContent = state.category;
    if (winCount) winCount.textContent = state.wins;

    if (attemptsDisplay) {
        attemptsDisplay.setAttribute("aria-valuenow", state.remainingAttempts);
        attemptsDisplay.innerHTML = Array.from({ length: MAX_ATTEMPTS }, (_, i) =>
            `<span class="attempts-pip${i >= state.remainingAttempts ? " used" : ""}"></span>`
        ).join('');
    }

    const display = state.word.split('').map(l => 
        state.guessedLetters.includes(l) ? l : "_"
    ).join(' ');
    
    wordDisplay.textContent = display;

    if (guessedLettersDisplay) {
        const section = document.getElementById("guessed-letters-section");
        if (state.guessedLetters.length > 0) {
            if (section) section.style.display = "block";
            guessedLettersDisplay.innerHTML = state.guessedLetters
                .map(l => `<span class="guessed-letter">${l}</span>`)
                .join('');
        } else {
            if (section) section.style.display = "none";
            guessedLettersDisplay.innerHTML = "";
        }
    }
}

/**
 * Displays a temporary message
 */
function showMessage(text) {
    const msg = document.getElementById("message");
    if (msg) msg.textContent = text;
}

/**
 * Handles game end state
 */
function endGame(won) {
    const msg = document.getElementById("message");
    const input = document.getElementById("letter-input");
    const btn = document.getElementById("try-button");
    const resetButton = document.getElementById("reset-button");

    if (input) input.disabled = true;
    if (btn) btn.disabled = true;
    if (resetButton) resetButton.style.display = "block";

    if (won) {
        state.wins++;
        localStorage.setItem('hangman_wins', state.wins);
        if (msg) {
            msg.textContent = "Victory! You've survived.";
            msg.className = "win";
        }
    } else {
        if (msg) {
            msg.textContent = `Defeat. The word was: ${state.word}`;
            msg.className = "loss";
        }
    }
}

// Event Listeners
if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        initGame();
        
        const tryBtn = document.getElementById("try-button");
        const resetBtn = document.getElementById("reset-button");
        const letterInput = document.getElementById("letter-input");

        if (tryBtn) tryBtn.addEventListener("click", attemptLetter);
        if (resetBtn) resetBtn.addEventListener("click", initGame);
        
        if (letterInput) {
            letterInput.addEventListener("keypress", (e) => {
                if (e.key === "Enter") attemptLetter();
            });
        }
    });
}

// Export for testing
if (typeof module !== 'undefined') {
    module.exports = { state, initGame, attemptLetter, CATEGORIES };
}
