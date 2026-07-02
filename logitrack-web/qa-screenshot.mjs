import { chromium } from "playwright";

const browser = await chromium.launch({
  headless: true,
  executablePath: "C:/Program Files/Google/Chrome/Application/chrome.exe",
});

const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 });
const consoleErrors = [];
const requestFailures = [];

page.on("console", (message) => {
  if (message.type() === "error") {
    consoleErrors.push(message.text());
  }
});

page.on("requestfailed", (request) => {
  requestFailures.push(`${request.url()} ${request.failure()?.errorText ?? ""}`.trim());
});

await page.goto("http://127.0.0.1:5173/", { waitUntil: "networkidle", timeout: 30000 });
await page.screenshot({ path: "qa-desktop.png", fullPage: true });

await page.setViewportSize({ width: 390, height: 900 });
await page.reload({ waitUntil: "networkidle", timeout: 30000 });
await page.screenshot({ path: "qa-mobile.png", fullPage: true });

await browser.close();

console.log(JSON.stringify({ consoleErrors, requestFailures }, null, 2));
