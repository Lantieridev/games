import { describe, it, expect } from 'vitest';
import { animateMove, animateAttack, animateMutate } from '../src/animations.js';

describe('animation helpers', () => {
  it('resolves immediately when the element is null', async () => {
    await expect(animateMove(null)).resolves.toBeUndefined();
  });

  it('adds the class, resolves on animationend, and removes the class', async () => {
    const el = document.createElement('div');
    const promise = animateMove(el);
    expect(el.classList.contains('piece-anim-move')).toBe(true);
    el.dispatchEvent(new Event('animationend'));
    await promise;
    expect(el.classList.contains('piece-anim-move')).toBe(false);
  });

  it('resolves via the fallback timeout if animationend never fires', async () => {
    const el = document.createElement('div');
    const promise = animateAttack(el);
    await promise; // fallback fires after 350ms in real time; vitest waits for it
    expect(el.classList.contains('piece-anim-attack')).toBe(false);
  }, 1000);

  it('applies the mutate animation class', async () => {
    const el = document.createElement('div');
    const promise = animateMutate(el);
    expect(el.classList.contains('piece-anim-mutate')).toBe(true);
    el.dispatchEvent(new Event('animationend'));
    await promise;
  });
});
