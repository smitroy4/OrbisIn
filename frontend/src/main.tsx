import { StrictMode, useState, type ReactNode } from "react";
import { createRoot } from "react-dom/client";
import "./app.css";
import { AuthProvider, useAuth } from "./lib/auth";
import { navigate, useRoute } from "./lib/router";
import { Footer, TopBar } from "./components/chrome";
import { Landing } from "./pages/Landing";
import { Login, Signup } from "./pages/Auth";
import { Feed } from "./pages/Feed";
import { Network } from "./pages/Network";
import { NotificationsPage } from "./pages/NotificationsPage";
import { PostDetail } from "./pages/PostDetail";
import { Profile } from "./pages/Profile";
import { Privacy, Terms } from "./pages/Legal";

function Guard({ children }: { children: ReactNode }) {
  const { session } = useAuth();
  if (!session) {
    navigate("login");
    return null;
  }
  return <>{children}</>;
}

function Routes({ query }: { query: string }) {
  const route = useRoute();
  switch (route.name) {
    case "login":
      return <Login />;
    case "signup":
      return <Signup />;
    case "feed":
      return (
        <Guard>
          <Feed query={query} />
        </Guard>
      );
    case "network":
      return (
        <Guard>
          <Network query={query} />
        </Guard>
      );
    case "notifications":
      return (
        <Guard>
          <NotificationsPage />
        </Guard>
      );
    case "profile":
      return (
        <Guard>
          <Profile />
        </Guard>
      );
    case "post":
      return (
        <Guard>
          <PostDetail postId={route.postId} />
        </Guard>
      );
    case "terms":
      return <Terms />;
    case "privacy":
      return <Privacy />;
    default:
      return <Landing />;
  }
}

function App() {
  const [query, setQuery] = useState("");
  return (
    <>
      <TopBar onSearch={setQuery} />
      <main>
        <Routes query={query} />
      </main>
      <Footer />
    </>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>,
);
