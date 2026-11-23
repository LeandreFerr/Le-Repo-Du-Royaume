import { expect, test } from '@playwright/test';
import { attachErrorGuards, type ErrorGuard, waitForAppReady } from './helpers/map-test-helpers';

test.describe('royaume_front smoke checks', () => {
  let guard: ErrorGuard;

  test.beforeEach(async ({ page }) => {
    guard = attachErrorGuards(page);
  });

  test.afterEach(() => {
    guard.assertClean();
    guard.detach();
  });

  test('boots clean Angular base without rendering the map component on launch', async ({
    page,
  }) => {
    const response = await page.goto('/');
    expect(response).not.toBeNull();
    expect(response?.ok()).toBe(true);

    await waitForAppReady(page, { requireMapCanvas: false });

    const title = await page.title();
    expect(title.trim().length).toBeGreaterThan(0);

    const appRoot = page.locator('app-root');
    await expect(appRoot).toBeVisible();
    await expect(appRoot.locator('h1')).toContainText('Le Royaume');
    await expect(page.locator('app-carte-royaume')).toHaveCount(0);
    await expect(page.locator('app-carte-royaume-sandbox')).toHaveCount(0);
  });

  test('contains zero <img> tags and zero external CSS background-image URLs', async ({ page }) => {
    await page.goto('/');
    await waitForAppReady(page, { requireMapCanvas: false });

    await expect(page.locator('img')).toHaveCount(0);

    const externalBackgroundUrls = await page.evaluate(() => {
      const violations: string[] = [];
      const elements = document.querySelectorAll('*');
      for (const el of elements) {
        const bg = getComputedStyle(el).backgroundImage;
        if (!bg || bg === 'none') {
          continue;
        }
        const matches = bg.matchAll(/url\(\s*['"]?([^'")]+)['"]?\s*\)/gi);
        for (const match of matches) {
          const ref = (match[1] ?? '').trim();
          if (ref.length > 0 && !ref.startsWith('#')) {
            violations.push(`${el.tagName.toLowerCase()}: ${ref}`);
          }
        }
      }
      return violations;
    });

    expect(externalBackgroundUrls).toEqual([]);
    expect(guard.externalRequests).toEqual([]);
    expect(guard.unexpectedImageRequests).toEqual([]);
  });

  test('remains visible and error-free across desktop and narrow viewport resizes', async ({
    page,
  }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/');
    await waitForAppReady(page, { requireMapCanvas: false });

    await page.setViewportSize({ width: 800, height: 600 });
    await waitForAppReady(page, { requireMapCanvas: false });

    const box = await page.locator('app-root').boundingBox();
    expect(box).not.toBeNull();
    expect(box?.width ?? 0).toBeGreaterThan(0);
    expect(box?.height ?? 0).toBeGreaterThan(0);
  });
});
