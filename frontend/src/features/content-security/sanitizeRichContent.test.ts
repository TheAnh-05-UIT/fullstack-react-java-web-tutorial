import { describe, expect, it } from 'vitest';

import {
  isLegacyFullHtmlDocument,
  parseAndSanitizeMarkdown,
  sanitizeRichContentHtml,
} from './sanitizeRichContent';

const parseDocument = (html: string): Document =>
  new DOMParser().parseFromString(html, 'text/html');

const hasEventHandlerAttribute = (document: Document): boolean =>
  Array.from(document.querySelectorAll('*')).some((element) =>
    element
      .getAttributeNames()
      .some((attributeName) => attributeName.toLowerCase().startsWith('on')),
  );

describe('isLegacyFullHtmlDocument', () => {
  it.each([
    '\n\t<!DoCtYpE html><HTML><body>Legacy</body></HTML>',
    '  <HtMl lang="en"><body>Legacy</body></HtMl>',
  ])('detects a full HTML document after leading whitespace', (content) => {
    expect(isLegacyFullHtmlDocument(content)).toBe(true);
  });

  it.each([
    '# Markdown heading',
    '<h2>HTML fragment</h2>',
    '<html-preview>Not an HTML document</html-preview>',
  ])('does not classify Markdown or HTML fragments as full documents', (content) => {
    expect(isLegacyFullHtmlDocument(content)).toBe(false);
  });
});

describe('sanitizeRichContentHtml', () => {
  it('removes executable, embedded, form, metadata, and foreign-content elements', () => {
    const sanitized = sanitizeRichContentHtml(`
      <script>alert(1)</script>
      <iframe src="https://evil.example"></iframe>
      <object data="https://evil.example"></object>
      <embed src="https://evil.example">
      <form action="/steal">
        <input name="secret">
        <button type="submit">Submit</button>
        <textarea>Secret</textarea>
        <select><option>Option</option></select>
      </form>
      <meta http-equiv="refresh" content="0;url=https://evil.example">
      <base href="https://evil.example/">
      <link rel="stylesheet" href="https://evil.example/style.css">
      <svg onload="alert(1)"><circle></circle></svg>
      <math><mtext>MathML</mtext></math>
      <style>body { background-image: url("javascript:alert(1)") }</style>
      <p>Safe paragraph</p>
    `);
    const document = parseDocument(sanitized);

    expect(
      document.querySelector(
        'script, iframe, object, embed, form, input, button, textarea, select, option, meta, base, link, svg, math, style',
      ),
    ).toBeNull();
    expect(document.querySelector('p')?.textContent).toContain('Safe paragraph');
  });

  it('removes event handlers and inline styles while preserving allowed elements', () => {
    const sanitized = sanitizeRichContentHtml(`
      <div class="article" onclick="alert(1)" onmouseover="alert(2)" style="color: red">
        Safe text
      </div>
      <img
        src="/uploads/safe.png"
        alt="safe"
        onerror="alert(3)"
        style="background-image: url(javascript:alert(4))"
      >
    `);
    const document = parseDocument(sanitized);

    expect(document.querySelector('div.article')?.textContent).toContain('Safe text');
    expect(document.querySelector('img[alt="safe"]')).not.toBeNull();
    expect(hasEventHandlerAttribute(document)).toBe(false);
    expect(document.querySelector('[style]')).toBeNull();
  });

  const unsafeLinkUrls = [
    ['javascript scheme', 'javascript:alert(1)'],
    ['mixed-case javascript scheme', 'JaVaScRiPt:alert(1)'],
    ['entity-encoded javascript scheme', 'java&#x73;cript:alert(1)'],
    ['tab-obfuscated javascript scheme', 'java&#x09;script:alert(1)'],
    ['newline-obfuscated javascript scheme', 'java&#10;script:alert(1)'],
    ['leading-whitespace javascript scheme', '  javascript:alert(1)'],
    ['vbscript scheme', 'vbscript:msgbox(1)'],
    ['data scheme', 'data:text/html,<script>alert(1)</script>'],
    ['file scheme', 'file:///etc/passwd'],
    ['blob scheme', 'blob:https://example.com/identifier'],
    ['protocol-relative URL', '//evil.example/path'],
    ['entity-encoded protocol-relative URL', '&#47;&#47;evil.example/path'],
    ['backslash-relative URL', '\\\\evil.example/path'],
    ['mixed slash and backslash URL', '/\\evil.example/path'],
    ['percent-encoded backslash URL', 'https:%5c%5cevil.example/path'],
    ['embedded-whitespace scheme', 'java script:alert(1)'],
    ['leading C0 control character', '\u0001javascript:alert(1)'],
  ] as const;

  it.each(unsafeLinkUrls)('removes an unsafe href using the %s', (_caseName, href) => {
    const document = parseDocument(
      sanitizeRichContentHtml(`<a href="${href}">unsafe link</a>`),
    );

    expect(document.body.textContent).toContain('unsafe link');
    expect(document.querySelector('a[href]')).toBeNull();
  });

  const unsafeImageUrls = [
    [
      'SVG data URL',
      'data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20onload%3D%22alert(1)%22%3E%3C%2Fsvg%3E',
    ],
    ['javascript scheme', 'javascript:alert(1)'],
    ['file scheme', 'file:///etc/passwd'],
    ['blob scheme', 'blob:https://example.com/identifier'],
    ['mailto scheme', 'mailto:security@example.com'],
    ['protocol-relative URL', '//evil.example/image.png'],
    ['backslash-relative URL', '\\\\evil.example/image.png'],
  ] as const;

  it.each(unsafeImageUrls)('removes an unsafe image src using the %s', (_caseName, src) => {
    const document = parseDocument(
      sanitizeRichContentHtml(`<img src="${src}" alt="unsafe image">`),
    );

    expect(document.querySelector('img[src]')).toBeNull();
  });

  const safeLinkUrls = [
    '/internal/path',
    'relative/path',
    './relative/path',
    '../relative/path',
    '#section',
    '?page=2',
    'http://example.com/path',
    'https://example.com/path',
    'mailto:security@example.com',
  ] as const;

  it.each(safeLinkUrls)('preserves the allowed link URL %s', (href) => {
    const document = parseDocument(
      sanitizeRichContentHtml(`<a href="${href}">safe link</a>`),
    );

    expect(document.querySelector('a')?.getAttribute('href')).toBe(href);
  });

  const safeImageUrls = [
    '/uploads/image.png',
    'images/image.png',
    './images/image.png',
    '../images/image.png',
    'http://example.com/image.png',
    'https://example.com/image.png',
  ] as const;

  it.each(safeImageUrls)('preserves the allowed image URL %s', (src) => {
    const document = parseDocument(
      sanitizeRichContentHtml(`<img src="${src}" alt="safe image">`),
    );

    expect(document.querySelector('img')?.getAttribute('src')).toBe(src);
  });

  it('preserves safe article formatting and allowed attributes', () => {
    const sanitized = sanitizeRichContentHtml(`
      <h2>Title</h2>
      <p class="lead">Hello <strong>world</strong> <em>today</em> <del>yesterday</del></p>
      <blockquote>Quoted text</blockquote>
      <ul><li>First item</li></ul>
      <ol><li>Second item</li></ol>
      <pre><code class="language-ts">const x = 1;</code></pre>
      <table>
        <thead><tr><th colspan="2">Header</th></tr></thead>
        <tbody><tr><td rowspan="2">A</td><td>B</td></tr></tbody>
      </table>
      <a
        href="https://example.com"
        title="Example"
        target="_blank"
        rel="nofollow"
      >safe external</a>
      <img
        src="https://example.com/a.png"
        alt="a"
        title="Image"
        class="rounded"
        width="640"
        height="480"
      >
    `);
    const document = parseDocument(sanitized);
    const externalLink = document.querySelector('a');
    const relTokens =
      externalLink?.getAttribute('rel')?.split(/\s+/).filter(Boolean) ?? [];

    expect(document.querySelector('h2')?.textContent).toBe('Title');
    expect(document.querySelector('p.lead strong')?.textContent).toBe('world');
    expect(document.querySelector('p.lead em')?.textContent).toBe('today');
    expect(document.querySelector('p.lead del')?.textContent).toBe('yesterday');
    expect(document.querySelector('blockquote')?.textContent).toBe('Quoted text');
    expect(document.querySelectorAll('li')).toHaveLength(2);
    expect(document.querySelector('pre > code.language-ts')?.textContent).toContain(
      'const x = 1;',
    );
    expect(document.querySelector('table')).not.toBeNull();
    expect(document.querySelector('th')?.getAttribute('colspan')).toBe('2');
    expect(document.querySelector('td')?.getAttribute('rowspan')).toBe('2');
    expect(externalLink?.getAttribute('href')).toBe('https://example.com');
    expect(externalLink?.getAttribute('target')).toBe('_blank');
    expect(relTokens).toEqual(expect.arrayContaining(['noopener', 'noreferrer']));
    expect(document.querySelector('img')?.getAttribute('src')).toBe(
      'https://example.com/a.png',
    );
    expect(document.querySelector('img')?.getAttribute('alt')).toBe('a');
    expect(document.querySelector('img')?.getAttribute('width')).toBe('640');
    expect(document.querySelector('img')?.getAttribute('height')).toBe('480');
  });

  it('removes opener and hardens an allowed link that opens a new tab', () => {
    const document = parseDocument(
      sanitizeRichContentHtml(`
        <a
          href="https://example.com"
          target="_BLANK"
          rel="opener ugc NOFOLLOW"
        >safe external</a>
      `),
    );
    const link = document.querySelector('a');
    const relTokens =
      link?.getAttribute('rel')?.split(/\s+/).filter(Boolean) ?? [];

    expect(link?.getAttribute('target')).toBe('_blank');
    expect(relTokens).toEqual(
      expect.arrayContaining(['ugc', 'nofollow', 'noopener', 'noreferrer']),
    );
    expect(relTokens).not.toContain('opener');
    expect(new Set(relTokens).size).toBe(relTokens.length);
  });

  it('removes globally allowed URL and layout attributes from the wrong elements', () => {
    const document = parseDocument(
      sanitizeRichContentHtml(`
        <div
          class="misplaced"
          href="/wrong"
          src="/wrong.png"
          alt="wrong"
          width="10"
          height="10"
          colspan="2"
          rowspan="2"
          target="_blank"
          rel="opener"
        >Safe text</div>
        <a class="safe-link" href="/safe" src="/wrong.png" width="10">Link</a>
        <img class="safe-image" src="/safe.png" href="/wrong" target="_blank">
      `),
    );
    const misplaced = document.querySelector('.misplaced');
    const safeLink = document.querySelector('.safe-link');
    const safeImage = document.querySelector('.safe-image');

    expect(misplaced?.getAttributeNames()).toEqual(['class']);
    expect(safeLink?.getAttribute('href')).toBe('/safe');
    expect(safeLink?.hasAttribute('src')).toBe(false);
    expect(safeLink?.hasAttribute('width')).toBe(false);
    expect(safeImage?.getAttribute('src')).toBe('/safe.png');
    expect(safeImage?.hasAttribute('href')).toBe(false);
    expect(safeImage?.hasAttribute('target')).toBe(false);
  });

  it('is idempotent for already-sanitized content', () => {
    const sanitized = sanitizeRichContentHtml(`
      <h2>Safe title</h2>
      <a href="https://example.com" target="_blank" rel="noopener">Link</a>
      <script>alert(1)</script>
    `);

    expect(sanitizeRichContentHtml(sanitized)).toBe(sanitized);
  });

  it('returns an empty string for empty or whitespace-only HTML', () => {
    expect(sanitizeRichContentHtml('')).toBe('');
    expect(sanitizeRichContentHtml(' \n\t')).toBe('');
  });
});

describe('parseAndSanitizeMarkdown', () => {
  it('parses Markdown and sanitizes embedded raw HTML', () => {
    const sanitized = parseAndSanitizeMarkdown(`
# Markdown title

Hello **world**.

\`\`\`ts
const x = 1;
\`\`\`

| Name | Value |
| --- | --- |
| Safe | 1 |

<div onclick="alert(1)">Safe raw HTML</div>
<a href="javascript:alert(1)">unsafe link</a>
    `);
    const document = parseDocument(sanitized);

    expect(document.querySelector('h1')?.textContent).toBe('Markdown title');
    expect(document.querySelector('strong')?.textContent).toBe('world');
    expect(document.querySelector('pre > code')?.textContent).toContain(
      'const x = 1;',
    );
    expect(document.querySelector('table')).not.toBeNull();
    expect(document.body.textContent).toContain('Safe raw HTML');
    expect(hasEventHandlerAttribute(document)).toBe(false);
    expect(document.querySelector('a[href]')).toBeNull();
  });

  it('keeps executable demo source inert inside a fenced code block', () => {
    const sanitized = parseAndSanitizeMarkdown(`
\`\`\`html
<html>
<script>alert(1)</script>
<iframe src="https://evil.example"></iframe>
</html>
\`\`\`
    `);
    const document = parseDocument(sanitized);

    expect(document.querySelector('script, iframe')).toBeNull();
    expect(document.querySelector('code')?.textContent).toContain(
      '<script>alert(1)</script>',
    );
    expect(document.querySelector('code')?.textContent).toContain(
      '<iframe src="https://evil.example"></iframe>',
    );
    expect(document.querySelector('code')?.textContent).toContain('<html>');
  });

  it('extracts and sanitizes only the body of a legacy full HTML document', () => {
    const sanitized = parseAndSanitizeMarkdown(`
      <!DoCtYpE html>
      <HtMl>
        <HeAd>
          <title>Head-only secret</title>
          <base href="https://evil.example/">
          <meta http-equiv="refresh" content="0;url=https://evil.example">
          <link rel="stylesheet" href="https://evil.example/style.css">
          <style>body { color: red; }</style>
          <script>alert("head")</script>
        </HeAd>
        <BoDy onload="alert(1)">
          <h2>Legacy body</h2>
          <p>Hello <strong>world</strong></p>
          <script>alert("body")</script>
        </BoDy>
      </HtMl>
    `);
    const document = parseDocument(sanitized);

    expect(document.body.textContent).not.toContain('Head-only secret');
    expect(document.querySelector('head > title, base, meta, link, style, script')).toBeNull();
    expect(document.querySelector('h2')?.textContent).toBe('Legacy body');
    expect(document.querySelector('strong')?.textContent).toBe('world');
    expect(hasEventHandlerAttribute(document)).toBe(false);
  });

  it('returns an empty string for empty or whitespace-only Markdown', () => {
    expect(parseAndSanitizeMarkdown('')).toBe('');
    expect(parseAndSanitizeMarkdown(' \n\t')).toBe('');
  });
});
