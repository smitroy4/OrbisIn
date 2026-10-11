import { useState, type FormEvent } from "react";
import { useAuth } from "../lib/auth";
import { href, navigate } from "../lib/router";

function Shell({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <div className="authwrap">
      <div className="hero">
        <h1>
          Welcome to your
          <br />
          professional community
        </h1>
        <p>
          Stay in touch with your network, share updates, and follow
          opportunities on OrbisIn — backed by live microservices.
        </p>
      </div>
      <div>
        <h2 style={{ fontSize: 28, fontWeight: 600, margin: "0 0 4px" }}>{title}</h2>
        <p className="note" style={{ marginTop: 0 }}>{subtitle}</p>
        <div className="form-card" style={{ marginTop: 16 }}>{children}</div>
      </div>
    </div>
  );
}

export function Login() {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await login(email.trim(), password);
      navigate("feed");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Sign in failed.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <Shell title="Sign in" subtitle="Stay updated on your professional world">
      <form onSubmit={submit}>
        {error && <div className="error">{error}</div>}
        <div className="field">
          <label htmlFor="login-email">Email</label>
          <input
            id="login-email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
        </div>
        <div className="field">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </div>
        <button className="btn" type="submit" disabled={busy} style={{ width: "100%", justifyContent: "center" }}>
          {busy ? "Signing in…" : "Sign in"}
        </button>
        <p className="note">
          New to OrbisIn? <a href={href("signup")}>Join now</a>
        </p>
      </form>
    </Shell>
  );
}

export function Signup() {
  const { signup } = useAuth();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await signup(name.trim(), email.trim(), password);
      navigate("feed");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Signup failed.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <Shell title="Join OrbisIn" subtitle="Make the most of your professional life">
      <form onSubmit={submit}>
        {error && <div className="error">{error}</div>}
        <div className="field">
          <label htmlFor="signup-name">Full name</label>
          <input
            id="signup-name"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            autoComplete="name"
          />
        </div>
        <div className="field">
          <label htmlFor="signup-email">Email</label>
          <input
            id="signup-email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
        </div>
        <div className="field">
          <label htmlFor="signup-password">Password (6+ characters)</label>
          <input
            id="signup-password"
            type="password"
            required
            minLength={6}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
          />
        </div>
        <button className="btn" type="submit" disabled={busy} style={{ width: "100%", justifyContent: "center" }}>
          {busy ? "Joining…" : "Agree & Join"}
        </button>
        <p className="note">
          Already on OrbisIn? <a href={href("login")}>Sign in</a>
        </p>
      </form>
    </Shell>
  );
}
