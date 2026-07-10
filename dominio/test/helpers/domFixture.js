import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const INDEX_HTML = fs.readFileSync(path.resolve(__dirname, '../../index.html'), 'utf-8');

/** Mounts the real index.html body into the jsdom document for a test. */
export function mountFixture() {
  const bodyMatch = INDEX_HTML.match(/<body[^>]*>([\s\S]*)<\/body>/);
  document.body.innerHTML = bodyMatch[1].replace(/<script[\s\S]*?<\/script>/g, '');
}
