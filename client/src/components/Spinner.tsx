// https://gist.github.com/tadeaspetak/c51e070b78a6251e4834baba2184254f
export const Spinner: React.FC<{
  className?: string;
  size: 4 | 6 | 8 | 12 | 16 | 24;
  stroke?: number;
}> = ({ className = "", size, stroke = 1 }) => {
  return (
    <svg
      className={`text-green-700 mx-auto animate-spin w-${size} ${className}`}
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="-2 -2 28 28"
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="12"
        stroke="currentColor"
        strokeWidth={stroke}
      ></circle>
      <path
        className="opacity-75"
        fill="none"
        strokeWidth="1"
        stroke="currentColor"
        d="M 0 12 a 12 12 0 0 0 19 9.745"
      ></path>
    </svg>
  );
};
