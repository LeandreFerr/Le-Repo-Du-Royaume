import fs from 'node:fs';
import path from 'node:path';
import { chromium, defineConfig, devices } from '@playwright/test';

const PORT = process.env['E2E_PORT'] ?? '4299';
const BASE_URL = process.env['E2E_BASE_URL'] ?? `http://127.0.0.1:${PORT}`;
const IS_CI = Boolean(process.env['CI']);
const IS_HEADED = process.argv.includes('--headed') || process.env['PW_HEADED'] === '1';
const SKIP_WEBSERVER = process.env['E2E_SKIP_WEBSERVER'] === '1';

function listRevisionDirs(browsersDir: string, prefix: string): string[] {
  if (!browsersDir || !fs.existsSync(browsersDir)) {
    return [];
  }
  const pattern = new RegExp(`^${prefix}(\\d+)$`);
  return fs
    .readdirSync(browsersDir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => {
      const match = pattern.exec(entry.name);
      return match ? { name: entry.name, rev: Number(match[1]) } : null;
    })
    .filter((item): item is { name: string; rev: number } => item !== null)
    .sort((a, b) => b.rev - a.rev)
    .map((item) => path.join(browsersDir, item.name));
}

function resolveChromiumExecutablePath(isHeaded: boolean): string | undefined {
  const checkedPaths: string[] = [];

  const envExecutable = process.env['PLAYWRIGHT_EXECUTABLE_PATH'];
  if (envExecutable) {
    checkedPaths.push(envExecutable);
    if (fs.existsSync(envExecutable)) {
      return envExecutable;
    }
  }

  try {
    const defaultExecutable = chromium.executablePath();
    checkedPaths.push(defaultExecutable);
    if (defaultExecutable && fs.existsSync(defaultExecutable)) {
      return undefined;
    }
  } catch {
    // Playwright default binary lookup failed; fall back to local cache / system browsers.
  }

  const browsersPath =
    process.env['PLAYWRIGHT_BROWSERS_PATH'] ??
    path.join(process.env['LOCALAPPDATA'] ?? '', 'ms-playwright');

  if (!isHeaded) {
    for (const dir of listRevisionDirs(browsersPath, 'chromium_headless_shell-')) {
      const candidate = path.join(dir, 'chrome-win', 'headless_shell.exe');
      checkedPaths.push(candidate);
      if (fs.existsSync(candidate)) {
        return candidate;
      }
    }
  }

  for (const dir of listRevisionDirs(browsersPath, 'chromium-')) {
    const candidate = path.join(dir, 'chrome-win', 'chrome.exe');
    checkedPaths.push(candidate);
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  const localAppData = process.env['LOCALAPPDATA'] ?? '';
  const systemChromeCandidates = [
    'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
    ...(localAppData
      ? [path.join(localAppData, 'Google', 'Chrome', 'Application', 'chrome.exe')]
      : []),
  ];

  for (const candidate of systemChromeCandidates) {
    checkedPaths.push(candidate);
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  const systemEdgeCandidates = [
    'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe',
  ];

  for (const candidate of systemEdgeCandidates) {
    checkedPaths.push(candidate);
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  throw new Error(
    [
      'Unable to resolve an offline Chromium/Chrome/Edge executable for Playwright.',
      'Checked paths:',
      ...checkedPaths.map((p) => `  - ${p}`),
      'Set PLAYWRIGHT_EXECUTABLE_PATH to a valid local browser binary.',
    ].join('\n'),
  );
}

const resolvedExecutablePath = resolveChromiumExecutablePath(IS_HEADED);
const isMsPlaywrightFullChrome =
  resolvedExecutablePath !== undefined &&
  /ms-playwright[\\/]chromium-\d+[\\/]chrome-win[\\/]chrome\.exe$/i.test(resolvedExecutablePath);

const launchArgs = isMsPlaywrightFullChrome ? ['--no-sandbox'] : [];
const launchOptions = {
  ...(resolvedExecutablePath ? { executablePath: resolvedExecutablePath } : {}),
  ...(launchArgs.length > 0 ? { args: launchArgs } : {}),
};

const webServerEnv: Record<string, string> = {};
for (const [key, value] of Object.entries(process.env)) {
  if (value !== undefined) {
    webServerEnv[key] = value;
  }
}
webServerEnv['PORT'] = PORT;
webServerEnv['NG_CLI_ANALYTICS'] = 'false';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  forbidOnly: IS_CI,
  retries: IS_CI ? 1 : 0,
  timeout: 30_000,
  expect: {
    timeout: 10_000,
  },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: BASE_URL,
    headless: !IS_HEADED,
    viewport: { width: 1440, height: 900 },
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    launchOptions,
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions,
      },
    },
  ],
  ...(SKIP_WEBSERVER
    ? {}
    : {
        webServer: {
          command: `npx ng serve --port ${PORT} --host 127.0.0.1`,
          cwd: __dirname,
          url: BASE_URL,
          reuseExistingServer: !IS_CI,
          timeout: 120_000,
          stdout: 'pipe',
          stderr: 'pipe',
          env: webServerEnv,
        },
      }),
});
