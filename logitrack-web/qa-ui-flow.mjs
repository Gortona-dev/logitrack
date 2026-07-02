import { chromium } from "playwright";

const suffix = Date.now().toString();
const browser = await chromium.launch({
  headless: true,
  executablePath: "C:/Program Files/Google/Chrome/Application/chrome.exe",
});

const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 });
await page.goto("http://127.0.0.1:5173/", { waitUntil: "networkidle", timeout: 30000 });

async function fillForm(index, values) {
  const form = page.locator(".quick-form").nth(index);
  for (const [placeholder, value] of Object.entries(values)) {
    await form.getByPlaceholder(placeholder).fill(value);
  }
  await form.getByRole("button", { name: "Create" }).click();
  await page.waitForLoadState("networkidle");
}

await fillForm(0, {
  Name: `Cliente UI ${suffix}`,
  Email: `cliente.ui.${suffix}@email.com`,
  Document: suffix.slice(0, 11),
  Phone: "11999999999",
});

await fillForm(1, {
  Name: `Entregador UI ${suffix}`,
  Email: `entregador.ui.${suffix}@email.com`,
  Document: `9${suffix}`.slice(0, 11),
  Phone: "11988888888",
});

await fillForm(2, {
  "License plate": `UI-${suffix.slice(-4)}`,
  Brand: "Fiat",
  Model: "Fiorino",
});

const orderForm = page.locator(".quick-form").nth(3);
await orderForm.locator("select").selectOption({ label: `Cliente UI ${suffix}` });
await orderForm.getByPlaceholder("Pickup address").fill("Rua Origem UI, 100");
await orderForm.getByPlaceholder("Delivery address").fill("Rua Destino UI, 200");
await orderForm.getByPlaceholder("Description").fill("Pedido criado pelo frontend");
await orderForm.getByRole("button", { name: "Create" }).click();
await page.waitForLoadState("networkidle");

await page.locator(".detail-panel form select").nth(0).selectOption({ label: `Entregador UI ${suffix}` });
const vehicleSelect = page.locator(".detail-panel form select").nth(1);
const vehicleValue = await vehicleSelect.locator("option").evaluateAll((options, plate) => {
  const match = options.find((option) => option.textContent?.includes(String(plate)));
  return match?.getAttribute("value") ?? "";
}, `UI-${suffix.slice(-4)}`);
await vehicleSelect.selectOption(vehicleValue);
await page.locator(".detail-panel form").getByRole("button", { name: "Assign" }).click();
await page.waitForLoadState("networkidle");

for (const label of ["Picked up", "In transit", "Delivered"]) {
  await page.getByRole("button", { name: label }).click();
  await page.waitForLoadState("networkidle");
  await page.locator(".detail-route .status-pill", { hasText: label }).waitFor({ timeout: 10000 });
}

const finalStatus = await page.locator(".detail-route .status-pill").textContent();
const timelineCount = await page.locator(".timeline-item").count();
await browser.close();

console.log(JSON.stringify({ finalStatus, timelineCount }, null, 2));
