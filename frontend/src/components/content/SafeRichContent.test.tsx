import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { SafeRichContent } from './SafeRichContent';

const hasEventHandlerAttribute = (root: Element): boolean =>
  Array.from(root.querySelectorAll('*')).some((element) =>
    element
      .getAttributeNames()
      .some((attributeName) => attributeName.toLowerCase().startsWith('on')),
  );

describe('SafeRichContent', () => {
  it('renders safe Markdown structure and link protections', () => {
    const { container } = render(
      <SafeRichContent
        content={`
## Safe title

Hello **world**.

\`\`\`ts
const x = 1;
\`\`\`

| Name | Value |
| --- | --- |
| Safe | 1 |

<a href="https://example.com" target="_blank">safe external</a>
<img src="https://example.com/a.png" alt="safe image">
        `}
        format="markdown"
      />,
    );
    const root = container.querySelector('[data-safe-rich-content]');
    const externalLink = screen.getByRole('link', { name: 'safe external' });
    const relTokens =
      externalLink.getAttribute('rel')?.split(/\s+/).filter(Boolean) ?? [];

    expect(root).not.toBeNull();
    expect(screen.getByRole('heading', { level: 2, name: 'Safe title' })).toBeVisible();
    expect(screen.getByText('world', { selector: 'strong' })).toBeVisible();
    expect(container.querySelector('pre > code')?.textContent).toContain(
      'const x = 1;',
    );
    expect(screen.getByRole('table')).toBeVisible();
    expect(externalLink).toHaveAttribute('href', 'https://example.com');
    expect(externalLink).toHaveAttribute('target', '_blank');
    expect(relTokens).toEqual(expect.arrayContaining(['noopener', 'noreferrer']));
    expect(screen.getByRole('img', { name: 'safe image' })).toHaveAttribute(
      'src',
      'https://example.com/a.png',
    );
  });

  it('sanitizes HTML before it reaches the rendering sink', () => {
    const { container } = render(
      <SafeRichContent
        content={`
          <script>alert(1)</script>
          <div onclick="alert(1)" style="color: red">Safe text</div>
          <svg onload="alert(1)"></svg>
          <iframe src="https://evil.example"></iframe>
          <object data="https://evil.example"></object>
          <embed src="https://evil.example">
          <a href="javascript:alert(1)">unsafe link</a>
          <img
            src="data:image/svg+xml,%3Csvg%20onload%3D%22alert(1)%22%3E%3C%2Fsvg%3E"
            alt="unsafe image"
            onerror="alert(1)"
          >
        `}
        format="html"
      />,
    );
    const root = container.querySelector('[data-safe-rich-content]');
    const unsafeLink = screen.getByText('unsafe link').closest('a');
    const unsafeImage = screen.getByAltText('unsafe image');

    expect(root).not.toBeNull();
    expect(root?.querySelector('script, svg, iframe, object, embed')).toBeNull();
    expect(root ? hasEventHandlerAttribute(root) : true).toBe(false);
    expect(root?.querySelector('[style]')).toBeNull();
    expect(unsafeLink?.hasAttribute('href')).toBe(false);
    expect(unsafeImage).not.toHaveAttribute('src');
    expect(unsafeImage).not.toHaveAttribute('onerror');
    expect(screen.getByText('Safe text')).toBeVisible();
  });

  it('handles empty content and applies the supplied class name', () => {
    const { container } = render(
      <SafeRichContent
        content=""
        className="security-prose"
      />,
    );
    const root = container.querySelector('[data-safe-rich-content]');

    expect(root).not.toBeNull();
    expect(root).toHaveClass('security-prose');
    expect(root).toBeEmptyDOMElement();
  });

  it('recomputes sanitized output when content or format changes', () => {
    const { container, rerender } = render(
      <SafeRichContent
        content="**bold**"
        format="markdown"
      />,
    );

    expect(container.querySelector('strong')?.textContent).toBe('bold');

    rerender(
      <SafeRichContent
        content="**bold**"
        format="html"
      />,
    );

    expect(container.querySelector('strong')).toBeNull();
    expect(
      container.querySelector('[data-safe-rich-content]')?.textContent,
    ).toContain('**bold**');

    rerender(
      <SafeRichContent
        content="<h3>Updated content</h3>"
        format="html"
      />,
    );

    expect(
      screen.getByRole('heading', { level: 3, name: 'Updated content' }),
    ).toBeVisible();
    expect(container.textContent).not.toContain('**bold**');
  });
});
