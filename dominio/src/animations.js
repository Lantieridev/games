const ANIM_FALLBACK_MS = {
  move:   250,
  attack: 350,
  mutate: 450,
};

function animate(element, className, fallbackMs) {
  return new Promise(resolve => {
    if (!element) { resolve(); return; }

    const done = () => {
      element.classList.remove(className);
      resolve();
    };

    element.classList.add(className);
    element.addEventListener('animationend', done, { once: true });
    // Fallback: resolve after expected duration in case animationend doesn't fire.
    setTimeout(done, fallbackMs);
  });
}

/**
 * Plays the arrival animation on a piece element after it has been moved in the DOM.
 * @param {HTMLElement} element — the piece element at its new position
 */
export function animateMove(element) {
  return animate(element, 'piece-anim-move', ANIM_FALLBACK_MS.move);
}

/**
 * Plays the attack flash on the attacking piece element.
 */
export function animateAttack(element) {
  return animate(element, 'piece-anim-attack', ANIM_FALLBACK_MS.attack);
}

/**
 * Plays the morph animation on a piece element after its type has changed.
 */
export function animateMutate(element) {
  return animate(element, 'piece-anim-mutate', ANIM_FALLBACK_MS.mutate);
}
