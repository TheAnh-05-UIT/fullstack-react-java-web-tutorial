import { HTMLAttributes, forwardRef, useState } from 'react';
import { User } from 'lucide-react';

interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  src?: string | null;
  alt?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

const sizeClasses = {
  sm: 'w-8 h-8',
  md: 'w-10 h-10',
  lg: 'w-12 h-12',
  xl: 'w-16 h-16',
};

const iconSizes = {
  sm: 'w-4 h-4',
  md: 'w-5 h-5',
  lg: 'w-6 h-6',
  xl: 'w-8 h-8',
};

export const Avatar = forwardRef<HTMLDivElement, AvatarProps>(
  ({ className = '', src, alt = 'User avatar', size = 'md', ...props }, ref) => {
    const [imgError, setImgError] = useState(false);

    return (
      <div
        ref={ref}
        className={`${sizeClasses[size]} rounded-full overflow-hidden bg-gray-100 dark:bg-gray-800 flex items-center justify-center shrink-0 ${className}`}
        {...props}
      >
        {src && !imgError ? (
          <img 
            src={src} 
            alt={alt} 
            className="w-full h-full object-cover"
            onError={() => setImgError(true)} 
          />
        ) : (
          <User className={`${iconSizes[size]} text-gray-400`} />
        )}
      </div>
    );
  }
);

Avatar.displayName = 'Avatar';
