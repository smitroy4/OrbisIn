import { useEffect, useRef, useState, type ReactNode } from "react";
import { useAuth } from "../lib/auth";
import { api } from "../lib/api";
import { href, useRoute } from "../lib/router";
import {
  BellIcon,
  CaretIcon,
  HomeIcon,
  MarkIcon,
  PeopleIcon,
  SearchIcon,
} from "../icons";

export function avatarColor(name: string): string {
  const palette = ["#56687a", "#7a5c56", "#567a5f", "#5f567a", "#776138"];
  let h = 0;
  for (const c of name) h = (h * 31 + c.charCodeAt(0)) % 997;
  return palette[h % palette.length];
}

export function Avatar({
  name,
  size = 48,
}: {
  name: string;
  size?: number;
}) {
  return (
    <div
      className="miniavatar"
      style={{
        width: size,
        height: size,
        fontSize: size * 0.38,
        background: avatarColor(name || "?"),
        flex: "none",
      }}
      aria-hidden="true"
    >
      {(name || "?").slice(0, 1).toUpperCase()}
    </div>
  );
}

export function TopBar({ onSearch }: { onSearch?: (q: string) => void }) {
  const { session, logout } = useAuth();
  const route = useRoute();
  const [unread, setUnread] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);
  const [q, setQ] = useState("");
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!session) {
      setUnread(0);
      return;
    }
    api
      .unreadCount(session.token, session.userId)
      .then((r) => setUnread(r.unreadCount))
      .catch(() => setUnread(0));
  }, [session, route.name]);

  useEffect(() => {
    const close = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node))
        setMenuOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  const item = (
    name: string,
    icon: ReactNode,
    label: string,
    badge?: number,
  ) => (
    <a
      href={href(name as never)}
      className={route.name === name ? "navitem active" : "navitem"}
    >
      {icon}
      <span>{label}</span>
      {badge !== undefined && badge > 0 && (
        <span className="nbadge">{badge}</span>
      )}
    </a>
  );

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <a className="brand" href={href(session ? "feed" : "home")} aria-label="OrbisIn home">
          <MarkIcon />
        </a>
        {session && (
          <div className="searchbox">
            <SearchIcon />
            <input
              placeholder="Search people, posts…"
              value={q}
              onChange={(e) => {
                setQ(e.target.value);
                onSearch?.(e.target.value);
              }}
              aria-label="Search"
            />
          </div>
        )}
        <nav className="nav">
          {session ? (
            <>
              {item("feed", <HomeIcon />, "Home")}
              {item("network", <PeopleIcon />, "My Network")}
              {item("notifications", <BellIcon />, "Notifications", unread)}
              <div className="dropdown" ref={menuRef}>
                <button
                  className="navitem me"
                  onClick={() => setMenuOpen((v) => !v)}
                  aria-haspopup="menu"
                >
                  <span className="meavatar">
                    {(session.name || "M").slice(0, 1).toUpperCase()}
                  </span>
                  <span className="lbl">Me</span>
                  <CaretIcon />
                </button>
                {menuOpen && (
                  <div className="menu" role="menu">
                    <div className="mehead">
                      <Avatar name={session.name || session.email} size={48} />
                      <div>
                        <div style={{ fontWeight: 700 }}>
                          {session.name || "Member"}
                        </div>
                        <div className="note">{session.email}</div>
                      </div>
                    </div>
                    <a href={href("profile")}>View Profile</a>
                    <a href={href("profile")}>My Posts</a>
                    <button
                      onClick={() => {
                        logout();
                        setMenuOpen(false);
                        window.location.hash = "#/";
                      }}
                    >
                      Sign out
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <a className="navitem" href={href("signup")}>
                Join now
              </a>
              <a className="navitem" href={href("login")}>
                Sign in
              </a>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

export function Footer() {
  return (
    <footer className="footer">
      <span>OrbisIn Corporation © 2026</span>
      <a href={href("terms")}>Terms of Service</a>
      <a href={href("privacy")}>Privacy Policy</a>
    </footer>
  );
}
