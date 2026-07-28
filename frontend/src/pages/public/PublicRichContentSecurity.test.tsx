import type { ReactElement } from 'react';
import { screen, within } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ProjectDetailPage } from './ProjectDetailPage';
import { RoadmapDetailPage } from './RoadmapDetailPage';
import { TutorialDetailPage } from './TutorialDetailPage';
import {
  projectService,
  roadmapService,
  tutorialService,
} from '../../services';
import { renderWithProviders } from '../../test/renderWithProviders';
import type { Project, Roadmap, Tutorial } from '../../types';

const { safeRichContentPropsSpy } = vi.hoisted(() => ({
  safeRichContentPropsSpy: vi.fn(),
}));

vi.mock('../../services', () => ({
  tutorialService: {
    getByIdOrSlug: vi.fn(),
  },
  projectService: {
    getByIdOrSlug: vi.fn(),
  },
  roadmapService: {
    getByIdOrSlug: vi.fn(),
  },
}));

vi.mock(
  '../../features/learning-progress/components/LearningProgressControls',
  () => ({
    LearningProgressControls: () => null,
  }),
);

vi.mock('../../components/content/SafeRichContent', async (importOriginal) => {
  const actual =
    await importOriginal<
      typeof import('../../components/content/SafeRichContent')
    >();
  const ActualSafeRichContent = actual.SafeRichContent;

  return {
    ...actual,
    SafeRichContent: (
      props: Parameters<typeof ActualSafeRichContent>[0],
    ) => {
      safeRichContentPropsSpy(props);

      return (
        <div data-testid="safe-rich-content-probe">
          <ActualSafeRichContent {...props} />
        </div>
      );
    },
  };
});

const storedXssContent = `
# Sanitized article

<script>alert(1)</script>
<div onclick="alert(1)" style="color: red">Safe raw text</div>
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
`;

const tutorialFixture: Tutorial = {
  id: 11,
  title: 'SEC-1 tutorial',
  slug: 'sec-1-tutorial',
  description: 'Tutorial security regression fixture.',
  category: 'Security',
  createdAt: '2026-01-01T00:00:00Z',
  content: storedXssContent,
};

const projectFixture: Project = {
  id: 22,
  title: 'SEC-1 project',
  slug: 'sec-1-project',
  description: 'Project security regression fixture.',
  content: storedXssContent,
};

const roadmapFixture: Roadmap = {
  id: 33,
  title: 'SEC-1 roadmap',
  slug: 'sec-1-roadmap',
  description: 'Roadmap security regression fixture.',
  icon: 'shield',
  color: 'primary',
  content: storedXssContent,
};

const renderAtRoute = (
  element: ReactElement,
  routePattern: string,
  initialEntry: string,
) =>
  renderWithProviders(
    <Routes>
      <Route path={routePattern} element={element} />
    </Routes>,
    { initialEntries: [initialEntry] },
  );

const hasEventHandlerAttribute = (root: Element): boolean =>
  Array.from(root.querySelectorAll('*')).some((element) =>
    element
      .getAttributeNames()
      .some((attributeName) => attributeName.toLowerCase().startsWith('on')),
  );

const expectSecureSharedRenderer = async (
  pageContainer: HTMLElement,
  expectedContent: string,
) => {
  const probe = await screen.findByTestId('safe-rich-content-probe');
  const richContentRoot = probe.querySelector('[data-safe-rich-content]');
  const unsafeLink = within(probe).getByText('unsafe link').closest('a');
  const unsafeImage = within(probe).getByAltText('unsafe image');

  expect(safeRichContentPropsSpy).toHaveBeenCalledWith(
    expect.objectContaining({
      content: expectedContent,
      format: 'markdown',
    }),
  );
  expect(richContentRoot).not.toBeNull();
  expect(
    within(probe).getByRole('heading', {
      level: 1,
      name: 'Sanitized article',
    }),
  ).toBeVisible();
  expect(probe.querySelector('script, iframe, object, embed, svg')).toBeNull();
  expect(hasEventHandlerAttribute(probe)).toBe(false);
  expect(probe.querySelector('[style]')).toBeNull();
  expect(unsafeLink?.hasAttribute('href')).toBe(false);
  expect(unsafeImage).not.toHaveAttribute('src');
  expect(unsafeImage).not.toHaveAttribute('onerror');
  expect(pageContainer.querySelector('iframe')).toBeNull();
};

describe('public rich-content security boundary', () => {
  beforeEach(() => {
    safeRichContentPropsSpy.mockClear();
    vi.mocked(tutorialService.getByIdOrSlug).mockReset();
    vi.mocked(projectService.getByIdOrSlug).mockReset();
    vi.mocked(roadmapService.getByIdOrSlug).mockReset();
  });

  it('routes tutorial API content through SafeRichContent', async () => {
    vi.mocked(tutorialService.getByIdOrSlug).mockResolvedValueOnce(
      tutorialFixture,
    );

    const { container } = renderAtRoute(
      <TutorialDetailPage />,
      '/tutorials/:id',
      '/tutorials/sec-1-tutorial',
    );

    await expectSecureSharedRenderer(container, storedXssContent);
    expect(tutorialService.getByIdOrSlug).toHaveBeenCalledWith(
      'sec-1-tutorial',
    );
  });

  it('routes project API content through SafeRichContent', async () => {
    vi.mocked(projectService.getByIdOrSlug).mockResolvedValueOnce(
      projectFixture,
    );

    const { container } = renderAtRoute(
      <ProjectDetailPage />,
      '/projects/:id',
      '/projects/sec-1-project',
    );

    await expectSecureSharedRenderer(container, storedXssContent);
    expect(projectService.getByIdOrSlug).toHaveBeenCalledWith('sec-1-project');
  });

  it('routes roadmap API content through SafeRichContent', async () => {
    vi.mocked(roadmapService.getByIdOrSlug).mockResolvedValueOnce(
      roadmapFixture,
    );

    const { container } = renderAtRoute(
      <RoadmapDetailPage />,
      '/roadmaps/:id',
      '/roadmaps/sec-1-roadmap',
    );

    await expectSecureSharedRenderer(container, storedXssContent);
    expect(roadmapService.getByIdOrSlug).toHaveBeenCalledWith('sec-1-roadmap');
  });
});
