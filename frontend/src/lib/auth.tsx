import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { api, userIdFromToken, type UserDto } from "./api";

interface Session {
  token: string;
  userId: number;
  name: string;
  email: string;
}

interface AuthValue {
  session: Session | null;
  login: (email: string, password: string) => Promise<void>;
  signup: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthValue | null>(null);

function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem("orbisin.session");
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Session;
    if (!parsed.token || !parsed.userId) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(loadSession);

  const persist = (s: Session | null) => {
    setSession(s);
    if (s) localStorage.setItem("orbisin.session", JSON.stringify(s));
    else localStorage.removeItem("orbisin.session");
  };

  const login = useCallback(async (email: string, password: string) => {
    const token = await api.login(email, password);
    const userId = userIdFromToken(token);
    if (userId === null) throw new Error("Login succeeded but token was unreadable.");
    persist({ token, userId, name: "", email });
  }, []);

  const signup = useCallback(
    async (name: string, email: string, password: string) => {
      const user: UserDto = await api.signup(name, email, password);
      const token = await api.login(email, password);
      persist({ token, userId: user.id, name: user.name, email: user.email });
    },
    [],
  );

  const logout = useCallback(() => persist(null), []);

  const value = useMemo(
    () => ({ session, login, signup, logout }),
    [session, login, signup, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
