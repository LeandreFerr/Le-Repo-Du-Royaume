import { expect, type Locator, type Page } from '@playwright/test';

const DEFAULT_PORT = process.env['E2E_PORT'] ?? '4299';
export const DEFAULT_BASE_URL = process.env['E2E_BASE_URL'] ?? `http://127.0.0.1:${DEFAULT_PORT}`;

export interface ErrorGuardOptions {
  allowedOrigins?: readonly string[];
  allowSameOriginFavicon?: boolean;
}

export interface ErrorGuard {
  readonly errors: string[];
  readonly consoleErrors: string[];
  readonly pageErrors: string[];
  readonly failedRequests: string[];
  readonly externalRequests: string[];
  readonly unexpectedImageRequests: string[];
  assertClean(): void;
  detach(): void;
}

export interface WaitForAppReadyOptions {
  requireMapCanvas?: boolean;
  timeoutMs?: number;
}

export interface ProjectedScreenPoint {
  screenX: number;
  screenY: number;
}

export interface CanvasStats {
  width: number;
  height: number;
  nonBackgroundPixels: number;
  uniqueColorBuckets: number;
  signature: string;
}

export interface CanvasPoint {
  x: number;
  y: number;
}

function isProjectedScreenPoint(value: unknown): value is ProjectedScreenPoint {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate['screenX'] === 'number' &&
    Number.isFinite(candidate['screenX']) &&
    typeof candidate['screenY'] === 'number' &&
    Number.isFinite(candidate['screenY'])
  );
}

export function attachErrorGuards(page: Page, options: ErrorGuardOptions = {}): ErrorGuard {
  const defaultOrigin = new URL(DEFAULT_BASE_URL).origin;
  const allowedOrigins = new Set<string>(options.allowedOrigins ?? [defaultOrigin]);
  const allowFavicon = options.allowSameOriginFavicon ?? true;

  const errors: string[] = [];
  const consoleErrors: string[] = [];
  const pageErrors: string[] = [];
  const failedRequests: string[] = [];
  const externalRequests: string[] = [];
  const unexpectedImageRequests: string[] = [];

  const onConsole = (msg: { type(): string; text(): string }): void => {
    if (msg.type() === 'error') {
      const entry = `[console.error] ${msg.text()}`;
      consoleErrors.push(entry);
      errors.push(entry);
    }
  };

  const onPageError = (err: Error): void => {
    const entry = `[pageerror] ${err.message}`;
    pageErrors.push(entry);
    errors.push(entry);
  };

  const onRequestFailed = (req: {
    url(): string;
    resourceType(): string;
    failure(): { errorText: string } | null;
  }): void => {
    const reason = req.failure()?.errorText ?? 'unknown failure';
    const entry = `[requestfailed] (${req.resourceType()}) ${req.url()} -> ${reason}`;
    failedRequests.push(entry);
    errors.push(entry);
  };

  const onRequest = (req: { url(): string; resourceType(): string }): void => {
    const rawUrl = req.url();
    if (
      rawUrl.startsWith('data:') ||
      rawUrl.startsWith('blob:') ||
      rawUrl.startsWith('about:') ||
      rawUrl.startsWith('chrome-error:')
    ) {
      return;
    }
    let parsed: URL;
    try {
      parsed = new URL(rawUrl);
    } catch {
      return;
    }
    if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
      return;
    }
    const isAllowedOrigin = allowedOrigins.has(parsed.origin);
    if (!isAllowedOrigin) {
      const entry = `[external-request] (${req.resourceType()}) ${rawUrl}`;
      externalRequests.push(entry);
      errors.push(entry);
    }
    if (req.resourceType() === 'image') {
      const isAllowedFavicon =
        allowFavicon && isAllowedOrigin && parsed.pathname === '/favicon.ico';
      if (!isAllowedFavicon) {
        const entry = `[unexpected-image-request] ${rawUrl}`;
        unexpectedImageRequests.push(entry);
        errors.push(entry);
      }
    }
  };

  page.on('console', onConsole);
  page.on('pageerror', onPageError);
  page.on('requestfailed', onRequestFailed);
  page.on('request', onRequest);

  return {
    errors,
    consoleErrors,
    pageErrors,
    failedRequests,
    externalRequests,
    unexpectedImageRequests,
    assertClean(): void {
      expect(errors, `Expected zero runtime/network errors, got:\n${errors.join('\n')}`).toEqual(
        [],
      );
    },
    detach(): void {
      page.off('console', onConsole);
      page.off('pageerror', onPageError);
      page.off('requestfailed', onRequestFailed);
      page.off('request', onRequest);
    },
  };
}

export async function waitForAppReady(
  page: Page,
  options: WaitForAppReadyOptions = {},
): Promise<void> {
  const requireMapCanvas = options.requireMapCanvas ?? true;
  const timeout = options.timeoutMs ?? 15_000;

  const appRoot = page.locator('app-root');
  await appRoot.waitFor({ state: 'visible', timeout });

  await expect
    .poll(
      async () =>
        page.evaluate(() => {
          const root = document.querySelector('app-root');
          if (!root) {
            return false;
          }
          const rect = root.getBoundingClientRect();
          return (
            root.children.length > 0 &&
            (root.textContent ?? '').trim().length > 0 &&
            rect.width > 0 &&
            rect.height > 0
          );
        }),
      { timeout, message: 'Expected <app-root> to render non-empty visible content' },
    )
    .toBe(true);

  if (!requireMapCanvas) {
    return;
  }

  const canvas = page.locator('[data-testid="map-canvas"]');
  await canvas.waitFor({ state: 'visible', timeout });

  await expect
    .poll(
      async () =>
        canvas.evaluate((el) => {
          if (!(el instanceof HTMLCanvasElement)) {
            return false;
          }
          const rect = el.getBoundingClientRect();
          if (rect.width <= 0 || rect.height <= 0 || el.width <= 0 || el.height <= 0) {
            return false;
          }
          const raw = el.getAttribute('data-projected-points');
          if (!raw) {
            return false;
          }
          try {
            const parsed = JSON.parse(raw) as Record<string, unknown>;
            const market = parsed['market'] as Record<string, unknown> | undefined;
            return (
              typeof market === 'object' &&
              market !== null &&
              typeof market['screenX'] === 'number' &&
              Number.isFinite(market['screenX']) &&
              typeof market['screenY'] === 'number' &&
              Number.isFinite(market['screenY'])
            );
          } catch {
            return false;
          }
        }),
      {
        timeout,
        message: 'Expected [data-testid="map-canvas"] to be sized and projected',
      },
    )
    .toBe(true);
}

export async function readProjectedPoints(
  page: Page,
): Promise<Record<string, ProjectedScreenPoint>> {
  const canvas = page.locator('[data-testid="map-canvas"]');
  await canvas.waitFor({ state: 'visible' });

  let parsedMap: Record<string, ProjectedScreenPoint> | null = null;

  await expect
    .poll(
      async () => {
        const raw = await canvas.getAttribute('data-projected-points');
        if (!raw) {
          return false;
        }
        try {
          const parsed: unknown = JSON.parse(raw);
          if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) {
            return false;
          }
          const result: Record<string, ProjectedScreenPoint> = {};
          for (const [key, val] of Object.entries(parsed)) {
            if (!isProjectedScreenPoint(val)) {
              return false;
            }
            result[key] = { screenX: val.screenX, screenY: val.screenY };
          }
          parsedMap = result;
          return Object.keys(result).length > 0;
        } catch {
          return false;
        }
      },
      { message: 'Expected valid JSON in data-projected-points attribute' },
    )
    .toBe(true);

  if (!parsedMap) {
    throw new Error('data-projected-points did not resolve to a valid coordinate map');
  }
  return parsedMap;
}

export async function sampleCanvasStats(page: Page): Promise<CanvasStats> {
  const canvas = page.locator('[data-testid="map-canvas"]');
  await canvas.waitFor({ state: 'visible' });

  return canvas.evaluate((el) => {
    if (!(el instanceof HTMLCanvasElement)) {
      throw new Error('[data-testid="map-canvas"] is not an HTMLCanvasElement');
    }
    const { width, height } = el;
    if (width <= 0 || height <= 0) {
      throw new Error(`Canvas has non-positive backing dimensions: ${width}x${height}`);
    }
    const ctx = el.getContext('2d', { willReadFrequently: true });
    if (!ctx) {
      throw new Error('Unable to acquire 2D context from [data-testid="map-canvas"]');
    }

    const imageData = ctx.getImageData(0, 0, width, height).data;
    const bgR = imageData[0] ?? 0;
    const bgG = imageData[1] ?? 0;
    const bgB = imageData[2] ?? 0;
    const bgA = imageData[3] ?? 0;

    const step = Math.max(1, Math.floor(Math.max(width, height) / 180));
    const buckets = new Set<number>();
    let nonBackgroundPixels = 0;
    let hash = 0x811c9dc5;

    for (let y = 0; y < height; y += step) {
      const rowOffset = y * width;
      for (let x = 0; x < width; x += step) {
        const idx = (rowOffset + x) * 4;
        const r = imageData[idx] ?? 0;
        const g = imageData[idx + 1] ?? 0;
        const b = imageData[idx + 2] ?? 0;
        const a = imageData[idx + 3] ?? 0;

        const isBackground =
          a === 0 ||
          (Math.abs(r - bgR) <= 4 &&
            Math.abs(g - bgG) <= 4 &&
            Math.abs(b - bgB) <= 4 &&
            Math.abs(a - bgA) <= 4);

        if (!isBackground) {
          nonBackgroundPixels += 1;
        }

        const qR = r >> 4;
        const qG = g >> 4;
        const qB = b >> 4;
        buckets.add((qR << 8) | (qG << 4) | qB);

        hash ^= (qR << 4) | qG;
        hash = Math.imul(hash, 0x01000193);
        hash ^= (qB << 4) | (a >> 4);
        hash = Math.imul(hash, 0x01000193);
      }
    }

    return {
      width,
      height,
      nonBackgroundPixels,
      uniqueColorBuckets: buckets.size,
      signature: `${width}x${height}:${(hash >>> 0).toString(16).padStart(8, '0')}`,
    };
  });
}

export async function waitForCanvasFrameChange(page: Page, timeoutMs = 3000): Promise<boolean> {
  const canvas = page.locator('[data-testid="map-canvas"]');
  await canvas.waitFor({ state: 'visible' });

  return canvas.evaluate((el, maxWaitMs) => {
    if (!(el instanceof HTMLCanvasElement)) {
      throw new Error('[data-testid="map-canvas"] is not an HTMLCanvasElement');
    }
    const ctx = el.getContext('2d', { willReadFrequently: true });
    if (!ctx) {
      throw new Error('Unable to acquire 2D context from [data-testid="map-canvas"]');
    }

    const computeRawHash = (): string => {
      const { width, height } = el;
      if (width <= 0 || height <= 0) {
        return 'empty';
      }
      const data = ctx.getImageData(0, 0, width, height).data;
      const step = Math.max(1, Math.floor(Math.max(width, height) / 140));
      let hash = 0x811c9dc5;
      for (let y = 0; y < height; y += step) {
        const row = y * width;
        for (let x = 0; x < width; x += step) {
          const idx = (row + x) * 4;
          hash ^= data[idx] ?? 0;
          hash = Math.imul(hash, 0x01000193);
          hash ^= data[idx + 1] ?? 0;
          hash = Math.imul(hash, 0x01000193);
          hash ^= data[idx + 2] ?? 0;
          hash = Math.imul(hash, 0x01000193);
          hash ^= data[idx + 3] ?? 0;
          hash = Math.imul(hash, 0x01000193);
        }
      }
      return (hash >>> 0).toString(16);
    };

    const baseline = computeRawHash();
    const start = performance.now();

    return new Promise<boolean>((resolve) => {
      const tick = (): void => {
        const current = computeRawHash();
        if (current !== baseline) {
          resolve(true);
          return;
        }
        if (performance.now() - start >= maxWaitMs) {
          resolve(false);
          return;
        }
        requestAnimationFrame(tick);
      };
      requestAnimationFrame(tick);
    });
  }, timeoutMs);
}

export async function dragCanvasPoint(
  page: Page,
  from: CanvasPoint,
  to: CanvasPoint,
  button: 'left' | 'right' = 'left',
): Promise<void> {
  const canvas = page.locator('[data-testid="map-canvas"]');
  await canvas.waitFor({ state: 'visible' });
  await canvas.scrollIntoViewIfNeeded();

  const box = await canvas.boundingBox();
  if (!box || box.width <= 0 || box.height <= 0) {
    throw new Error('Cannot drag on [data-testid="map-canvas"]: boundingBox is empty');
  }

  const startX = box.x + from.x;
  const startY = box.y + from.y;
  const endX = box.x + to.x;
  const endY = box.y + to.y;

  await page.mouse.move(startX, startY);
  await page.mouse.down({ button });
  await page.mouse.move(endX, endY, { steps: 12 });
  await page.mouse.up({ button });
}

export async function fillInputAndCommit(locator: Locator, value: string): Promise<void> {
  await locator.waitFor({ state: 'visible' });
  const tagName = await locator.evaluate((el) => el.tagName.toLowerCase());
  if (tagName === 'select') {
    await locator.selectOption(value);
    return;
  }
  await locator.fill(value);
  await locator.blur();
}

export async function readInputValue(locator: Locator): Promise<string> {
  await locator.waitFor({ state: 'visible' });
  return locator.inputValue();
}
