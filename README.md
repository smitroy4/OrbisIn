<div align="center">

# Roadmap to - **OrbisIn**

### A LinkedIn-inspired professional networking backend built with Spring Boot Microservices

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2026-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)](https://kafka.apache.org/)
[![Neo4j](https://img.shields.io/badge/Neo4j-Graph_DB-4581C3?style=for-the-badge&logo=neo4j&logoColor=white)](https://neo4j.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Deployed-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)](https://kubernetes.io/)

</div>

---

## Overview of the System Design

**OrbisIn** is a production-grade, event-driven microservices backend that replicates the core domain of a professional social network. Built entirely in Java 21 with Spring Boot and Spring Cloud, the system is designed around the principle of **database-per-service**, with each service owning its data store and communicating asynchronously through Apache Kafka.

The connections graph is powered by **Neo4j**, making first-degree and multi-hop relationship traversals natural and performant. File uploads are abstracted behind a dedicated **Uploader Service** supporting Cloudinary and Google Cloud Storage. The entire system is containerized with **Jib** and ships with full **Kubernetes manifests**.

---

## Proposed Architecture

```
                        ┌─────────────────────────────────────────────────────┐
                        │                  Kubernetes Cluster                 │
                        │                                                     │
  Client                │  ┌───────────┐       ┌──────────────────────────┐  │
    │                   │  │  Ingress  │──────▶│      API Gateway         │  │
    └──────────────────▶│  │  (GCE)    │       │  Spring Cloud Gateway    │  │
                        │  └───────────┘       │  JWT Auth Filter         │  │
                        │                      └──────────┬───────────────┘  │
                        │                                 │ load-balanced     │
                        │             ┌───────────────────┼───────────────┐  │
                        │             ▼                   ▼               ▼  │
                        │   ┌──────────────┐  ┌────────────────┐  ┌──────────────────┐  │
                        │   │ User Service │  │ Posts Service  │  │Connections Svc   │  │
                        │   │  (PostgreSQL)│  │  (PostgreSQL)  │  │    (Neo4j)       │  │
                        │   └──────────────┘  └───────┬────────┘  └──────────────────┘  │
                        │                             │ Feign                │
                        │                     ┌───────┴────────┐            │
                        │                     ▼                ▼            │
                        │           ┌──────────────────┐  ┌─────────────┐  │
                        │           │  Apache Kafka     │  │  Uploader   │  │
                        │           │ post_created_topic│  │  Service    │  │
                        │           │ post_liked_topic  │  │ (Cloudinary │  │
                        │           └────────┬──────────┘  │    / GCS)   │  │
                        │                    │              └─────────────┘  │
                        │                    ▼                               │
                        │        ┌────────────────────┐                     │
                        │        │ Notification Service│                     │
                        │        │    (PostgreSQL)     │                     │
                        │        └────────────────────┘                     │
                        │                                                     │
                        │        ┌────────────────────┐                     │
                        │        │   Eureka Discovery  │  ◀── all services  │
                        │        │   (Discover Server) │      register here  │
                        │        └────────────────────┘                     │
                        └─────────────────────────────────────────────────────┘
```

All services register with the **Eureka Discovery Server**. The API Gateway resolves upstream URIs via Eureka's client-side load balancing (`lb://SERVICE-NAME`).

---

## Services Designed

| Service | Port | Database | Description |
|---|---|---|---|
| `api-gateway` | 8080 | — | Spring Cloud Gateway; JWT validation; routes all traffic |
| `discover-server` | 8761 | — | Netflix Eureka; service registry |
| `user-service` | — | PostgreSQL | Signup, login, JWT issuance; publishes `UserCreatedEvent` to Kafka |
| `posts-service` | — | PostgreSQL | Create/read posts with media; Feign calls to Connections & Uploader; publishes to Kafka |
| `connections-service` | — | Neo4j | Graph-backed social connections; send/accept/reject requests |
| `notification-service` | — | PostgreSQL | Kafka consumer; persists notifications for post and like events |
| `uploader-service` | 9050 | — | File upload abstraction over Cloudinary and Google Cloud Storage |

---

## Key Design Decisions Designed

**Neo4j for the Connections Graph**
Social connections are inherently graph data. The `CONNECTED_TO` and `REQUESTED_TO` Cypher relationships make first-degree traversal and request lifecycle management natural, without complex JOIN tables.

**JWT propagated through the Gateway**
The API Gateway validates the JWT on protected routes and forwards the `userId` in an `X-User-Id` request header via the `AuthenticationFilter`. Downstream services read the user identity from this header using a `UserContextInterceptor` that stores it in a thread‑local `ContextHolder`. All outbound Feign calls automatically copy the `X-User-Id` header via a Feign `RequestInterceptor`, so the user context is preserved across the mesh.

**Kafka for cross-service notifications**
When a post is created, the Posts Service fetches first-degree connections via Feign and publishes a `PostCreated` event to Kafka for each connection. The Notification Service consumes these events independently, ensuring loose coupling and eventual consistency.

**Feign with auth propagation**
Each service contains a `FeignClientInterceptor` that copies the `userId` header from the incoming request into outgoing Feign calls, maintaining identity context across the service mesh without a second token round-trip.

---

## API Reference Decided

All routes are accessed through the API Gateway at `http://localhost:8080`.

### Auth (`/api/v1/users`) — no JWT required
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users/auth/signup` | Register a new user |
| `POST` | `/api/v1/users/auth/login` | Login; returns JWT |

### Posts (`/api/v1/posts`) — JWT required
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/posts/core` | Create a post (multipart: `post` + `file`) |
| `GET` | `/api/v1/posts/core/{postId}` | Get post by ID |
| `GET` | `/api/v1/posts/core/users/{userId}/allPosts` | Get all posts by a user |
| `POST` | `/api/v1/posts/core/{postId}/likes` | Like a post |
| `DELETE` | `/api/v1/posts/core/{postId}/likes` | Unlike a post |

### Connections (`/api/v1/connections`) — JWT required
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/connections/core/{userId}/first-degree` | Get first-degree connections |
| `POST` | `/api/v1/connections/core/request/{userId}` | Send connection request |
| `POST` | `/api/v1/connections/core/accept/{userId}` | Accept connection request |
| `POST` | `/api/v1/connections/core/reject/{userId}` | Reject connection request |

---

## Kafka Events Planned

| Topic | Published By | Consumed By | Payload |
|---|---|---|---|
| `post_created_topic` | Posts Service | Notification Service | `postId`, `content`, `ownerUserId`, `userId` (connection's id) |
| `post_liked_topic` | Posts Service | Notification Service | `postId`, `likedByUserId`, `ownerUserId` |
| `user_created_topic` | User Service | Connections Service | `userId` (creates the `Person` node in Neo4j) |

---

## Tech Stack Strategized

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.x |
| Service Mesh | Spring Cloud 2023.0.5 (Eureka, Gateway, OpenFeign) |
| Async Messaging | Apache Kafka |
| Relational DB | PostgreSQL (User, Posts, Notification services) |
| Graph DB | Neo4j (Connections Service) |
| Auth | JWT (JJWT 0.12.6) + BCrypt |
| File Storage | Cloudinary / Google Cloud Storage |
| Containerization | Jib Maven Plugin (Docker) |
| Orchestration | Kubernetes (GKE Ingress) |
| Build Tool | Maven |

---

## Will be Running Locally

### Prerequisites

- Java 21+
- Docker & Docker Compose
- A running Kafka broker (e.g., via Docker)
- A running Neo4j instance
- PostgreSQL instances (one per service, or a shared local instance)

### 1. Start infrastructure

```bash
# Kafka + Zookeeper (minimal docker-compose)
docker run -d --name zookeeper -p 2181:2181 zookeeper
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka

# Neo4j
docker run -d --name neo4j -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=none neo4j

# PostgreSQL (shared local, or per-service)
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_PASSWORD=password postgres
```

### 2. Start services in order

```bash
# 1. Eureka Discovery Server
cd DiscoverServer && ./mvnw spring-boot:run

# 2. API Gateway
cd APIGateway && ./mvnw spring-boot:run

# 3. Core services (any order)
cd userService && ./mvnw spring-boot:run
cd postsService && ./mvnw spring-boot:run
cd ConnectionsService && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
cd uploader-service && ./mvnw spring-boot:run
```

All services auto-register with Eureka at `http://localhost:8761`.

---

## Kubernetes Deployment Planned

Full Kubernetes manifests are in the `/k8s` directory, targeting GKE with a GCE Ingress.

```bash
# Apply all manifests
kubectl apply -f k8s/

# Individual services
kubectl apply -f k8s/user-service.yml
kubectl apply -f k8s/posts-service.yml
kubectl apply -f k8s/connections-service.yml
kubectl apply -f k8s/notification-service.yml
kubectl apply -f k8s/uploader-service.yml
kubectl apply -f k8s/api-gateway.yml
kubectl apply -f k8s/kafka.yml
kubectl apply -f k8s/ingress.yml
```

Each service has a corresponding `application-k8s.properties` profile that overrides service URIs for the in-cluster environment.

---

## Project Structure Designed

```
OrbisIn/
├── APIGateway/             # Spring Cloud Gateway + JWT filter
├── DiscoverServer/         # Netflix Eureka server
├── userService/            # Auth: signup, login, JWT, Kafka producer
├── postsService/           # Posts CRUD, likes, media upload, Kafka producer
├── ConnectionsService/     # Neo4j graph: connection requests & relationships
├── notification-service/   # Kafka consumer: post and like notifications
├── uploader-service/       # File upload: Cloudinary & Google Cloud Storage
└── k8s/                    # Kubernetes deployment manifests
```

---

## Author

**Smit Roy**
MCA — Cloud Computing | Java Backend Developer

[![Portfolio](https://img.shields.io/badge/Portfolio-smitroy.com-black?style=flat-square&logo=vercel)](https://smitroy.com)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-smitroy22-0A66C2?style=flat-square&logo=linkedin)](https://linkedin.com/in/smitroy22)
[![GitHub](https://img.shields.io/badge/GitHub-smitroy4-181717?style=flat-square&logo=github)](https://github.com/smitroy4)
