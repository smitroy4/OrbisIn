import { useCallback, useEffect, useState } from "react";
import { api, type PostDto } from "../lib/api";
import { useAuth } from "../lib/auth";
import { PostCard, toggleLike } from "../components/posts";

export function PostDetail({ postId }: { postId: number }) {
  const { session } = useAuth();
  const [post, setPost] = useState<PostDto | null>(null);
  const [liked, setLiked] = useState(false);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    if (!session) return;
    setLoading(true);
    setError("");
    try {
      setPost(await api.postById(session.token, postId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load the post.");
    } finally {
      setLoading(false);
    }
  }, [session, postId]);

  useEffect(() => {
    void load();
  }, [load]);

  const onToggleLike = async (p: PostDto) => {
    if (!session) return;
    setBusy(true);
    try {
      setLiked(
        await toggleLike(
          liked,
          () => api.likePost(session.token, p.id),
          () => api.unlikePost(session.token, p.id),
        ),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Like action failed.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="page">
      <div className="singlecol">
        {error && <div className="error">{error}</div>}
        {loading ? (
          <div className="card skeleton">Loading post…</div>
        ) : post ? (
          <PostCard
            post={post}
            author={
              post.userId === session?.userId
                ? session?.name || "You"
                : `User ${post.userId}`
            }
            liked={liked}
            busy={busy}
            onToggleLike={onToggleLike}
            token={session!.token}
            currentUserId={session!.userId}
            currentUserName={session?.name || "You"}
          />
        ) : (
          !error && (
            <div className="card cardpad"><p className="note">Post not found.</p></div>
          )
        )}
      </div>
    </div>
  );
}
