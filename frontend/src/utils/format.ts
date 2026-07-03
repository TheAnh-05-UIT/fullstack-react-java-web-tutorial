export function formatReadTime(readTime?: number | null, content?: string | null, description?: string | null): number {
  // tính thời gian đọc
  if (readTime && readTime > 0 && readTime <= 30) {
    return readTime;
  }
  const rawText = content || description || '';
  if (!rawText) return 5;

  // Loại bỏ chuỗi Base64 ảnh
  const cleanText = rawText
    .replace(/data:image\/[^;]+;base64,[a-zA-Z0-9+/=]+/g, '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/[^a-zA-Z0-9À-ỹ\s]/g, ' ');

  const words = cleanText.trim().split(/\s+/).filter(Boolean).length;
  const minutes = Math.ceil(words / 250);
  // Giới hạn thời gian đọc thực tế cho 1 bài tutorial chuẩn từ 3 đến 20 phút
  return Math.max(3, Math.min(20, minutes));
}

export function formatViews(views?: number | null, viewCount?: number | null): string | number {
  const v = viewCount ?? views ?? 0;
  return v >= 1000 ? (v / 1000).toFixed(1) + 'k' : v;
}
