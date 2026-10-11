export const API_BASE =
  import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export interface UserDto {
  id: number;
  name: string;
  email: string;
}

export interface PostDto {
  id: number;
  content: string;
  userId: number;
  imageUrl: string | null;
  createdAt: string;
}

export interface Person {
  id: number;
  userId: number;
  name: string;
}

export interface NotificationItem {
  id: string;
  recipientId: number;
  senderId: number | null;
  type: string;
  message: string;
  postId: number | null;
  read: boolean;
  createdAt: string | null;
}

export interface CommentDto {
  id: number;
  postId: number;
  userId: number;
  content: string;
  createdAt: string;
}

export interface ConnectionRequestDto {
  id: number;
  senderUserId: number;
  senderName: string;
  receiverUserId: number;
  receiverName: string;
  status: string;
  createdAt: string | null;
}

export interface UploadResponse {
  key: string;
  url: string;
  bucket: string;
  contentType: string;
  size: number;
  uploadedAt: string;
}

export async function uploadFile(
  token: string,
  file: File,
  folder = "post-images",
): Promise<UploadResponse> {
  const form = new FormData();
  form.append("file", file);
  const res = await fetch(
    `${API_BASE}/api/v1/uploads/core/upload?folder=${encodeURIComponent(folder)}`,
    { method: "POST", headers: { Authorization: `Bearer ${token}` }, body: form },
  );
  if (!res.ok) {
    let message = `Upload failed (${res.status})`;
    try {
      const parsed = await res.json();
      message = parsed.error ?? parsed.message ?? message;
    } catch {
      /* keep default */
    }
    throw new ApiError(res.status, message);
  }
  return (await res.json()) as UploadResponse;
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export function userIdFromToken(token: string): number | null {
  try {
    const payload = token.split(".")[1];
    const json = JSON.parse(
      atob(payload.replace(/-/g, "+").replace(/_/g, "/")),
    );
    return Number(json.sub);
  } catch {
    return null;
  }
}

async function request<T>(
  path: string,
  token: string | null,
  init?: RequestInit,
): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {}),
    },
  });
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!res.ok) {
    let message = text;
    try {
      const parsed = JSON.parse(text);
      message = parsed.message ?? parsed.error ?? text;
    } catch {
      /* keep raw text */
    }
    throw new ApiError(res.status, message || `Request failed (${res.status})`);
  }
  if (!text) return undefined as T;
  try {
    return JSON.parse(text) as T;
  } catch {
    return text as unknown as T;
  }
}

export const api = {
  signup: (name: string, email: string, password: string) =>
    request<UserDto>("/api/v1/users/auth/signup", null, {
      method: "POST",
      body: JSON.stringify({ name, email, password }),
    }),

  login: async (email: string, password: string): Promise<string> => {
    const raw = await request<string>("/api/v1/users/auth/login", null, {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    const token = String(raw).trim();
    return token.startsWith('"') ? JSON.parse(token) : token;
  },

  myPosts: (token: string, userId: number) =>
    request<PostDto[]>(
      `/api/v1/posts/core/users/${userId}/allPosts`,
      token,
    ),

  userPosts: (token: string, userId: number) =>
    request<PostDto[]>(
      `/api/v1/posts/core/users/${userId}/allPosts`,
      token,
    ),

  createPost: (token: string, content: string, imageUrl?: string | null) =>
    request<PostDto>("/api/v1/posts/core", token, {
      method: "POST",
      body: JSON.stringify({ content, imageUrl: imageUrl ?? null }),
    }),

  postById: (token: string, postId: number) =>
    request<PostDto>(`/api/v1/posts/core/${postId}`, token),

  likePost: (token: string, postId: number) =>
    request<void>(`/api/v1/posts/likes/${postId}`, token, {
      method: "POST",
    }),

  unlikePost: (token: string, postId: number) =>
    request<void>(`/api/v1/posts/likes/${postId}`, token, {
      method: "DELETE",
    }),

  comments: (token: string, postId: number) =>
    request<CommentDto[]>(`/api/v1/posts/core/${postId}/comments`, token),

  addComment: (token: string, postId: number, content: string) =>
    request<CommentDto>(`/api/v1/posts/core/${postId}/comments`, token, {
      method: "POST",
      body: JSON.stringify({ content }),
    }),

  deleteComment: (token: string, commentId: number) =>
    request<void>(`/api/v1/posts/core/comments/${commentId}`, token, {
      method: "DELETE",
    }),

  connections: (token: string, userId: number) =>
    request<Person[]>(
      `/api/v1/connections/core/${userId}/first-degree`,
      token,
    ),

  discover: (token: string, q: string) =>
    request<Person[]>(
      `/api/v1/connections/core/discover?q=${encodeURIComponent(q)}`,
      token,
    ),

  sendConnectionRequest: (token: string, receiverId: number) =>
    request<ConnectionRequestDto>(
      `/api/v1/connections/core/requests/${receiverId}`,
      token,
      { method: "POST" },
    ),

  receivedRequests: (token: string) =>
    request<ConnectionRequestDto[]>(
      `/api/v1/connections/core/requests/received`,
      token,
    ),

  sentRequests: (token: string) =>
    request<ConnectionRequestDto[]>(
      `/api/v1/connections/core/requests/sent`,
      token,
    ),

  acceptRequest: (token: string, requestId: number) =>
    request<ConnectionRequestDto>(
      `/api/v1/connections/core/requests/${requestId}/accept`,
      token,
      { method: "POST" },
    ),

  rejectRequest: (token: string, requestId: number) =>
    request<ConnectionRequestDto>(
      `/api/v1/connections/core/requests/${requestId}/reject`,
      token,
      { method: "POST" },
    ),

  notifications: (token: string, userId: number) =>
    request<NotificationItem[]>(
      `/api/v1/notifications/recipient/${userId}`,
      token,
    ),

  unreadCount: (token: string, userId: number) =>
    request<{ unreadCount: number }>(
      `/api/v1/notifications/recipient/${userId}/unread-count`,
      token,
    ),

  markRead: (token: string, id: string) =>
    request<NotificationItem>(`/api/v1/notifications/${id}/read`, token, {
      method: "PATCH",
    }),
};
