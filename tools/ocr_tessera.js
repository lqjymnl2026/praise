// OCR via tesseract.js（Node，离线模型在 data/tessdata）
// 用法: node ocr_tessera.js <图片路径> [PSM模式，默认4]
const path = require("path");
const { createWorker } = require("tesseract.js");
const ROOT = path.resolve(__dirname, "..");
const langPath = path.join(ROOT, "data", "tessdata");
const img = process.argv[2];
const psm = process.argv[3] || "4";
if (!img) { console.error("usage: ocr_tessera.js <image> [psm]"); process.exit(2); }
(async () => {
  const worker = await createWorker("chi_sim+eng", 1, {
    langPath,
    gzip: true,
    cachePath: path.join(ROOT, "data", "tessdata", ".cache"),
    corePath: path.join(ROOT, "node_modules", "tesseract.js-core"),
    logger: () => {},
  });
  await worker.setParameters({ tessedit_pageseg_mode: psm });
  const { data } = await worker.recognize(img);
  process.stdout.write(data.text || "");
  await worker.terminate();
})().catch((e) => { console.error("OCR_FAIL:", e && e.message); process.exit(1); });
