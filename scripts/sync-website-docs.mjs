#!/usr/bin/env node
// Generates the Android pages of the developer website (trustall-website) from docs/.
//
//   node scripts/sync-website-docs.mjs <path to a trustall-website checkout>
//
// docs/ is the source of truth; every file under <website>/site/docs/android/ is overwritten on
// each run. The conversion:
//   - prepends Docusaurus frontmatter (sidebar_position, title) from the PAGES table below
//   - turns "> **Note:** ..." / "> **Tip — Title:** ..." blockquotes into admonitions
//   - turns a "#### Kotlin DSL" / "#### Groovy" pair of code blocks into <Tabs>
//   - rewrites HTML that MDX rejects (<br>, <!-- -->) outside code fences
//
// A page missing from PAGES, or a website page with no source here, fails the run: a new page
// also needs a feature card on the website's landing page, and a removed one must leave its sidebar.
// The workflow in .github/workflows/sync-website-docs.yml runs this on every docs change to master.

import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { basename, dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const PAGES = {
  'getting-started': { position: 1, title: 'Getting Started' },
  'auth': { position: 2, title: 'Auth' },
  'caller-id': { position: 3, title: 'Caller ID' },
  'call-log': { position: 4, title: 'Call Log' },
  'contact': { position: 5, title: 'Contact' },
  'message-filter': { position: 6, title: 'Message Filter' },
  'number-block': { position: 7, title: 'Number Block' },
  'number-search': { position: 8, title: 'Number Search' },
  'offline-db': { position: 9, title: 'Offline DB' },
  'sms-flow': { position: 10, title: 'SMS Flow' },
  'sms-log': { position: 11, title: 'SMS Log' },
  'url-scan': { position: 12, title: 'URL Scan' },
  'number-categories': { position: 13, title: 'Number Categories' },
  'changelog': { position: 14, title: 'Release Notes' },
};

// Blockquote label -> Docusaurus admonition type.
const ADMONITIONS = { Note: 'info', Tip: 'tip', Warning: 'warning', Caution: 'danger' };

const isFence = (line) => /^\s*(```|~~~)/.test(line);

/** "> **Tip — Title:** text" blockquotes become ":::tip Title" admonitions. */
function convertAdmonitions(lines) {
  const out = [];
  let inFence = false;
  for (let i = 0; i < lines.length; ) {
    const line = lines[i];
    if (isFence(line)) inFence = !inFence;
    const m = inFence
      ? null
      : line.match(/^> \*\*(Note|Tip|Warning|Caution)(?:\s[—–-]\s(.+?))?:\*\*\s*(.*)$/);
    if (!m) {
      out.push(line);
      i++;
      continue;
    }
    const [, label, title, first] = m;
    const body = first ? [first] : [];
    for (i++; i < lines.length && /^>/.test(lines[i]); i++) {
      body.push(lines[i].replace(/^> ?/, ''));
    }
    const heading = title ? `:::${ADMONITIONS[label]} ${title.replace(/`/g, '')}` : `:::${ADMONITIONS[label]}`;
    out.push(heading, ...body, ':::');
  }
  return out;
}

/** Reads a fenced code block starting at or after `from` (skipping blank lines), or null. */
function readFencedBlock(lines, from) {
  let i = from;
  while (i < lines.length && lines[i].trim() === '') i++;
  if (i >= lines.length || !isFence(lines[i])) return null;
  const start = i;
  for (i++; i < lines.length; i++) {
    if (isFence(lines[i])) return { block: lines.slice(start, i + 1), next: i + 1 };
  }
  return null;
}

/** A "#### Kotlin DSL" code block followed by a "#### Groovy" code block becomes <Tabs>. */
function convertGradleTabs(lines) {
  const out = [];
  let used = false;
  let inFence = false;
  for (let i = 0; i < lines.length; ) {
    const line = lines[i];
    if (isFence(line)) inFence = !inFence;
    if (inFence || line.trim() !== '#### Kotlin DSL') {
      out.push(line);
      i++;
      continue;
    }
    const kotlin = readFencedBlock(lines, i + 1);
    let j = kotlin?.next ?? -1;
    while (j >= 0 && j < lines.length && lines[j].trim() === '') j++;
    const groovy = j >= 0 && lines[j]?.trim() === '#### Groovy' ? readFencedBlock(lines, j + 1) : null;
    if (!kotlin || !groovy) {
      out.push(line);
      i++;
      continue;
    }
    used = true;
    out.push(
      '<Tabs groupId="gradle-dsl">',
      '<TabItem value="kotlin" label="Kotlin DSL">',
      '',
      ...kotlin.block,
      '',
      '</TabItem>',
      '<TabItem value="groovy" label="Groovy">',
      '',
      ...groovy.block,
      '',
      '</TabItem>',
      '</Tabs>',
    );
    i = groovy.next;
  }
  return { lines: out, used };
}

/** MDX rejects void tags without a slash and HTML comments; fix both outside code fences. */
function fixMdxHtml(lines) {
  let inFence = false;
  return lines.map((line) => {
    if (isFence(line)) inFence = !inFence;
    if (inFence) return line;
    return line.replace(/<br>/g, '<br />').replace(/<!--(.*?)-->/g, '{/*$1*/}');
  });
}

function convert(name, source) {
  const page = PAGES[name];
  if (!page) {
    throw new Error(`docs/${name}.md has no entry in PAGES; add it there and a feature card on the website`);
  }
  let lines = source.replace(/\r\n/g, '\n').replace(/\n+$/, '').split('\n');
  lines = convertAdmonitions(lines);
  const tabs = convertGradleTabs(lines);
  lines = fixMdxHtml(tabs.lines);

  const head = ['---', `sidebar_position: ${page.position}`, `title: ${page.title}`, '---', ''];
  if (tabs.used) head.push("import Tabs from '@theme/Tabs';", "import TabItem from '@theme/TabItem';", '');
  return [...head, ...lines, ''].join('\n');
}

function main() {
  const website = process.argv[2] && resolve(process.argv[2]);
  const targetDir = website && join(website, 'site', 'docs', 'android');
  if (!targetDir || !existsSync(targetDir) || !statSync(targetDir).isDirectory()) {
    console.error('Usage: node scripts/sync-website-docs.mjs <path to a trustall-website checkout>');
    process.exit(2);
  }
  const sourceDir = resolve(dirname(fileURLToPath(import.meta.url)), '..', 'docs');

  const sources = readdirSync(sourceDir).filter((f) => f.endsWith('.md')).sort();
  const sourceNames = new Set(sources.map((f) => basename(f, '.md')));
  const orphans = readdirSync(targetDir)
    .filter((f) => f.endsWith('.md') && !sourceNames.has(basename(f, '.md')));
  if (orphans.length) {
    throw new Error(`website pages with no source in docs/: ${orphans.join(', ')}; delete them and update the website sidebar`);
  }

  let changed = 0;
  for (const file of sources) {
    const output = convert(basename(file, '.md'), readFileSync(join(sourceDir, file), 'utf8'));
    const target = join(targetDir, file);
    if (existsSync(target) && readFileSync(target, 'utf8') === output) continue;
    writeFileSync(target, output);
    changed++;
    console.log(`updated ${file}`);
  }
  console.log(`${sources.length} pages, ${changed} changed`);
}

main();
