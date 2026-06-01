import { chromium } from "playwright";
const url = process.argv[2] || "http://localhost:3000/course-pack";
const tabLabel = process.argv[3] || "TORFL";

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newContext().then((c) => c.newPage());
  page.on("pageerror", (e) => console.log(`[PAGEERROR] ${e.message}`));
  await page.goto(url, { waitUntil: "networkidle", timeout: 20000 });
  await page.waitForTimeout(1200);

  const tabs = await page.locator("nav button").all();
  let clicked = false;
  for (const tab of tabs) {
    const text = (await tab.textContent()) || "";
    if (text.includes(tabLabel)) {
      console.log(`>>> clicking tab: ${text.trim()}`);
      await tab.click();
      clicked = true;
      break;
    }
  }
  if (!clicked) console.log(`!!! tab containing "${tabLabel}" not found`);

  await page.waitForTimeout(800);

  const text = (await page.evaluate(() => document.body.innerText)) || "";
  console.log("\n----- BODY (1500) -----");
  console.log(text.slice(0, 1500));
  console.log("----- END -----\n");

  const cardCount = await page.evaluate(() => document.querySelectorAll("article").length);
  console.log(`articles: ${cardCount}`);

  await page.screenshot({ path: ".tools/page.png", fullPage: true });
  await browser.close();
})();
