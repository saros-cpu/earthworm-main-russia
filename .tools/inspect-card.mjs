import { chromium } from "playwright";

const url = process.argv[2];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newContext().then((c) => c.newPage());
  await page.goto(url, { waitUntil: "networkidle", timeout: 20000 });
  await page.waitForTimeout(1500);

  const info = await page.evaluate(() => {
    const article = document.querySelector("article");
    if (!article) return { error: "no article found" };
    const cs = getComputedStyle(article);
    const rect = article.getBoundingClientRect();
    const h3 = article.querySelector("h3");
    const p = article.querySelector("p");
    const result = {
      article: {
        outerHTML: article.outerHTML.slice(0, 2000),
        rect: { w: rect.width, h: rect.height },
        css: { display: cs.display, minHeight: cs.minHeight, height: cs.height, padding: cs.padding, overflow: cs.overflow },
      },
    };
    if (h3) {
      const hs = getComputedStyle(h3);
      const hr = h3.getBoundingClientRect();
      result.title = {
        text: h3.textContent,
        rect: { w: hr.width, h: hr.height },
        css: {
          display: hs.display,
          fontSize: hs.fontSize,
          lineHeight: hs.lineHeight,
          fontFamily: hs.fontFamily,
          webkitLineClamp: hs.webkitLineClamp,
          overflow: hs.overflow,
          textOverflow: hs.textOverflow,
        },
      };
    }
    if (p) {
      const ps = getComputedStyle(p);
      const pr = p.getBoundingClientRect();
      result.desc = {
        text: p.textContent.slice(0, 80),
        rect: { w: pr.width, h: pr.height },
        css: {
          display: ps.display,
          fontSize: ps.fontSize,
          lineHeight: ps.lineHeight,
          webkitLineClamp: ps.webkitLineClamp,
          flex: ps.flex,
          overflow: ps.overflow,
        },
      };
    }
    return result;
  });

  console.log(JSON.stringify(info, null, 2));
  await browser.close();
})();
