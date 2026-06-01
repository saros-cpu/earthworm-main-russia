import { chromium } from "playwright";

const url = process.argv[2] || "http://localhost:3000/course-pack";

(async () => {
  const browser = await chromium.launch();
  const ctx = await browser.newContext();
  const page = await ctx.newPage();

  page.on("console", (msg) => {
    console.log(`[CONSOLE ${msg.type()}] ${msg.text()}`);
  });
  page.on("pageerror", (err) => {
    console.log(`[PAGEERROR] ${err.message}\n${err.stack}`);
  });
  page.on("requestfailed", (req) => {
    console.log(`[REQ-FAIL] ${req.method()} ${req.url()} -> ${req.failure()?.errorText}`);
  });
  page.on("response", async (res) => {
    if (res.status() >= 400) {
      console.log(`[HTTP ${res.status()}] ${res.url()}`);
    }
  });

  console.log(`>>> navigate to ${url}`);
  try {
    await page.goto(url, { waitUntil: "networkidle", timeout: 20000 });
  } catch (e) {
    console.log(`[GOTO-ERR] ${e.message}`);
  }
  await page.waitForTimeout(2000);

  const text = (await page.evaluate(() => document.body.innerText)) || "";
  console.log("\n----- BODY INNER TEXT (first 1500 chars) -----");
  console.log(text.slice(0, 1500));
  console.log("----- END -----\n");

  const cardCount = await page.evaluate(() =>
    document.querySelectorAll("article").length,
  );
  console.log(`<article> elements: ${cardCount}`);

  await page.screenshot({ path: ".tools/page.png", fullPage: true });
  console.log("screenshot saved -> .tools/page.png");

  await browser.close();
})();
