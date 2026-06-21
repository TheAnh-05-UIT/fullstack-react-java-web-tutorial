import React, { useState, useRef } from 'react';
import { Upload, X, Loader2, Image as ImageIcon } from 'lucide-react';
import { api } from '../../services/api';

interface ImageUploadProps {
  value?: string;
  onChange: (url: string) => void;
  className?: string;
  folder?: 'tutorials' | 'projects' | 'roadmaps' | 'users' | 'general';
}

export function ImageUpload({ value, onChange, className = '', folder = 'general' }: ImageUploadProps) {
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Check file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      setError('File size must be less than 5MB');
      return;
    }

    // Check file type
    if (!file.type.startsWith('image/')) {
      setError('Only image files are allowed');
      return;
    }

    setError(null);
    setIsUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', file);

      // Use the pre-configured api instance to handle tokens and response wrapping
      const endpoint = folder ? `/upload?folder=${folder}` : '/upload';
      const data: any = await api.post(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (data && data.url) {
        onChange(data.url);
      } else if (data && typeof data === 'string') {
        onChange(data);
      } else {
        throw new Error('No URL returned');
      }
    } catch (err) {
      console.error('Upload error:', err);
      setError('Failed to upload image. Please try again.');
    } finally {
      setIsUploading(false);
      // Reset input so the same file can be selected again
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  return (
    <div className={`space-y-4 ${className}`}>
      {value ? (
        <div className="relative rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700 aspect-video w-full max-w-sm bg-gray-50 dark:bg-gray-900 flex items-center justify-center group">
          <img src={value} alt="Uploaded" className="object-cover w-full h-full" />
          <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
            <button
              type="button"
              onClick={() => onChange('')}
              className="p-2 bg-red-600 text-white rounded-full hover:bg-red-700 transition-colors shadow-lg"
              title="Remove image"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>
      ) : (
        <div 
          onClick={() => fileInputRef.current?.click()}
          className="relative rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700 hover:border-primary-500 dark:hover:border-primary-500 transition-colors aspect-video w-full max-w-sm bg-gray-50 dark:bg-gray-900/50 flex flex-col items-center justify-center cursor-pointer group overflow-hidden"
        >
          {isUploading ? (
            <div className="flex flex-col items-center gap-3 text-primary-600">
              <Loader2 className="w-8 h-8 animate-spin" />
              <span className="text-sm font-medium">Uploading...</span>
            </div>
          ) : (
            <>
              <div className="p-4 bg-white dark:bg-gray-800 rounded-full shadow-sm mb-3 group-hover:scale-110 transition-transform">
                <Upload className="w-6 h-6 text-gray-400 dark:text-gray-500 group-hover:text-primary-500 transition-colors" />
              </div>
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">Click to upload image</p>
              <p className="text-xs text-gray-500 mt-1">SVG, PNG, JPG or GIF (max. 5MB)</p>
            </>
          )}
        </div>
      )}
      
      {error && (
        <p className="text-sm text-red-600 dark:text-red-400 font-medium flex items-center gap-1">
          <X className="w-4 h-4" /> {error}
        </p>
      )}

      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        accept="image/*"
        className="hidden"
      />
      
      {/* Hidden input to store the URL value for form submission if needed, but since we use Controller, it's not strictly necessary */}
    </div>
  );
}
