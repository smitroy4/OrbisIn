import { useState } from "react";
import { ApiError, api, type CommentDto, type PostDto } from "../lib/api";
import { href } from "../lib/router";
import { Avatar } from "./chrome";
import {
  CommentIcon,
  HeartIcon,
  RepostIcon,
  SendIcon,
} from "../icons";

export function formatTime(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const diff = Date.now() - d.getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "Just now";
  if (mins < 60) return `${mins}m`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d`;
  return d.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export interface LocalComment {
  id: string;
  author: string;
  text: string;
  createdAt: string;
}

interface PostCardProps {
  post: PostDto;
  author: string;
  headline?: string;
  liked: boolean;
  likeCount?: number;
  busy: boolean;
  onToggleLike: (post: PostDto) => void;
  token: string;
  currentUserId: number;
  currentUserName: string;
  authorNames?: Map<number, string>;
}

export function PostCard({
  post,
  author,
  headline,
  liked,
  likeCount,
  busy,
  onToggleLike,
  token,
  currentUserId,
  currentUserName,
  authorNames,
}: PostCardProps) {
  const [expanded, setExpanded] = useState(false);
  const [comments, setComments] = useState<CommentDto[] | null>(null);
  const [loadingComments, setLoadingComments] = useState(false);
  const [commentsError, setCommentsError] = useState("");
  const [draft, setDraft] = useState("");
  const [posting, setPosting] = useState(false);
  const [reposted, setReposted] = useState(false);

  const toggleExpanded = () => {
    const next = !expanded;
    setExpanded(next);
    if (next && comments === null) {
      setLoadingComments(true);
      setCommentsError("");
      api
        .comments(token, post.id)
        .then(setComments)
        .catch((err) =>
          setCommentsError(err instanceof Error ? err.message : "Could not load comments."),
        )
        .finally(() => setLoadingComments(false));
    }
  };

  const addComment = () => {
    const text = draft.trim();
    if (!text || posting) return;
    setPosting(true);
    setCommentsError("");
    api
      .addComment(token, post.id, text)
      .then((c) => {
        setComments((prev) => [...(prev ?? []), c]);
        setDraft("");
      })
      .catch((err) =>
        setCommentsError(err instanceof Error ? err.message : "Could not post comment."),
      )
      .finally(() => setPosting(false));
  };

  const removeComment = (id: number) => {
    api
      .deleteComment(token, id)
      .then(() => setComments((prev) => (prev ?? []).filter((c) => c.id !== id)))
      .catch((err) =>
        setCommentsError(err instanceof Error ? err.message : "Could not delete comment."),
      );
  };

  const nameOf = (userId: number) => {
    if (userId === currentUserId) return currentUserName || "You";
    return authorNames?.get(userId) ?? `User ${userId}`;
  };

  const share = () => {
    const url = `${window.location.origin}${window.location.pathname}${href("post", post.id)}`;
    void navigator.clipboard?.writeText(url).catch(() => {});
    setReposted(true);
    window.setTimeout(() => setReposted(false), 1500);
  };

  return (
    <article className="card">
      <div className="posthead">
        <Avatar name={author} size={48} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="who">{author}</div>
          <div className="headline">
            {headline ?? `Member ID ${post.userId}`}
          </div>
          <div className="time">
            {formatTime(post.createdAt)} · Post #{post.id}
          </div>
        </div>
      </div>
      <p className="postbody">{post.content}</p>
      {post.imageUrl && (
        <div style={{ padding: "0 16px 12px" }}>
          <img
            src={post.imageUrl}
            alt="Post attachment"
            className="postimage"
            loading="lazy"
          />
        </div>
      )}
      <div className="poststats">
        {(likeCount ?? (liked ? 1 : 0)) > 0 && (
          <span>👍 {likeCount ?? (liked ? 1 : 0)}</span>
        )}
        {(comments?.length ?? 0) > 0 && (
          <span style={{ marginLeft: "auto" }}>
            {comments!.length} comment{comments!.length === 1 ? "" : "s"}
          </span>
        )}
      </div>
      <div className="postbar">
        <button
          className={liked ? "paction on" : "paction"}
          disabled={busy}
          onClick={() => onToggleLike(post)}
        >
          <HeartIcon filled={liked} />
          Like
        </button>
        <button
          className={expanded ? "paction on" : "paction"}
          onClick={toggleExpanded}
        >
          <CommentIcon />
          Comment
        </button>
        <button className="paction" onClick={share}>
          <RepostIcon />
          {reposted ? "Link copied!" : "Repost"}
        </button>
        <button
          className="paction"
          onClick={() => (window.location.hash = `#/post/${post.id}`)}
        >
          <SendIcon />
          Open
        </button>
      </div>
      {expanded && (
        <div className="comments">
          {commentsError && <div className="error">{commentsError}</div>}
          {loadingComments ? (
            <p className="note">Loading comments…</p>
          ) : (
            (comments ?? []).map((c) => (
              <div className="comment" key={c.id}>
                <Avatar name={nameOf(c.userId)} size={32} />
                <div className="cbubble" style={{ flex: 1 }}>
                  <div className="cwho">{nameOf(c.userId)}</div>
                  <div>{c.content}</div>
                  <div className="note">{formatTime(c.createdAt)}</div>
                </div>
                {c.userId === currentUserId && (
                  <button
                    className="btn small plain"
                    onClick={() => removeComment(c.id)}
                    aria-label="Delete comment"
                  >
                    Delete
                  </button>
                )}
              </div>
            ))
          )}
          {comments !== null && comments.length === 0 && !loadingComments && (
            <p className="note">No comments yet. Start the conversation.</p>
          )}
          <div className="commentform">
            <Avatar name={currentUserName || "Y"} size={32} />
            <input
              placeholder="Add a comment…"
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") addComment();
              }}
            />
            <button
              className="btn small secondary"
              onClick={addComment}
              disabled={!draft.trim() || posting}
            >
              {posting ? "Posting…" : "Post"}
            </button>
          </div>
        </div>
      )}
    </article>
  );
}

export async function toggleLike(
  liked: boolean,
  like: () => Promise<void>,
  unlike: () => Promise<void>,
): Promise<boolean> {
  if (liked) {
    try {
      await unlike();
      return false;
    } catch (err) {
      if (err instanceof ApiError && err.status === 400) return false;
      throw err;
    }
  }
  try {
    await like();
    return true;
  } catch (err) {
    if (err instanceof ApiError && err.status === 400) return true;
    throw err;
  }
}
