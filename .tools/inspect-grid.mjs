import { chromium } from "playwright";
const url = process.argv[2];
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newContext().then((c) => c.newPage());
  await page.goto(url, { waitUntil: "networkidle", timeout: 20000 });
  await page.waitForTimeout(1500);
  const info = await page.evaluate(() => {
    const article = document.querySelector("article");
    if (!article) return { error: "no article" };
    const grid = article.parentElement;
    const gridParent = grid?.parentElement;
    const gcs = grid && getComputedStyle(grid);
    const gpcs = gridParent && getComputedStyle(gridParent);
    return {
      grid: grid && {
        className: grid.className,
        rect: grid.getBoundingClientRect(),
        css: {
          display: gcs.display,
          height: gcs.height,
          gridAutoRows: gcs.gridAutoRows,
          gridTemplateRows: gcs.gridTemplateRows,
          alignItems: gcs.alignItems,
          overflow: gcs.overflow,
        },
      },
      gridParent: gridParent && {
        className: gridParent.className,
        rect: gridParent.getBoundingClientRect(),
        css: { height: gpcs.height, overflow: gpcs.overflow },
      },
    };
  });
  console.log(JSON.stringify(info, null, 2));
  await browser.close();
})();
