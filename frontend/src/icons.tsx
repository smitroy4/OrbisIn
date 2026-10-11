import type { ReactNode } from "react";

interface IconProps {
  size?: number;
  filled?: boolean;
}

function base(size: number, children: ReactNode, fill = false) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill={fill ? "currentColor" : "none"}
      stroke={fill ? "none" : "currentColor"}
      strokeWidth={fill ? 0 : 1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  );
}

export const MarkIcon = ({ size = 34 }: IconProps) => (
  <svg width={size} height={size} viewBox="0 0 34 34" aria-hidden="true">
    <rect x="1" y="1" width="32" height="32" rx="7" fill="#e8710a" />
    <ellipse
      cx="17"
      cy="17"
      rx="10.5"
      ry="6.5"
      fill="none"
      stroke="#fff"
      strokeWidth={2.4}
      transform="rotate(-24 17 17)"
    />
    <circle cx="17" cy="17" r="3.4" fill="#fff" />
    <circle cx="25.2" cy="11.4" r="2" fill="#ffe3c2" />
  </svg>
);

export const HomeIcon = ({ size = 24 }: IconProps) =>
  base(
    size,
    <>
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V21h14V9.5" />
      <path d="M10 21v-6h4v6" />
    </>,
  );

export const PeopleIcon = ({ size = 24 }: IconProps) =>
  base(
    size,
    <>
      <circle cx="9" cy="8" r="3.5" />
      <path d="M2.5 20a6.5 6.5 0 0 1 13 0" />
      <circle cx="17" cy="9" r="2.6" />
      <path d="M16.5 14.2A5.4 5.4 0 0 1 21.5 19" />
    </>,
  );

export const JobsIcon = ({ size = 24 }: IconProps) =>
  base(
    size,
    <>
      <rect x="3" y="7" width="18" height="13" rx="1.5" />
      <path d="M9 7V5.5A1.5 1.5 0 0 1 10.5 4h3A1.5 1.5 0 0 1 15 5.5V7" />
      <path d="M3 12.5h18" />
    </>,
  );

export const MsgIcon = ({ size = 24 }: IconProps) =>
  base(
    size,
    <>
      <path d="M4 5h16v11H9l-5 4V5Z" />
      <path d="M8 9.5h8M8 12.5h5" />
    </>,
  );

export const BellIcon = ({ size = 24 }: IconProps) =>
  base(
    size,
    <>
      <path d="M6 16v-5a6 6 0 0 1 12 0v5l1.5 2.5h-15L6 16Z" />
      <path d="M10 21a2 2 0 0 0 4 0" />
    </>,
  );

export const UserIcon = ({ size = 24 }: IconProps) =>
  base(
    size,
    <>
      <circle cx="12" cy="8" r="3.6" />
      <path d="M4.5 20.5a7.5 7.5 0 0 1 15 0" />
    </>,
  );

export const FeedIcon = HomeIcon;

export const SearchIcon = ({ size = 16 }: IconProps) =>
  base(
    size,
    <>
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4.5 4.5" />
    </>,
  );

export const PlusIcon = ({ size = 15 }: IconProps) =>
  base(
    size,
    <>
      <path d="M12 5v14M5 12h14" />
    </>,
  );

export const HeartIcon = ({ size = 20, filled = false }: IconProps) =>
  base(
    size,
    <path d="M12 20.5C7 16.5 3 13.2 3 9.3 3 6.4 5.2 4.5 7.7 4.5c1.7 0 3.3.9 4.3 2.4 1-1.5 2.6-2.4 4.3-2.4 2.5 0 4.7 1.9 4.7 4.8 0 3.9-4 7.2-9 11.2Z" />,
    filled,
  );

export const CommentIcon = ({ size = 20 }: IconProps) =>
  base(
    size,
    <>
      <path d="M4 5h16v10H9l-5 4V5Z" />
    </>,
  );

export const RepostIcon = ({ size = 20 }: IconProps) =>
  base(
    size,
    <>
      <path d="M4 9a8 8 0 0 1 14-3l2 2" />
      <path d="M20 4v5h-5" />
      <path d="M20 15a8 8 0 0 1-14 3l-2-2" />
      <path d="M4 20v-5h5" />
    </>,
  );

export const SendIcon = ({ size = 20 }: IconProps) =>
  base(
    size,
    <>
      <path d="M21 3 10 14" />
      <path d="M21 3 14 21l-4-7-7-4 18-7Z" />
    </>,
  );

export const PhotoIcon = ({ size = 20 }: IconProps) =>
  base(
    size,
    <>
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <circle cx="9" cy="10" r="1.6" />
      <path d="m5 18 5-5 3 3 3-3 3 3" />
    </>,
  );

export const VideoIcon = ({ size = 20 }: IconProps) =>
  base(
    size,
    <>
      <rect x="3" y="6" width="13" height="12" rx="2" />
      <path d="m16 10 5-3v10l-5-3" />
    </>,
  );

export const ArticleIcon = ({ size = 20 }: IconProps) =>
  base(
    size,
    <>
      <rect x="5" y="3" width="14" height="18" rx="1.5" />
      <path d="M9 8h6M9 12h6M9 16h4" />
    </>,
  );

export const CaretIcon = ({ size = 14 }: IconProps) =>
  base(
    size,
    <>
      <path d="m6 9 6 6 6-6" />
    </>,
  );
