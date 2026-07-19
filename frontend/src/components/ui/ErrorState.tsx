import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from './Button';

interface ErrorStateProps {
  title?: string;
  message?: string;
  error?: any;
  onRetry?: () => void;
  retryLabel?: string;
  className?: string;
}

export function ErrorState({
  title = 'Something went wrong',
  message,
  error,
  onRetry,
  retryLabel = 'Try Again',
  className = ''
}: ErrorStateProps) {
  // Ưu tiên message từ API hoặc prop an toàn, không hiển thị raw stack trace
  const safeMessage = React.useMemo(() => {
    if (message) return message;
    if (!error) return 'Unable to load data at this time. Please try again later.';
    
    if (typeof error === 'string') return error;
    if (Array.isArray(error?.message)) return error.message.join(', ');
    if (typeof error?.message === 'string') {
      // Tránh hiển thị stack trace thô nếu vô tình lọt vào message
      if (!error.message.includes('at ') && !error.message.includes('Error:')) {
        return error.message;
      }
    }
    return 'Unable to connect to server or load data. Please check your connection and try again.';
  }, [message, error]);

  return (
    <div className={`flex flex-col items-center justify-center text-center py-16 px-4 rounded-2xl border border-error-200 dark:border-error-900/50 bg-error-50/50 dark:bg-error-950/20 my-6 ${className}`}>
      <div className="w-16 h-16 mx-auto rounded-full bg-error-100 dark:bg-error-900/40 flex items-center justify-center mb-4 text-error-600 dark:text-error-400">
        <AlertCircle className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-bold text-gray-900 dark:text-gray-100 mb-2">
        {title}
      </h3>
      <p className="text-sm text-gray-600 dark:text-gray-300 max-w-md mb-6">
        {safeMessage}
      </p>
      {onRetry && (
        <Button variant="primary" onClick={onRetry} className="flex items-center gap-2">
          <RefreshCw className="w-4 h-4" />
          {retryLabel}
        </Button>
      )}
    </div>
  );
}
