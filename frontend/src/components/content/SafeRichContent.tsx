import { useMemo } from 'react';
import {
  parseAndSanitizeMarkdown,
  sanitizeRichContentHtml,
} from '../../features/content-security/sanitizeRichContent';

export type SafeRichContentFormat = 'markdown' | 'html';

export interface SafeRichContentProps {
  content: string;
  format?: SafeRichContentFormat;
  className?: string;
}

const RICH_CONTENT_CLASSES = [
  'overflow-x-auto break-words p-6 leading-7 text-gray-700 dark:text-gray-300',
  '[&>*:first-child]:mt-0 [&>*:last-child]:mb-0',
  '[&_h1]:mb-4 [&_h1]:mt-8 [&_h1]:text-3xl [&_h1]:font-bold [&_h1]:text-gray-900 dark:[&_h1]:text-gray-100',
  '[&_h2]:mb-4 [&_h2]:mt-8 [&_h2]:text-2xl [&_h2]:font-bold [&_h2]:text-gray-900 dark:[&_h2]:text-gray-100',
  '[&_h3]:mb-3 [&_h3]:mt-6 [&_h3]:text-xl [&_h3]:font-semibold [&_h3]:text-gray-900 dark:[&_h3]:text-gray-100',
  '[&_h4]:mb-3 [&_h4]:mt-6 [&_h4]:text-lg [&_h4]:font-semibold [&_h4]:text-gray-900 dark:[&_h4]:text-gray-100',
  '[&_h5]:mb-2 [&_h5]:mt-5 [&_h5]:font-semibold [&_h5]:text-gray-900 dark:[&_h5]:text-gray-100',
  '[&_h6]:mb-2 [&_h6]:mt-5 [&_h6]:font-semibold [&_h6]:text-gray-700 dark:[&_h6]:text-gray-300',
  '[&_p]:my-4',
  '[&_ul]:my-4 [&_ul]:list-disc [&_ul]:pl-6',
  '[&_ol]:my-4 [&_ol]:list-decimal [&_ol]:pl-6',
  '[&_li]:my-1',
  '[&_blockquote]:my-5 [&_blockquote]:border-l-4 [&_blockquote]:border-gray-200 [&_blockquote]:pl-4 [&_blockquote]:italic [&_blockquote]:text-gray-500 dark:[&_blockquote]:border-gray-700 dark:[&_blockquote]:text-gray-400',
  '[&_pre]:my-5 [&_pre]:overflow-x-auto [&_pre]:rounded-lg [&_pre]:bg-gray-100 [&_pre]:p-4 [&_pre]:font-mono [&_pre]:text-sm dark:[&_pre]:bg-gray-900',
  '[&_code]:rounded [&_code]:bg-gray-100 [&_code]:px-1.5 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-sm dark:[&_code]:bg-gray-900',
  '[&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:text-inherit',
  '[&_table]:my-6 [&_table]:w-full [&_table]:border-collapse',
  '[&_th]:border [&_th]:border-gray-300 [&_th]:bg-gray-100 [&_th]:px-3 [&_th]:py-2 [&_th]:text-left [&_th]:font-semibold dark:[&_th]:border-gray-700 dark:[&_th]:bg-gray-900',
  '[&_td]:border [&_td]:border-gray-300 [&_td]:px-3 [&_td]:py-2 dark:[&_td]:border-gray-700',
  '[&_a]:break-all [&_a]:text-primary-600 [&_a]:underline [&_a]:underline-offset-2 hover:[&_a]:text-primary-700 dark:[&_a]:text-primary-400 dark:hover:[&_a]:text-primary-300',
  '[&_img]:my-5 [&_img]:h-auto [&_img]:max-w-full [&_img]:rounded-lg',
  '[&_hr]:my-8 [&_hr]:border-gray-200 dark:[&_hr]:border-gray-700',
].join(' ');

export function SafeRichContent({
  content,
  format = 'markdown',
  className,
}: SafeRichContentProps) {
  const sanitizedHtml = useMemo(
    () =>
      format === 'html'
        ? sanitizeRichContentHtml(content)
        : parseAndSanitizeMarkdown(content),
    [content, format],
  );

  const rootClassName = className
    ? `${RICH_CONTENT_CLASSES} ${className}`
    : RICH_CONTENT_CLASSES;

  return (
    <div
      className={rootClassName}
      data-safe-rich-content
      dangerouslySetInnerHTML={{ __html: sanitizedHtml }}
    />
  );
}
