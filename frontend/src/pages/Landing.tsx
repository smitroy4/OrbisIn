import { href } from "../lib/router";
import { useAuth } from "../lib/auth";

export function Landing() {
  const { session } = useAuth();
  return (
    <div className="authwrap">
      <div className="hero">
        <h1>
          Welcome to your
          <br />
          professional community
        </h1>
        <div className="btnrow">
          {session ? (
            <a className="btn" href={href("feed")}>
              Go to feed
            </a>
          ) : (
            <>
              <a className="btn secondary" href={href("login")}>
                Sign in
              </a>
              <a className="btn" href={href("signup")}>
                Join now
              </a>
            </>
          )}
        </div>
        <p style={{ marginTop: 24 }}>
          Connect with colleagues, share updates, and catch up on
          alerts — all powered by OrbisIn microservices (gateway, user, posts,
          connections graph, notifications).
        </p>
      </div>
      <div>
        <div className="card cardpad">
          <h3>Explore</h3>
          <div className="jobrow">
            <div className="jtitle">Share an update</div>
            <div className="jmeta">Publish posts from your Home feed</div>
            <div style={{ marginTop: 8 }}>
              <a href={href("feed")}>Open feed →</a>
            </div>
          </div>
          <div className="jobrow">
            <div className="jtitle">Grow your network</div>
            <div className="jmeta">Accept invitations from the graph</div>
            <div style={{ marginTop: 8 }}>
              <a href={href("network")}>My Network →</a>
            </div>
          </div>
          <div className="jobrow">
            <div className="jtitle">Never miss an update</div>
            <div className="jmeta">Likes and activity land in Notifications</div>
            <div style={{ marginTop: 8 }}>
              <a href={href("notifications")}>View alerts →</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
