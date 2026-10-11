import { useEffect, useState } from "react";

export type Route =
  | { name: "home" }
  | { name: "login" }
  | { name: "signup" }
  | { name: "feed" }
  | { name: "network" }
  | { name: "notifications" }
  | { name: "profile" }
  | { name: "post"; postId: number }
  | { name: "terms" }
  | { name: "privacy" };

export function parseHash(hash: string): Route {
  const clean = hash.replace(/^#\/?/, "");
  const postMatch = /^post\/(\d+)$/.exec(clean);
  if (postMatch) return { name: "post", postId: Number(postMatch[1]) };
  switch (clean) {
    case "":
      return { name: "home" };
    case "login":
      return { name: "login" };
    case "signup":
      return { name: "signup" };
    case "feed":
      return { name: "feed" };
    case "network":
      return { name: "network" };
    case "notifications":
      return { name: "notifications" };
    case "profile":
      return { name: "profile" };
    case "terms":
      return { name: "terms" };
    case "privacy":
      return { name: "privacy" };
    default:
      return { name: "home" };
  }
}

export function href(route: Route["name"] | "post", postId?: number): string {
  if (route === "post" && postId !== undefined) return `#/post/${postId}`;
  if (route === "home") return "#/";
  return `#/${route}`;
}

export function navigate(route: Route["name"] | "post", postId?: number): void {
  window.location.hash = href(route, postId);
}

export function useRoute(): Route {
  const [route, setRoute] = useState<Route>(() =>
    parseHash(window.location.hash),
  );
  useEffect(() => {
    const onChange = () => setRoute(parseHash(window.location.hash));
    window.addEventListener("hashchange", onChange);
    return () => window.removeEventListener("hashchange", onChange);
  }, []);
  return route;
}
