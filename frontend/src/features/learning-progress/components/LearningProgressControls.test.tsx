import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { screen, fireEvent, act } from '@testing-library/react';
import { LearningProgressControls } from './LearningProgressControls';
import { renderWithProviders } from '../../../test/renderWithProviders';
import { useAuth } from '../../../context/AuthContext';
import { useLearningProgress } from '../hooks/useLearningProgress';
import {
  useTouchLearningProgress,
  useUpdateLearningProgress,
  useCompleteLearningProgress,
  useResetLearningProgress,
} from '../hooks/useLearningProgressMutations';
import { createProgress, createMutationMock } from '../test/learningProgressTestFactories';
import toast from 'react-hot-toast';
import { StrictMode } from 'react';

// Mock auth context
vi.mock('../../../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

// Mock hooks
vi.mock('../hooks/useLearningProgress', () => ({
  useLearningProgress: vi.fn(),
}));

vi.mock('../hooks/useLearningProgressMutations', () => ({
  useTouchLearningProgress: vi.fn(),
  useUpdateLearningProgress: vi.fn(),
  useCompleteLearningProgress: vi.fn(),
  useResetLearningProgress: vi.fn(),
}));

// Mock toast
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

describe('LearningProgressControls', () => {
  let touchMutation: ReturnType<typeof createMutationMock>;
  let updateMutation: ReturnType<typeof createMutationMock>;
  let completeMutation: ReturnType<typeof createMutationMock>;
  let resetMutation: ReturnType<typeof createMutationMock>;

  const defaultProps = {
    contentType: 'TUTORIAL' as const,
    contentKey: 'java-basic',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    // Default auth state
    vi.mocked(useAuth).mockReturnValue({
      isInitialized: true,
      isAuthenticated: true,
      role: null,
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
    });

    // Default query state
    vi.mocked(useLearningProgress).mockReturnValue({
      data: createProgress(),
      isLoading: false,
      isError: false,
      isSuccess: true,
      refetch: vi.fn(),
    } as never);

    touchMutation = createMutationMock();
    updateMutation = createMutationMock();
    completeMutation = createMutationMock();
    resetMutation = createMutationMock();

    vi.mocked(useTouchLearningProgress).mockReturnValue(touchMutation as never);
    vi.mocked(useUpdateLearningProgress).mockReturnValue(updateMutation as never);
    vi.mocked(useCompleteLearningProgress).mockReturnValue(completeMutation as never);
    vi.mocked(useResetLearningProgress).mockReturnValue(resetMutation as never);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Auth initialization', () => {
    it('shows loading spinner when not initialized', () => {
      vi.mocked(useAuth).mockReturnValue({ isInitialized: false, isAuthenticated: false } as never);
      const { container } = renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(container.querySelector('.animate-spin')).toBeInTheDocument();
      expect(screen.queryByText(/Đăng nhập để lưu tiến độ/i)).not.toBeInTheDocument();
      expect(touchMutation.mutate).not.toHaveBeenCalled();
    });

    it('shows login CTA when initialized but not authenticated', () => {
      vi.mocked(useAuth).mockReturnValue({ isInitialized: true, isAuthenticated: false } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(screen.getByText(/Đăng nhập để lưu tiến độ/i)).toBeInTheDocument();
      expect(touchMutation.mutate).not.toHaveBeenCalled();
      expect(screen.queryByRole('slider')).not.toBeInTheDocument();
    });
  });

  describe('Progress loading/error', () => {
    it('shows loading when query is loading', () => {
      vi.mocked(useLearningProgress).mockReturnValue({ isLoading: true, isError: false, data: undefined, isSuccess: false } as never);
      const { container } = renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(container.querySelector('.animate-spin')).toBeInTheDocument();
      expect(touchMutation.mutate).not.toHaveBeenCalled();
      expect(screen.queryByText(/Chưa bắt đầu/i)).not.toBeInTheDocument();
    });

    it('shows error state and retry button when query errors', () => {
      const refetchMock = vi.fn();
      vi.mocked(useLearningProgress).mockReturnValue({ isLoading: false, isError: true, isSuccess: false, refetch: refetchMock } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(screen.getByText(/Không thể tải tiến độ học tập/i)).toBeInTheDocument();
      const retryBtn = screen.getByRole('button', { name: /Thử lại/i });
      fireEvent.click(retryBtn);
      expect(refetchMock).toHaveBeenCalled();
      expect(touchMutation.mutate).not.toHaveBeenCalled();
    });
  });

  describe('Auto-touch', () => {
    it('calls touch progress exactly once on successful query', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(touchMutation.mutate).toHaveBeenCalledTimes(1);
      expect(touchMutation.mutate).toHaveBeenCalledWith({
        contentType: 'TUTORIAL',
        contentKey: 'java-basic',
      });
    });

    it('does not touch again on re-render with same identity', () => {
      const { rerender } = renderWithProviders(<LearningProgressControls {...defaultProps} />);
      rerender(<LearningProgressControls {...defaultProps} />);
      expect(touchMutation.mutate).toHaveBeenCalledTimes(1);
    });

    it('does not loop in StrictMode', () => {
      renderWithProviders(
        <StrictMode>
          <LearningProgressControls {...defaultProps} />
        </StrictMode>
      );
      // In React 18 StrictMode, effects run twice in dev, but our ref guard should block the second call
      expect(touchMutation.mutate).toHaveBeenCalledTimes(1);
    });

    it('touches once when switching content', () => {
      const { rerender } = renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(touchMutation.mutate).toHaveBeenCalledTimes(1);

      rerender(<LearningProgressControls contentType="PROJECT" contentKey="new-project" />);
      expect(touchMutation.mutate).toHaveBeenCalledTimes(2);
      expect(touchMutation.mutate).toHaveBeenLastCalledWith({
        contentType: 'PROJECT',
        contentKey: 'new-project',
      });
    });
  });

  describe('Status rendering', () => {
    it('renders NOT_STARTED correctly', () => {
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'NOT_STARTED', progressPercent: 0 }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(screen.getByText(/Chưa bắt đầu/i)).toBeInTheDocument();
      expect(screen.getAllByText('0%')[0]).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đánh dấu hoàn thành/i })).toBeInTheDocument();
    });

    it('renders IN_PROGRESS correctly', () => {
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'IN_PROGRESS', progressPercent: 50 }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(screen.getByText(/Đang học/i)).toBeInTheDocument();
      expect(screen.getAllByText('50%')[0]).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đánh dấu hoàn thành/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đặt lại/i })).toBeInTheDocument();
    });

    it('renders COMPLETED correctly', () => {
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'COMPLETED', progressPercent: 100, completedAt: '2026-07-20T10:00:00Z' }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      expect(screen.getByText(/Đã hoàn thành/i)).toBeInTheDocument();
      expect(screen.getAllByText('100%')[0]).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Đánh dấu hoàn thành/i })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Đặt lại/i })).toBeInTheDocument();
    });
  });

  describe('Manual editor', () => {
    beforeEach(() => {
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'IN_PROGRESS', progressPercent: 50 }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
    });

    it('shows server progress and disables save when pristine', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      const slider = screen.getByRole('slider');
      expect(slider).toHaveValue('50');
      const saveBtn = screen.getByRole('button', { name: /Lưu tiến độ/i });
      expect(saveBtn).toBeDisabled();
    });

    it('enables save when dragging slider, calls update when save clicked', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      const slider = screen.getByRole('slider');
      fireEvent.change(slider, { target: { value: '80' } });
      
      const saveBtn = screen.getByRole('button', { name: /Lưu tiến độ/i });
      expect(saveBtn).toBeEnabled();
      expect(updateMutation.mutate).not.toHaveBeenCalled();

      fireEvent.click(saveBtn);
      expect(updateMutation.mutate).toHaveBeenCalledTimes(1);
      expect(updateMutation.mutate).toHaveBeenCalledWith(
        { contentType: 'TUTORIAL', contentKey: 'java-basic', request: { progressPercent: 80 } },
        expect.any(Object)
      );
    });

    it('disables controls when update is pending', () => {
      updateMutation.isPending = true;
      vi.mocked(useUpdateLearningProgress).mockReturnValue(updateMutation as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      
      expect(screen.getByRole('slider')).toBeDisabled();
      expect(screen.getByRole('button', { name: /Đang lưu.../i })).toBeDisabled();
      expect(screen.getByRole('button', { name: /Đánh dấu hoàn thành/i })).toBeDisabled();
    });

    it('resets dirty state and syncs draft on success', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      fireEvent.change(screen.getByRole('slider'), { target: { value: '80' } });
      fireEvent.click(screen.getByRole('button', { name: /Lưu tiến độ/i }));
      
      const onSuccess = updateMutation.mutate.mock.calls[0][1].onSuccess;
      
      act(() => {
        vi.mocked(useLearningProgress).mockReturnValue({
          data: createProgress({ status: 'IN_PROGRESS', progressPercent: 80 }),
          isLoading: false, isError: false, isSuccess: true
        } as never);
        onSuccess({ progressPercent: 80 });
      });
      
      expect(vi.mocked(toast.success)).toHaveBeenCalledWith('Đã cập nhật tiến độ học tập.');
      expect(screen.getByRole('slider')).toHaveValue('80');
      expect(screen.getByRole('button', { name: /Lưu tiến độ/i })).toBeDisabled(); // not dirty anymore
    });

    it('shows error toast on update failure and keeps draft', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      fireEvent.change(screen.getByRole('slider'), { target: { value: '80' } });
      fireEvent.click(screen.getByRole('button', { name: /Lưu tiến độ/i }));
      
      const onError = updateMutation.mutate.mock.calls[0][1].onError;
      onError();
      
      expect(vi.mocked(toast.error)).toHaveBeenCalledWith('Không thể cập nhật tiến độ. Vui lòng thử lại.');
      expect(screen.getByRole('slider')).toHaveValue('80'); // draft kept
      expect(screen.getByRole('button', { name: /Lưu tiến độ/i })).toBeEnabled(); // still dirty
    });
  });

  describe('Complete action', () => {
    it('calls complete mutation when clicked', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      fireEvent.click(screen.getByRole('button', { name: /Đánh dấu hoàn thành/i }));
      
      expect(completeMutation.mutate).toHaveBeenCalledTimes(1);
      expect(completeMutation.mutate).toHaveBeenCalledWith(
        { contentType: 'TUTORIAL', contentKey: 'java-basic' },
        expect.any(Object)
      );
    });

    it('disables controls when complete is pending', () => {
      completeMutation.isPending = true;
      vi.mocked(useCompleteLearningProgress).mockReturnValue(completeMutation as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      
      expect(screen.getByRole('button', { name: /Đang hoàn thành.../i })).toBeDisabled();
      expect(screen.getByRole('slider')).toBeDisabled();
    });

    it('shows success toast and updates draft on success', () => {
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      fireEvent.click(screen.getByRole('button', { name: /Đánh dấu hoàn thành/i }));
      
      const onSuccess = completeMutation.mutate.mock.calls[0][1].onSuccess;
      
      act(() => {
        vi.mocked(useLearningProgress).mockReturnValue({
          data: createProgress({ status: 'COMPLETED', progressPercent: 100 }),
          isLoading: false, isError: false, isSuccess: true
        } as never);
        onSuccess({ progressPercent: 100 });
      });
      
      expect(vi.mocked(toast.success)).toHaveBeenCalledWith('Đã đánh dấu nội dung hoàn thành.');
      expect(screen.getByRole('slider')).toHaveValue('100');
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
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'IN_PROGRESS', progressPercent: 50 }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      
      expect(resetMutation.mutate).not.toHaveBeenCalled();
    });

    it('calls reset if confirmed', () => {
      confirmSpy.mockReturnValue(true);
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'IN_PROGRESS', progressPercent: 50 }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      
      expect(resetMutation.mutate).toHaveBeenCalledTimes(1);
      expect(resetMutation.mutate).toHaveBeenCalledWith(
        { contentType: 'TUTORIAL', contentKey: 'java-basic' },
        expect.any(Object)
      );
    });

    it('resets draft on success', () => {
      confirmSpy.mockReturnValue(true);
      vi.mocked(useLearningProgress).mockReturnValue({
        data: createProgress({ status: 'IN_PROGRESS', progressPercent: 50 }),
        isLoading: false, isError: false, isSuccess: true
      } as never);
      renderWithProviders(<LearningProgressControls {...defaultProps} />);
      
      // dirty the draft first
      fireEvent.change(screen.getByRole('slider'), { target: { value: '90' } });
      
      fireEvent.click(screen.getByRole('button', { name: /Đặt lại/i }));
      
      const onSuccess = resetMutation.mutate.mock.calls[0][1].onSuccess;
      
      act(() => {
        vi.mocked(useLearningProgress).mockReturnValue({
          data: createProgress({ status: 'NOT_STARTED', progressPercent: 0 }),
          isLoading: false, isError: false, isSuccess: true
        } as never);
        onSuccess();
      });
      
      expect(vi.mocked(toast.success)).toHaveBeenCalledWith('Đã đặt lại tiến độ.');
      expect(screen.getByRole('slider')).toHaveValue('0');
      expect(screen.getByRole('button', { name: /Lưu tiến độ/i })).toBeDisabled(); // dirty is reset
    });
  });
});
