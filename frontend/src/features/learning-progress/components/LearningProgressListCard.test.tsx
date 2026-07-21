import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { screen, fireEvent } from '@testing-library/react';
import { LearningProgressListCard } from './LearningProgressListCard';
import { renderWithProviders } from '../../../test/renderWithProviders';
import { useNavigate } from 'react-router-dom';
import {
  useCompleteLearningProgress,
  useResetLearningProgress,
} from '../hooks/useLearningProgressMutations';
import { createListItem, createMutationMock } from '../test/learningProgressTestFactories';
import toast from 'react-hot-toast';

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});

vi.mock('../hooks/useLearningProgressMutations', () => ({
  useCompleteLearningProgress: vi.fn(),
  useResetLearningProgress: vi.fn(),
}));

vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe('LearningProgressListCard', () => {
  let completeMutation: ReturnType<typeof createMutationMock>;
  let resetMutation: ReturnType<typeof createMutationMock>;
  let navigateMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.clearAllMocks();
    
    navigateMock = vi.fn();
    vi.mocked(useNavigate).mockReturnValue(navigateMock as any);

    completeMutation = createMutationMock();
    resetMutation = createMutationMock();

    vi.mocked(useCompleteLearningProgress).mockReturnValue(completeMutation as never);
    vi.mocked(useResetLearningProgress).mockReturnValue(resetMutation as never);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Visibility and Rendering', () => {
    it('renders correctly for available IN_PROGRESS item', () => {
      const item = createListItem({ status: 'IN_PROGRESS', progressPercent: 50 });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      expect(screen.getByText('Tiếp tục học')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Hoàn thành/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đặt lại/i })).toBeInTheDocument();
      expect(screen.queryByText(/Nội dung không khả dụng/i)).not.toBeInTheDocument();
    });

    it('renders correctly for COMPLETED item', () => {
      const item = createListItem({ status: 'COMPLETED', progressPercent: 100 });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      expect(screen.getByText('Tiếp tục học')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Hoàn thành/i })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đặt lại/i })).toBeInTheDocument();
    });

    it('renders correctly for orphan (unavailable) item', () => {
      const item = createListItem({ contentAvailable: false });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      expect(screen.getByText(/Nội dung không khả dụng/i)).toBeInTheDocument();
      expect(screen.queryByText('Tiếp tục học')).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Hoàn thành/i })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đặt lại/i })).toBeInTheDocument();
    });

    it('does not show Continue button for invalid routes', () => {
      const items = [
        createListItem({ route: 'https://evil.com' }),
        createListItem({ route: '//evil.com' }),
        createListItem({ route: '' }),
        createListItem({ route: null as never }),
      ];

      items.forEach(item => {
        const { unmount } = renderWithProviders(<LearningProgressListCard item={item} />);
        expect(screen.queryByText('Tiếp tục học')).not.toBeInTheDocument();
        unmount();
      });
    });
  });

  describe('Continue action', () => {
    it('navigates to route on Continue click', () => {
      const item = createListItem({ route: '/tutorials/java-basic' });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByText('Tiếp tục học'));
      expect(navigateMock).toHaveBeenCalledWith('/tutorials/java-basic');
    });

    it('does not navigate if mutation is pending', () => {
      completeMutation.isPending = true;
      vi.mocked(useCompleteLearningProgress).mockReturnValue(completeMutation as never);
      
      const item = createListItem({ route: '/tutorials/java-basic' });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      const continueBtn = screen.getByText('Tiếp tục học').closest('button');
      expect(continueBtn).toBeDisabled();
    });
  });

  describe('Complete action', () => {
    it('calls complete mutation when clicked', () => {
      const item = createListItem({ contentType: 'TUTORIAL', contentKey: 'java-basic', status: 'IN_PROGRESS', progressPercent: 50 });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Hoàn thành/i }));
      expect(completeMutation.mutate).toHaveBeenCalledWith(
        { contentType: 'TUTORIAL', contentKey: 'java-basic' },
        expect.any(Object)
      );
    });

    it('shows success toast on success', () => {
      const item = createListItem();
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Hoàn thành/i }));
      const onSuccess = completeMutation.mutate.mock.calls[0][1].onSuccess;
      onSuccess();
      
      expect(vi.mocked(toast.success)).toHaveBeenCalledWith('Đã đánh dấu hoàn thành.');
    });

    it('shows error toast on failure', () => {
      const item = createListItem();
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Hoàn thành/i }));
      const onError = completeMutation.mutate.mock.calls[0][1].onError;
      onError();
      
      expect(vi.mocked(toast.error)).toHaveBeenCalledWith('Không thể hoàn thành nội dung. Vui lòng thử lại.');
    });

    it('disables buttons when pending', () => {
      completeMutation.isPending = true;
      vi.mocked(useCompleteLearningProgress).mockReturnValue(completeMutation as never);
      
      const item = createListItem();
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      expect(screen.getByRole('button', { name: /Đang.../i })).toBeDisabled(); // Complete
      expect(screen.getByRole('button', { name: /Đặt lại/i })).toBeDisabled(); // Reset
      expect(screen.getByText('Tiếp tục học').closest('button')).toBeDisabled(); // Continue
    });
  });

  describe('Reset action', () => {
    let confirmSpy: ReturnType<typeof vi.spyOn>;
    
    beforeEach(() => {
      confirmSpy = vi.spyOn(window, 'confirm');
    });

    afterEach(() => {
      confirmSpy.mockRestore();
    });

    it('does not call reset if confirm is cancelled', () => {
      confirmSpy.mockReturnValue(false);
      const item = createListItem();
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      expect(resetMutation.mutate).not.toHaveBeenCalled();
    });

    it('calls reset if confirmed', () => {
      confirmSpy.mockReturnValue(true);
      const item = createListItem({ contentType: 'TUTORIAL', contentKey: 'java-basic' });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      expect(resetMutation.mutate).toHaveBeenCalledWith(
        { contentType: 'TUTORIAL', contentKey: 'java-basic' },
        expect.any(Object)
      );
    });

    it('allows orphan items to be reset', () => {
      confirmSpy.mockReturnValue(true);
      const item = createListItem({ contentAvailable: false });
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      expect(resetMutation.mutate).toHaveBeenCalled();
    });

    it('shows toasts based on callbacks', () => {
      confirmSpy.mockReturnValue(true);
      const item = createListItem();
      renderWithProviders(<LearningProgressListCard item={item} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      
      const { onSuccess, onError } = resetMutation.mutate.mock.calls[0][1];
      
      onSuccess();
      expect(vi.mocked(toast.success)).toHaveBeenCalledWith('Đã đặt lại tiến độ học tập.');
      
      onError();
      expect(vi.mocked(toast.error)).toHaveBeenCalledWith('Không thể đặt lại tiến độ. Vui lòng thử lại.');
    });
  });
});
