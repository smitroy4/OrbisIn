import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";
import { api, uploadFile, type Person, type PostDto } from "../lib/api";
import { useAuth } from "../lib/auth";
import { href } from "../lib/router";
import { PostCard, toggleLike } from "../components/posts";
import { Avatar } from "../components/chrome";

interface FeedPost extends PostDto {
  authorName: string;
}

const NEWS = [
  { title: "Microservices hiring up 18% this quarter", meta: "2h ago · 4,120 readers" },
  { title: "Kafka event-driven patterns, explained", meta: "5h ago · 1,830 readers" },
  { title: "Neo4j graphs for professional networks", meta: "8h ago · 964 readers" },
  { title: "Spring Gateway JWT best practices", meta: "1d ago · 2,410 readers" },
];

export function Feed({ query }: { query: string }) {
  const { session } = useAuth();
  const [posts, setPosts] = useState<FeedPost[]>([]);
  const [names, setNames] = useState<Map<number, string>>(new Map());
  const [liked, setLiked] = useState<Set<number>>(new Set());
  const [busyIds, setBusyIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [draft, setDraft] = useState("");
  const [posting, setPosting] = useState(false);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [imageKey, setImageKey] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [sort, setSort] = useState<"top" | "recent">("recent");

  const load = useCallback(async () => {
    if (!session) return;
    setLoading(true);
    setError("");
    try {
      let people: Person[] = [];
      try {
        people = await api.connections(session.token, session.userId);
      } catch {
        people = [];
      }
      const nm = new Map<number, string>();
      for (const p of people) nm.set(p.userId, p.name);
      setNames(nm);
      const mine = await api.myPosts(session.token, session.userId);
      const others = await Promise.all(
        people.map((p) =>
          api.userPosts(session.token, p.userId).catch((): PostDto[] => []),
        ),
      );
      const all: FeedPost[] = [
        ...mine.map((post) => ({
          ...post,
          authorName: session.name || "You",
        })),
        ...others.flat().map((post) => ({
          ...post,
          authorName: nm.get(post.userId) ?? `User ${post.userId}`,
        })),
      ].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt));
      setPosts(all);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load the feed.");
    } finally {
      setLoading(false);
    }
  }, [session]);

  useEffect(() => {
    void load();
  }, [load]);

  const publish = async (e: FormEvent) => {
    e.preventDefault();
    if (!session || (!draft.trim() && !imageUrl) || uploading) return;
    setPosting(true);
    setError("");
    try {
      const created = await api.createPost(session.token, draft.trim(), imageKey);
      setPosts((prev) => [
        { ...created, authorName: session.name || "You" },
        ...prev,
      ]);
      setDraft("");
      setImageUrl(null);
      setImageKey(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not publish the post.");
    } finally {
      setPosting(false);
    }
  };

  const onPickImage = async (file: File | undefined) => {
    if (!session || !file) return;
    setUploading(true);
    setError("");
    try {
      const res = await uploadFile(session.token, file, "post-images");
      // Preview uses the short-lived URL; the storage key is what gets saved
      // on the post (backend resolves a fresh URL on every read).
      setImageUrl(res.url);
      setImageKey(res.key);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Image upload failed.");
    } finally {
      setUploading(false);
    }
  };

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

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase();
    let list = posts;
    if (q)
      list = list.filter(
        (p) =>
          p.content.toLowerCase().includes(q) ||
          p.authorName.toLowerCase().includes(q),
      );
    if (sort === "top")
      return [...list].sort((a, b) => Number(liked.has(b.id)) - Number(liked.has(a.id)));
    return list;
  }, [posts, query, sort, liked]);

  if (!session) return null;

  return (
    <div className="page">
      <div className="feedgrid">
        <aside className="sidecol">
          <div className="card">
            <div className="profilecover" />
            <div style={{ paddingBottom: 12 }}>
              <Avatar name={session.name || session.email} size={72} />
              <div style={{ padding: "0 16px" }}>
                <div style={{ fontWeight: 700, fontSize: 16 }}>
                  {session.name || "Member"}
                </div>
                <div className="note">Professional · Member #{session.userId}</div>
                <div className="note">{session.email}</div>
              </div>
            </div>
            <a className="sidelink" href={href("network")}>
              Connections<strong>{names.size} in network</strong>
            </a>
            <a className="sidelink" href={href("profile")}>
              View profile
            </a>
          </div>
        </aside>

        <section>
          <div className="card composer">
            <div className="composer-row">
              <Avatar name={session.name || session.email} />
              <button
                className="startpost"
                onClick={() =>
                  document.getElementById("composer-text")?.focus()
                }
              >
                Start a post…
              </button>
            </div>
            <form onSubmit={publish} style={{ marginTop: 8 }}>
              <div className="field" style={{ marginBottom: 8 }}>
                <textarea
                  id="composer-text"
                  value={draft}
                  onChange={(e) => setDraft(e.target.value)}
                  placeholder="What do you want to talk about?"
                  rows={2}
                />
              </div>
              {imageUrl && (
                <div style={{ position: "relative", marginBottom: 8 }}>
                  <img
                    src={imageUrl}
                    alt="Upload preview"
                    style={{ width: "100%", maxHeight: 220, objectFit: "cover", borderRadius: 4 }}
                  />
                  <button
                    type="button"
                    className="btn small plain"
                    style={{ position: "absolute", top: 8, right: 8 }}
                    onClick={() => {
                      setImageUrl(null);
                      setImageKey(null);
                    }}
                  >
                    Remove
                  </button>
                </div>
              )}
              <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                <label className="btn small secondary" style={{ cursor: "pointer" }}>
                  {uploading ? "Uploading…" : "Upload"}
                  <input
                    type="file"
                    accept="image/*"
                    hidden
                    disabled={uploading || posting}
                    onChange={(e) => {
                      void onPickImage(e.target.files?.[0]);
                      e.target.value = "";
                    }}
                  />
                </label>
                <button
                  className="btn small"
                  type="submit"
                  disabled={posting || uploading || (!draft.trim() && !imageUrl)}
                >
                  {posting ? "Posting…" : "Post"}
                </button>
              </div>
            </form>
          </div>

          <div className="toolbar">
            <select
              value={sort}
              onChange={(e) => setSort(e.target.value as "top" | "recent")}
              aria-label="Sort feed"
            >
              <option value="recent">Most recent</option>
              <option value="top">Top</option>
            </select>
            {query.trim() && (
              <span className="note">
                {visible.length} result{visible.length === 1 ? "" : "s"} for “{query.trim()}”
              </span>
            )}
          </div>

          {error && <div className="error">{error}</div>}
          {loading ? (
            <div className="card skeleton">Loading your feed…</div>
          ) : visible.length === 0 ? (
            <div className="card cardpad">
              <h3>No posts to show</h3>
              <p className="note">
                {query.trim()
                  ? "Try a different search."
                  : "Be the first to post, or grow your network to see more."}
              </p>
            </div>
          ) : (
            visible.map((post) => (
              <PostCard
                key={post.id}
                post={post}
                author={post.authorName}
                liked={liked.has(post.id)}
                busy={busyIds.has(post.id)}
                onToggleLike={onToggleLike}
                token={session.token}
                currentUserId={session.userId}
                currentUserName={session.name || "You"}
                authorNames={names}
              />
            ))
          )}
        </section>

        <aside className="sidecol">
          <div className="card cardpad">
            <h3>OrbisIn News</h3>
            {NEWS.map((n) => (
              <div key={n.title} style={{ marginBottom: 12 }}>
                <div style={{ fontWeight: 700, fontSize: 13 }}>• {n.title}</div>
                <div className="note">{n.meta}</div>
              </div>
            ))}
          </div>
          <div className="card cardpad">
            <h3>Add to your feed</h3>
            <p className="note">Follow graph members from the My Network page.</p>
            <a href={href("network")}>Find connections →</a>
          </div>
        </aside>
      </div>
    </div>
  );
}
