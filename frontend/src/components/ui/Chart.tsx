interface ProgressRingProps {
  progress: number;
  size?: number;
  strokeWidth?: number;
  className?: string;
}

export function ProgressRing({ progress, size = 120, strokeWidth = 10, className = '' }: ProgressRingProps) {
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  const offset = circumference - (progress / 100) * circumference;

  return (
    <div className={`relative inline-flex items-center justify-center ${className}`}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          className="text-gray-200 dark:text-gray-700"
          strokeWidth={strokeWidth}
          stroke="currentColor"
          fill="transparent"
          r={radius}
          cx={size / 2}
          cy={size / 2}
        />
        <circle
          className="text-primary-600"
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          stroke="currentColor"
          fill="transparent"
          r={radius}
          cx={size / 2}
          cy={size / 2}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <span className="text-2xl font-semibold text-gray-900 dark:text-gray-100">{progress}%</span>
      </div>
    </div>
  );
}

interface SimpleBarChartProps {
  data: { date: string; value: number }[];
  height?: number;
}

export function SimpleBarChart({ data, height = 120 }: SimpleBarChartProps) {
  const maxValue = Math.max(...data.map(d => d.value));

  return (
    <div className="w-full" style={{ height }}>
      <div className="flex items-end justify-between h-full gap-2">
        {data.map((item, index) => (
          <div key={index} className="flex flex-col items-center gap-2 flex-1">
            <div
              className="w-full bg-primary-100 dark:bg-primary-900/30 rounded-t-sm transition-all duration-300 hover:bg-primary-200 dark:hover:bg-primary-900/50"
              style={{ height: `${(item.value / maxValue) * 100}%`, minHeight: 4 }}
            />
            <span className="text-xs text-gray-500 dark:text-gray-400">{item.date}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

interface DonutChartProps {
  data: { name: string; value: number; color: string }[];
  size?: number;
}

export function DonutChart({ data, size = 180 }: DonutChartProps) {
  const total = data.reduce((sum, item) => sum + item.value, 0);
  let currentAngle = -90;

  const segments = data.map(item => {
    const angle = (item.value / total) * 360;
    const startAngle = currentAngle;
    currentAngle += angle;
    return { ...item, startAngle, angle };
  });

  const createArc = (startAngle: number, angle: number, radius: number) => {
    const x1 = radius + radius * Math.cos((startAngle * Math.PI) / 180);
    const y1 = radius + radius * Math.sin((startAngle * Math.PI) / 180);
    const x2 = radius + radius * Math.cos(((startAngle + angle) * Math.PI) / 180);
    const y2 = radius + radius * Math.sin(((startAngle + angle) * Math.PI) / 180);
    const largeArc = angle > 180 ? 1 : 0;

    return `M ${radius} ${radius} L ${x1} ${y1} A ${radius} ${radius} 0 ${largeArc} 1 ${x2} ${y2} Z`;
  };

  return (
    <div className="flex items-center gap-6">
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        {segments.map((segment, index) => (
          <path
            key={index}
            d={createArc(segment.startAngle, segment.angle, size / 2 - 20)}
            fill={segment.color}
            className="transition-opacity hover:opacity-80"
          />
        ))}
        <circle cx={size / 2} cy={size / 2} r={size / 2 - 40} fill="white" className="dark:fill-gray-900" />
      </svg>
      <div className="flex flex-col gap-2">
        {data.map((item, index) => (
          <div key={index} className="flex items-center gap-2">
            <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
            <span className="text-sm text-gray-600 dark:text-gray-400">{item.name}</span>
            <span className="text-sm font-medium text-gray-900 dark:text-gray-100 ml-auto">{item.value}%</span>
          </div>
        ))}
      </div>
    </div>
  );
}
