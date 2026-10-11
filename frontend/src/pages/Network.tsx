import { useCallback, useEffect, useState } from "react";
import {
  api,
  type ConnectionRequestDto,
  type Person,
} from "../lib/api";
import { useAuth } from "../lib/auth";
import { Avatar } from "../components/chrome";
import { SearchIcon } from "../icons";

export function Network({ query }: { query: string }) {
  const { session } = useAuth();
  const [people, setPeople] = useState<Person[]>([]);
  const [received, setReceived] = useState<ConnectionRequestDto[]>([]);
  const [sent, setSent] = useState<ConnectionRequestDto[]>([]);
  const [discover, setDiscover] = useState<Person[]>([]);
  // The top-bar search and the in-page search drive the same people search.
  const [search, setSearch] = useState(query);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState<string | null>(null);

  // Whatever is typed in the top navigation search also searches people here.
  useEffect(() => {
    setSearch(query);
  }, [query]);

  const load = useCallback(async () => {
    if (!session) return;
    setLoading(true);
    setError("");
    try {
      const [conn, rec, out] = await Promise.all([
        api.connections(session.token, session.userId),
        api.receivedRequests(session.token),
        api.sentRequests(session.token),
      ]);
      setPeople(conn);
      setReceived(rec);
      setSent(out);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load network.");
    } finally {
      setLoading(false);
    }
  }, [session]);

  const runDiscover = useCallback(
    async (q: string) => {
      if (!session) return;
      setSearching(true);
      try {
        setDiscover(await api.discover(session.token, q));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Search failed.");
      } finally {
        setSearching(false);
      }
    },
    [session],
  );

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    const t = window.setTimeout(() => void runDiscover(search), 350);
    return () => window.clearTimeout(t);
  }, [search, runDiscover]);

  const refreshAfter = async () => {
    await load();
    await runDiscover(search);
  };

  const send = async (receiverId: number) => {
    if (!session) return;
    setBusyId(`send-${receiverId}`);
    setError("");
    try {
      await api.sendConnectionRequest(session.token, receiverId);
      await refreshAfter();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not send request.");
    } finally {
      setBusyId(null);
    }
  };

  const accept = async (id: number) => {
    if (!session) return;
    setBusyId(`accept-${id}`);
    setError("");
    try {
      await api.acceptRequest(session.token, id);
      await refreshAfter();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not accept request.");
    } finally {
      setBusyId(null);
    }
  };

  const reject = async (id: number) => {
    if (!session) return;
    setBusyId(`reject-${id}`);
    setError("");
    try {
      await api.rejectRequest(session.token, id);
      await refreshAfter();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not ignore request.");
    } finally {
      setBusyId(null);
    }
  };

  const sentIds = new Set(sent.map((r) => r.receiverUserId));

  return (
    <div className="page">
      <div className="networkgrid">
        <aside className="sidecol">
          <div className="card cardpad">
            <h3>Manage my network</h3>
            <div className="jobrow">
              <div className="jtitle">Connections</div>
              <div className="jmeta">{people.length} connected</div>
            </div>
            <div className="jobrow">
              <div className="jtitle">Invitations</div>
              <div className="jmeta">{received.length} pending</div>
            </div>
            <div className="jobrow">
              <div className="jtitle">Sent requests</div>
              <div className="jmeta">{sent.length} awaiting reply</div>
            </div>
          </div>
          <div className="card cardpad">
            <h3>How connecting works</h3>
            <p className="sub">
              Search for members below (or from the top bar), send a request,
              and they get a notification. Once they accept, a connection is
              created and their posts appear in your feed.
            </p>
          </div>
        </aside>

        <section>
          {error && <div className="error">{error}</div>}

          <div className="card">
            <div className="cardpad">
              <h3 style={{ marginBottom: 4 }}>Find people to connect with</h3>
              <p className="note" style={{ marginTop: 0 }}>
                Search every member of the network by name.
              </p>
              <div className="toolbar" style={{ marginBottom: 0 }}>
                <span style={{ color: "var(--muted)" }}>
                  <SearchIcon />
                </span>
                <input
                  placeholder="Search by name…"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  aria-label="Search people"
                  style={{ flex: 1 }}
                />
              </div>
            </div>
            {searching ? (
              <div className="skeleton">Searching…</div>
            ) : discover.length === 0 ? (
              <div className="cardpad">
                <p className="note">
                  {search.trim()
                    ? `Nobody matches “${search.trim()}”.`
                    : "No new people to show — everyone is already connected or invited."}
                </p>
              </div>
            ) : (
              <>
                <div className="cardpad note">
                  {discover.length} result{discover.length === 1 ? "" : "s"}
                  {search.trim() && (
                    <>
                      {" "}for “{search.trim()}”
                    </>
                  )}
                </div>
                {discover.map((p) => (
                  <div className="personrow" key={p.userId}>
                    <Avatar name={p.name} size={56} />
                    <div className="grow">
                      <div className="who">{p.name}</div>
                      <div className="sub">Member ID {p.userId}</div>
                    </div>
                    {sentIds.has(p.userId) ? (
                      <span className="pill">Requested ✓</span>
                    ) : (
                      <button
                        className="btn small secondary"
                        disabled={busyId === `send-${p.userId}`}
                        onClick={() => void send(p.userId)}
                      >
                        + Connect
                      </button>
                    )}
                  </div>
                ))}
              </>
            )}
          </div>

          <div className="card cardpad">
            <h3>
              Invitations
              {received.length > 0 && (
                <span className="pill unread" style={{ marginLeft: 8 }}>
                  {received.length} new
                </span>
              )}
            </h3>
            {loading ? (
              <p className="note">Loading…</p>
            ) : received.length === 0 ? (
              <p className="note">
                No pending invitations. When someone invites you, it appears
                here and in your notifications.
              </p>
            ) : (
              received.map((r) => (
                <div className="personrow" key={r.id}>
                  <Avatar name={r.senderName} size={56} />
                  <div className="grow">
                    <div className="who">{r.senderName}</div>
                    <div className="sub">
                      Member ID {r.senderUserId} · wants to connect
                    </div>
                  </div>
                  <button
                    className="btn small plain"
                    disabled={busyId === `reject-${r.id}`}
                    onClick={() => void reject(r.id)}
                  >
                    Ignore
                  </button>
                  <button
                    className="btn small"
                    disabled={busyId === `accept-${r.id}`}
                    onClick={() => void accept(r.id)}
                  >
                    Accept
                  </button>
                </div>
              ))
            )}
          </div>

          <div className="card cardpad">
            <div
              style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
            >
              <h3>Connections ({people.length})</h3>
              <button className="btn small plain" onClick={() => void load()}>
                Refresh
              </button>
            </div>
            {loading ? (
              <p className="note">Loading connections…</p>
            ) : people.length === 0 ? (
              <p className="note">
                No connections yet. Search above and send your first request.
              </p>
            ) : (
              people.map((p) => (
                <div className="personrow" key={p.userId}>
                  <Avatar name={p.name} size={56} />
                  <div className="grow">
                    <div className="who">{p.name}</div>
                    <div className="sub">Member ID {p.userId}</div>
                  </div>
                  <span className="pill">Connected</span>
                </div>
              ))
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
