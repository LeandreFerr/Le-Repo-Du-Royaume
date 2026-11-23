import { createNoise2D, type NoiseFunction2D } from 'simplex-noise';

function normalizeSeed(seed: string | number): string {
  if (typeof seed === 'number') {
    return Object.is(seed, -0) ? '0' : String(seed);
  }
  return String(seed ?? '');
}

/**
 * Deterministic 32-bit unsigned seed hash (`xmur3`) with collision-proof salt encoding.
 */
export function hashSeed(seed: string | number, salt?: string): number {
  const base = normalizeSeed(seed);
  const input =
    salt !== undefined && salt.length > 0 ? `${base.length}:${base}\0${salt}` : base;

  let h = 1779033703 ^ input.length;
  for (let i = 0; i < input.length; i++) {
    h = Math.imul(h ^ input.charCodeAt(i), 3432918353);
    h = (h << 13) | (h >>> 19);
  }
  h = Math.imul(h ^ (h >>> 16), 2246822507);
  h = Math.imul(h ^ (h >>> 13), 3266489909);
  return (h ^ (h >>> 16)) >>> 0;
}

/**
 * Mulberry32 deterministic PRNG returning floats strictly in `[0, 1)`.
 */
export function mulberry32(seedInt: number): () => number {
  let state = Number.isFinite(seedInt) ? seedInt >>> 0 : 0;
  return (): number => {
    state = (state + 0x6d2b79f5) | 0;
    let t = Math.imul(state ^ (state >>> 15), 1 | state);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/**
 * Creates an isolated deterministic PRNG stream from a seed and optional domain salt.
 */
export function createSeededRng(seed: string | number, salt?: string): () => number {
  return mulberry32(hashSeed(seed, salt));
}

/**
 * Creates a deterministic 2D Simplex noise function (`[-1, 1]`) backed by the given PRNG.
 */
export function createSimplex2D(rng: () => number): (x: number, y: number) => number {
  if (typeof rng !== 'function') {
    throw new TypeError('createSimplex2D requires a seeded rng function.');
  }
  const noise2D: NoiseFunction2D = createNoise2D(rng);
  return noise2D;
}

/**
 * Multi-octave Fractional Brownian Motion (fBm) normalized to `[-1, 1]`.
 */
export function fbm2D(
  noise: (x: number, y: number) => number,
  x: number,
  y: number,
  octaves = 4,
  persistence = 0.5,
  lacunarity = 2.0,
): number {
  if (
    typeof noise !== 'function' ||
    !Number.isFinite(x) ||
    !Number.isFinite(y) ||
    !Number.isFinite(octaves) ||
    !Number.isFinite(persistence) ||
    persistence < 0 ||
    !Number.isFinite(lacunarity) ||
    lacunarity <= 0
  ) {
    return 0;
  }

  const count = Math.floor(octaves);
  if (count <= 0) {
    return 0;
  }

  let total = 0;
  let amplitude = 1;
  let frequency = 1;
  let maxAmplitude = 0;

  for (let i = 0; i < count; i++) {
    const sample = noise(x * frequency, y * frequency);
    if (!Number.isFinite(sample)) {
      return 0;
    }
    total += sample * amplitude;
    maxAmplitude += amplitude;
    amplitude *= persistence;
    frequency *= lacunarity;
  }

  if (maxAmplitude <= 0) {
    return 0;
  }

  const normalized = total / maxAmplitude;
  return normalized === 0 ? 0 : Math.max(-1, Math.min(1, normalized));
}

/**
 * UI-only helper generating a fresh random seed string for the sandbox "Aléatoire" button.
 * Never used inside deterministic map generation or placement functions.
 */
export function randomSeedString(): string {
  let value: number;
  if (typeof globalThis.crypto?.getRandomValues === 'function') {
    const buf = new Uint32Array(1);
    globalThis.crypto.getRandomValues(buf);
    value = buf[0] ?? 0;
  } else {
    value = Math.floor(Math.random() * 0x100000000);
  }
  return `royaume-${value.toString(16).padStart(8, '0')}`;
}
