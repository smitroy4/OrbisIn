import { useCallback, useEffect, useState } from "react";
import { api, type PostDto } from "../lib/api";
import { useAuth } from "../lib/auth";
import { PostCard, toggleLike } from "../components/posts";
import { Avatar } from "../components/chrome";

export function Profile() {
  const { session } = useAuth();
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [liked, setLiked] = useState<Set<number>>(new Set());
  const [busyIds, setBusyIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [headline, setHeadline] = useState(
    () => localStorage.getItem("orbisin.headline") ?? "",
  );
  const [editing, setEditing] = useState(false);

  const load = useCallback(async () => {
    if (!session) return;
    setLoading(true);
    setError("");
    try {
      setPosts(await api.myPosts(session.token, session.userId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load profile.");
    } finally {
      setLoading(false);
    }
  }, [session]);

  useEffect(() => {
    void load();
  }, [load]);

  const onToggleLike = async (post: PostDto) => {
    if (!session) return;
    setBusyIds((prev) => new Set(prev).add(post.id));
    try {
      const next = await toggleLike(
        liked.has(post.id),
        () => api.likePost(session.token, post.id),
        () => api.unlikePost(session.token, post.id),
      );
      setLiked((prev) => {
        const copy = new Set(prev);
        if (next) copy.add(post.id);
        else copy.delete(post.id);
        return copy;
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Like action failed.");
    } finally {
      setBusyIds((prev) => {
        const copy = new Set(prev);
        copy.delete(post.id);
        return copy;
      });
    }
  };

  if (!session) return null;
  const name = session.name || "Member";

  return (
    <div className="page">
      <div className="singlecol">
        <div className="card">
          <div className="profilecover" style={{ height: 120 }} />
          <div style={{ padding: "0 24px 16px" }}>
            <Avatar name={name} size={120} />
            <h2 style={{ margin: "8px 0 0", fontSize: 24 }}>{name}</h2>
            <div className="sub">
              {headline || "Professional headline — click edit to add yours"}
            </div>
            <div className="note">
              {session.email} · Member ID {session.userId} · {posts.length} post
              {posts.length === 1 ? "" : "s"}
            </div>
            <div className="btnrow">
              {!editing ? (
                <button className="btn small secondary" onClick={() => setEditing(true)}>
                  Edit headline
                </button>
              ) : (
                <form
                  style={{ display: "flex", gap: 8, flex: 1 }}
                  onSubmit={(e) => {
                    e.preventDefault();
                    localStorage.setItem("orbisin.headline", headline);
                    setEditing(false);
                  }}
                >
                  <input
                    value={headline}
                    onChange={(e) => setHeadline(e.target.value)}
                    placeholder="e.g. Backend Engineer @ OrbisIn"
                    style={{ flex: 1, font: "inherit", padding: "6px 10px" }}
                  />
                  <button className="btn small" type="submit">Save</button>
                </form>
              )}
            </div>
          </div>
        </div>

        <div className="card cardpad">
          <h3>About</h3>
          <p className="sub">
            {headline
              ? `${name} — ${headline}. Active on OrbisIn with ${posts.length} published post(s).`
              : `${name} is a member of the OrbisIn professional network.`}
          </p>
        </div>

        <div className="card cardpad">
          <h3>Experience</h3>
          <div className="jobrow">
            <div className="jtitle">Member, OrbisIn Network</div>
            <div className="jmeta">2026 – Present · Professional community</div>
          </div>
        </div>

        <h3 style={{ margin: "16px 0 8px" }}>Activity</h3>
        {error && <div className="error">{error}</div>}
        {loading ? (
          <div className="card skeleton">Loading posts…</div>
        ) : posts.length === 0 ? (
          <div className="card cardpad">
            <p className="note">No posts yet. Publish from the Home feed.</p>
          </div>
        ) : (
          [...posts]
            .sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt))
            .map((post) => (
              <PostCard
                key={post.id}
                post={post}
                author={name}
                headline={headline || undefined}
                liked={liked.has(post.id)}
                busy={busyIds.has(post.id)}
                onToggleLike={onToggleLike}
                token={session.token}
                currentUserId={session.userId}
                currentUserName={name}
              />
            ))
        )}
      </div>
    </div>
  );
}
