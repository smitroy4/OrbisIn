# OrbisIn Frontend

React + TypeScript single page app for the OrbisIn microservices project.
It talks to the backend only through the API gateway. No mock data.

## Prerequisites (backend, in order)

1. Eureka: `discoveryServer` on port 8761.
2. `userService` (9020), `postsService` (9010), `connectionsService` (9030),
   `notificationService` (9040). Each needs its own folder as the working
   directory, or the repo root, so its local `.env` file is found.
3. `apiGateway` on port 8080. Its JWT key defaults to the same value as
   `userService`; set `JWT_SECRET_KEY` env var to override both together.

## Run the frontend

```sh
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open the printed local URL (default http://localhost:5173).
To point at a non default gateway, set `VITE_API_BASE` in `.env`.

## What maps to what

| Page             | Backend calls (via gateway)                              |
| ---------------- | -------------------------------------------------------- |
| Signup / Sign in | `POST /api/v1/users/auth/signup`, `POST .../auth/login` |
| Feed             | own posts + each connection's posts, merged newest first |
| Publish          | `POST /api/v1/posts/core`                                |
| Single post view | `GET /api/v1/posts/core/{postId}`                        |
| Like / Unlike    | `POST` / `DELETE /api/v1/posts/likes/{postId}`           |
| Network          | `GET /api/v1/connections/core/{userId}/first-degree`     |
| Alerts           | `GET /api/v1/notifications/recipient/{userId}`, unread count, mark read |
| Profile          | account info from the sign in token plus own posts       |

## Known backend limitations reflected in the UI

- Likes are recorded by the backend for a fixed user, and there is no
  "who liked what" query, so like buttons toggle optimistically per post
  without counts.
- The feed is assembled from your posts plus your connections' posts,
  because the backend exposes no global timeline endpoint.
- Auth state (token, user id, name, email) lives in `localStorage`.
