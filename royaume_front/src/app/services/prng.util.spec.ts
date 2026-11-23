import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  createSeededRng,
  createSimplex2D,
  fbm2D,
  hashSeed,
  mulberry32,
  randomSeedString,
} from './prng.util';

describe('prng.util', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('hashSeed', () => {
    it('returns identical uint32 hashes for identical seed and salt', () => {
      const h1 = hashSeed('Valdore-123', 'terrain');
      const h2 = hashSeed('Valdore-123', 'terrain');
      expect(h1).toBe(h2);
      expect(Number.isInteger(h1)).toBe(true);
      expect(h1).toBeGreaterThanOrEqual(0);
      expect(h1).toBeLessThanOrEqual(0xffffffff);
      expect(Object.is(h1, -0)).toBe(false);
    });

    it('matches numeric seeds with their equivalent string form', () => {
      expect(hashSeed(42)).toBe(hashSeed('42'));
      expect(hashSeed(42, 'placement')).toBe(hashSeed('42', 'placement'));
    });

    it('normalizes -0 and 0 identically without returning -0 or NaN', () => {
      const hNegZero = hashSeed(-0);
      const hPosZero = hashSeed(0);
      expect(hNegZero).toBe(hPosZero);
      expect(Object.is(hNegZero, -0)).toBe(false);
      expect(Number.isNaN(hNegZero)).toBe(false);
    });

    it('distinguishes empty string, negative numbers, floats, and Unicode', () => {
      expect(hashSeed('')).not.toBe(hashSeed(0));
      expect(hashSeed(-42)).not.toBe(hashSeed(42));
      expect(hashSeed(1.5)).not.toBe(hashSeed(1.6));
      expect(hashSeed('Élysée')).not.toBe(hashSeed('Elysee'));
      expect(hashSeed('é')).not.toBe(hashSeed('e'));
      expect(hashSeed('🏰')).not.toBe(hashSeed('⚔️'));
    });

    it('produces distinct hashes across domain salts and avoids concatenation collisions', () => {
      const hTerrain = hashSeed('royaume', 'terrain');
      const hPlacement = hashSeed('royaume', 'placement');
      const hFeatures = hashSeed('royaume', 'features');

      expect(new Set([hTerrain, hPlacement, hFeatures]).size).toBe(3);
      expect(hashSeed('ab', 'c')).not.toBe(hashSeed('a', 'bc'));
      expect(hashSeed('a:b', 'c')).not.toBe(hashSeed('a', 'b:c'));
    });
  });

  describe('mulberry32', () => {
    it('produces identical 1000-element sequences for the same seedInt', () => {
      const rngA = mulberry32(123456789);
      const rngB = mulberry32(123456789);
      const seqA = Array.from({ length: 1000 }, () => rngA());
      const seqB = Array.from({ length: 1000 }, () => rngB());
      expect(seqA).toEqual(seqB);
    });

    it('keeps separate instances isolated without cross-interference', () => {
      const rngA = mulberry32(999);
      const rngB = mulberry32(999);
      const rngOther = mulberry32(111);

      expect(rngA()).toBe(rngB());
      for (let i = 0; i < 50; i++) {
        rngOther();
      }
      expect(rngA()).toBe(rngB());
    });

    it('handles seedInt = 0, negative ints, and NaN safely', () => {
      const zeroRng = mulberry32(0);
      const firstFive = Array.from({ length: 5 }, () => zeroRng());
      expect(firstFive.some((v) => v > 0)).toBe(true);

      const negOneRng = mulberry32(-1);
      const maxUintRng = mulberry32(0xffffffff);
      expect(Array.from({ length: 10 }, () => negOneRng())).toEqual(
        Array.from({ length: 10 }, () => maxUintRng()),
      );

      const nanRng = mulberry32(Number.NaN);
      for (let i = 0; i < 20; i++) {
        const val = nanRng();
        expect(Number.isFinite(val)).toBe(true);
        expect(val).toBeGreaterThanOrEqual(0);
        expect(val).toBeLessThan(1);
      }
    });

    it('outputs finite values strictly in [0, 1) with reasonable mean over 10,000 samples', () => {
      for (const seedInt of [0, 42, 0xdeadbeef]) {
        const rng = mulberry32(seedInt);
        let sum = 0;
        const n = 10_000;
        for (let i = 0; i < n; i++) {
          const v = rng();
          expect(Number.isFinite(v)).toBe(true);
          expect(v).toBeGreaterThanOrEqual(0);
          expect(v).toBeLessThan(1);
          expect(Object.is(v, -0)).toBe(false);
          sum += v;
        }
        const mean = sum / n;
        expect(mean).toBeGreaterThanOrEqual(0.45);
        expect(mean).toBeLessThanOrEqual(0.55);
      }
    });
  });

  describe('createSeededRng', () => {
    it('is deterministic for same (seed, salt) and diverges on different seed or salt', () => {
      const a1 = createSeededRng('royaume-alpha', 'terrain');
      const a2 = createSeededRng('royaume-alpha', 'terrain');
      const b = createSeededRng('royaume-beta', 'terrain');
      const c = createSeededRng('royaume-alpha', 'placement');

      const seqA1 = Array.from({ length: 20 }, () => a1());
      const seqA2 = Array.from({ length: 20 }, () => a2());
      const seqB = Array.from({ length: 20 }, () => b());
      const seqC = Array.from({ length: 20 }, () => c());

      expect(seqA1).toEqual(seqA2);
      expect(seqA1).not.toEqual(seqB);
      expect(seqA1).not.toEqual(seqC);
    });
  });

  describe('createSimplex2D', () => {
    it('is deterministic for identical PRNGs, diverges for different seeds, and stays in [-1, 1]', () => {
      const n1 = createSimplex2D(createSeededRng('seed-1', 'noise'));
      const n2 = createSimplex2D(createSeededRng('seed-1', 'noise'));
      const nOther = createSimplex2D(createSeededRng('seed-2', 'noise'));

      let differences = 0;
      for (let x = -5; x <= 5; x += 0.7) {
        for (let y = -5; y <= 5; y += 0.7) {
          const v1 = n1(x, y);
          const v2 = n2(x, y);
          const vOther = nOther(x, y);

          expect(v1).toBe(v2);
          expect(Number.isFinite(v1)).toBe(true);
          expect(v1).toBeGreaterThanOrEqual(-1);
          expect(v1).toBeLessThanOrEqual(1);
          if (v1 !== vOther) {
            differences++;
          }
        }
      }
      expect(differences).toBeGreaterThan(0);
    });

    it('throws TypeError if rng is omitted or not a function (no Math.random fallback)', () => {
      expect(() => createSimplex2D(undefined as unknown as () => number)).toThrow(TypeError);
      expect(() => createSimplex2D(null as unknown as () => number)).toThrow(TypeError);
    });
  });

  describe('fbm2D', () => {
    it('is deterministic, matches raw noise at octaves = 1, and stays bounded in [-1, 1]', () => {
      const noise = createSimplex2D(createSeededRng('fbm-seed', 'elevation'));

      expect(fbm2D(noise, 1.25, -2.5, 1)).toBe(noise(1.25, -2.5));
      expect(fbm2D(noise, 3.1, 4.2, 4, 0.5, 2)).toBe(fbm2D(noise, 3.1, 4.2, 4, 0.5, 2));

      for (let octaves = 1; octaves <= 8; octaves++) {
        for (const persistence of [0.2, 0.5, 0.8]) {
          for (const lacunarity of [1.5, 2.0, 3.0]) {
            const val = fbm2D(noise, 2.34, -1.78, octaves, persistence, lacunarity);
            expect(Number.isFinite(val)).toBe(true);
            expect(val).toBeGreaterThanOrEqual(-1);
            expect(val).toBeLessThanOrEqual(1);
            expect(Object.is(val, -0)).toBe(false);
          }
        }
      }
    });

    it('returns 0 cleanly on invalid or non-finite parameters without NaN', () => {
      const noise = createSimplex2D(createSeededRng('fbm-edge', 'elevation'));

      const edgeCases = [
        fbm2D(noise, 1, 2, 0),
        fbm2D(noise, 1, 2, -3),
        fbm2D(noise, 1, 2, Number.NaN),
        fbm2D(noise, 1, 2, Number.POSITIVE_INFINITY),
        fbm2D(noise, Number.NaN, 2, 4),
        fbm2D(noise, 1, Number.POSITIVE_INFINITY, 4),
        fbm2D(noise, 1, 2, 4, -0.5, 2),
        fbm2D(noise, 1, 2, 4, 0.5, 0),
      ];

      for (const value of edgeCases) {
        expect(value).toBe(0);
        expect(Number.isNaN(value)).toBe(false);
        expect(Object.is(value, -0)).toBe(false);
      }
    });
  });

  describe('randomSeedString and zero Math.random() guarantee', () => {
    it('returns a non-empty string from randomSeedString()', () => {
      const generated = randomSeedString();
      expect(typeof generated).toBe('string');
      expect(generated.trim().length).toBeGreaterThan(0);
    });

    it('never calls Math.random() in any deterministic PRNG or noise function', () => {
      const randomSpy = vi.spyOn(Math, 'random');

      hashSeed('audit-seed', 'salt');
      const m = mulberry32(42);
      m();
      const rng = createSeededRng('audit-seed', 'terrain');
      rng();
      const noise = createSimplex2D(createSeededRng('audit-seed', 'noise'));
      noise(0.5, 0.5);
      fbm2D(noise, 0.5, 0.5, 4, 0.5, 2.0);

      expect(randomSpy).not.toHaveBeenCalled();
    });
  });
});
