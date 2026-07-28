import DOMPurify, { type Config } from 'dompurify';
import { marked } from 'marked';

const HTML_NAMESPACE = 'http://www.w3.org/1999/xhtml';

const ALLOWED_TAGS = [
  'p',
  'br',
  'hr',
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6',
  'strong',
  'em',
  'del',
  'blockquote',
  'ul',
  'ol',
  'li',
  'pre',
  'code',
  'table',
  'thead',
  'tbody',
  'tr',
  'th',
  'td',
  'a',
  'img',
  'div',
  'span',
];

const ALLOWED_ATTRIBUTES = [
  'href',
  'src',
  'alt',
  'title',
  'class',
  'width',
  'height',
  'colspan',
  'rowspan',
  'target',
  'rel',
];

const FORBIDDEN_TAGS = [
  'script',
  'iframe',
  'object',
  'embed',
  'form',
  'input',
  'button',
  'textarea',
  'select',
  'option',
  'meta',
  'base',
  'link',
  'svg',
  'math',
  'style',
];

const SANITIZE_CONFIG = {
  ALLOWED_TAGS,
  ALLOWED_ATTR: ALLOWED_ATTRIBUTES,
  ALLOWED_NAMESPACES: [HTML_NAMESPACE],
  ALLOW_ARIA_ATTR: false,
  ALLOW_DATA_ATTR: false,
  ALLOW_UNKNOWN_PROTOCOLS: false,
  FORBID_ATTR: ['style'],
  FORBID_TAGS: FORBIDDEN_TAGS,
  KEEP_CONTENT: true,
  NAMESPACE: HTML_NAMESPACE,
  RETURN_DOM_FRAGMENT: true,
  RETURN_TRUSTED_TYPE: false,
} satisfies Config;

const LEADING_HTML_DOCTYPE = /^<!doctype\s+html(?:\s|>)/iu;
const LEADING_HTML_ELEMENT = /^<html(?:\s|>)/iu;
const WHITESPACE = /\s/u;
const ENCODED_BACKSLASH = /%5c/iu;
const SCHEME = /^[a-z][a-z\d+.-]*$/iu;

type UrlContext = 'link' | 'image';

function extractLegacyDocumentBody(html: string): string {
  if (typeof DOMParser === 'undefined') {
    return '';
  }

  return new DOMParser().parseFromString(html, 'text/html').body.innerHTML;
}

function firstPathBoundaryIndex(value: string): number {
  const boundaryIndexes = ['/', '?', '#']
    .map((character) => value.indexOf(character))
    .filter((index) => index >= 0);

  return boundaryIndexes.length > 0 ? Math.min(...boundaryIndexes) : value.length;
}

function hasControlCharacter(value: string): boolean {
  return Array.from(value).some((character) => {
    const codePoint = character.codePointAt(0);
    return (
      codePoint !== undefined &&
      (codePoint <= 0x1f || (codePoint >= 0x7f && codePoint <= 0x9f))
    );
  });
}

function isAllowedUrl(rawValue: string, context: UrlContext): boolean {
  if (hasControlCharacter(rawValue)) {
    return false;
  }

  const value = rawValue.trim();
  if (
    value.length === 0 ||
    WHITESPACE.test(value) ||
    value.includes('\\') ||
    ENCODED_BACKSLASH.test(value) ||
    value.startsWith('//')
  ) {
    return false;
  }

  const colonIndex = value.indexOf(':');
  if (colonIndex < 0 || colonIndex >= firstPathBoundaryIndex(value)) {
    return true;
  }

  const scheme = value.slice(0, colonIndex);
  if (!SCHEME.test(scheme)) {
    return false;
  }

  const normalizedScheme = scheme.toLowerCase();
  if (context === 'image') {
    return normalizedScheme === 'http' || normalizedScheme === 'https';
  }

  return (
    normalizedScheme === 'http' ||
    normalizedScheme === 'https' ||
    normalizedScheme === 'mailto'
  );
}

function removeMisplacedAttributes(fragment: DocumentFragment): void {
  fragment.querySelectorAll('*').forEach((element) => {
    const tagName = element.tagName.toLowerCase();

    if (tagName !== 'a') {
      element.removeAttribute('href');
      element.removeAttribute('target');
      element.removeAttribute('rel');
    }

    if (tagName !== 'img') {
      element.removeAttribute('src');
      element.removeAttribute('alt');
      element.removeAttribute('width');
      element.removeAttribute('height');
    }

    if (tagName !== 'th' && tagName !== 'td') {
      element.removeAttribute('colspan');
      element.removeAttribute('rowspan');
    }
  });
}

function secureLinks(fragment: DocumentFragment): void {
  fragment.querySelectorAll('a').forEach((anchor) => {
    const href = anchor.getAttribute('href');

    if (href === null || !isAllowedUrl(href, 'link')) {
      anchor.removeAttribute('href');
      anchor.removeAttribute('target');
      anchor.removeAttribute('rel');
      return;
    }

    anchor.setAttribute('href', href.trim());

    const target = anchor.getAttribute('target')?.trim().toLowerCase();
    if (target === '_blank') {
      const existingRelTokens = (anchor.getAttribute('rel') ?? '')
        .split(/\s+/u)
        .map((token) => token.toLowerCase())
        .filter((token) => token.length > 0 && token !== 'opener');
      const relTokens = new Set(existingRelTokens);
      relTokens.add('noopener');
      relTokens.add('noreferrer');

      anchor.setAttribute('target', '_blank');
      anchor.setAttribute('rel', [...relTokens].join(' '));
      return;
    }

    if (target === '_self') {
      anchor.setAttribute('target', '_self');
      return;
    }

    anchor.removeAttribute('target');
  });
}

function secureImages(fragment: DocumentFragment): void {
  fragment.querySelectorAll('img').forEach((image) => {
    const src = image.getAttribute('src');

    if (src === null || !isAllowedUrl(src, 'image')) {
      image.removeAttribute('src');
      return;
    }

    image.setAttribute('src', src.trim());
  });
}

function serializeFragment(fragment: DocumentFragment): string {
  const container = document.createElement('div');
  container.append(fragment);
  return container.innerHTML;
}

export function isLegacyFullHtmlDocument(content: string): boolean {
  const leadingContent = content.trimStart();
  return (
    LEADING_HTML_DOCTYPE.test(leadingContent) ||
    LEADING_HTML_ELEMENT.test(leadingContent)
  );
}

export function sanitizeRichContentHtml(html: string): string {
  if (
    html.trim().length === 0 ||
    typeof document === 'undefined' ||
    !DOMPurify.isSupported
  ) {
    return '';
  }

  const source = isLegacyFullHtmlDocument(html)
    ? extractLegacyDocumentBody(html)
    : html;
  const fragment = DOMPurify.sanitize(source, SANITIZE_CONFIG);

  removeMisplacedAttributes(fragment);
  secureLinks(fragment);
  secureImages(fragment);

  return serializeFragment(fragment);
}

export function parseAndSanitizeMarkdown(markdown: string): string {
  if (markdown.trim().length === 0) {
    return '';
  }

  if (isLegacyFullHtmlDocument(markdown)) {
    return sanitizeRichContentHtml(markdown);
  }

  return sanitizeRichContentHtml(marked.parse(markdown, { async: false }));
}
