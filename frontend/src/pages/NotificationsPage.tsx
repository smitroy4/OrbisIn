import { useCallback, useEffect, useState } from "react";
import { api, type NotificationItem } from "../lib/api";
import { useAuth } from "../lib/auth";
import { formatTime } from "../components/posts";
import { Avatar } from "../components/chrome";

export function NotificationsPage() {
  const { session } = useAuth();
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [marking, setMarking] = useState<string | null>(null);
  const [filter, setFilter] = useState<"all" | "unread">("all");

  const load = useCallback(async () => {
    if (!session) return;
    setLoading(true);
    setError("");
    try {
      setItems(await api.notifications(session.token, session.userId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load notifications.");
    } finally {
      setLoading(false);
    }
  }, [session]);

  useEffect(() => {
    void load();
  }, [load]);

  const markRead = async (id: string) => {
    if (!session) return;
    setMarking(id);
    try {
      const updated = await api.markRead(session.token, id);
      setItems((prev) => prev.map((n) => (n.id === id ? updated : n)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not mark as read.");
    } finally {
      setMarking(null);
    }
  };

  const markAll = async () => {
    if (!session) return;
    const unread = items.filter((n) => !n.read);
    for (const n of unread) {
      try {
        const updated = await api.markRead(session.token, n.id);
        setItems((prev) => prev.map((x) => (x.id === n.id ? updated : x)));
      } catch {
        /* keep going */
      }
    }
  };

  const visible = filter === "unread" ? items.filter((n) => !n.read) : items;

  return (
    <div className="page">
      <div className="singlecol">
        <div className="card">
          <div
            className="cardpad"
            style={{ display: "flex", alignItems: "center", gap: 12 }}
          >
            <h3 style={{ margin: 0, flex: 1 }}>Notifications</h3>
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as "all" | "unread")}
              aria-label="Filter notifications"
            >
              <option value="all">All</option>
              <option value="unread">Unread</option>
            </select>
            <button className="btn small secondary" onClick={() => void markAll()}>
              Mark all as read
            </button>
          </div>
          {error && <div style={{ padding: "0 16px 12px" }}><div className="error">{error}</div></div>}
          {loading ? (
            <div className="skeleton">Loading notifications…</div>
          ) : visible.length === 0 ? (
            <div className="cardpad">
              <p className="note">You are all caught up.</p>
            </div>
          ) : (
            visible.map((n) => (
              <div className={n.read ? "notif" : "notif unread"} key={n.id}>
                <Avatar name={n.message || n.type} size={48} />
                <div style={{ flex: 1 }}>
                  <div>{n.message}</div>
                  <div className="note">
                    {n.type}
                    {n.createdAt ? ` · ${formatTime(n.createdAt)}` : ""}
                    {n.postId ? ` · Post #${n.postId}` : ""}
                  </div>
                  {!n.read && (
                    <button
                      className="btn small secondary"
                      style={{ marginTop: 8 }}
                      disabled={marking === n.id}
                      onClick={() => void markRead(n.id)}
                    >
                      {marking === n.id ? "Marking…" : "Mark as read"}
                    </button>
                  )}
                </div>
                <span className={n.read ? "pill" : "pill unread"}>
                  {n.read ? "Read" : "New"}
                </span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
