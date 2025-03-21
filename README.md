# 🚀 Wallet Service – Secure Reactive Microservice

A high-performance, secure wallet backend service built using **Spring WebFlux**, designed for reactive, non-blocking operations. It features encrypted HTTPS communication, basic authentication, resilience mechanisms, and full containerization.

---

## 📦 Tech Stack

- ☕ **Java 17** (Eclipse Temurin)
- 🔥 **Spring Boot 3** + **Spring WebFlux**
- 🔐 **Spring Security** (Basic Auth)
- 🐘 **PostgreSQL 15**
- ⚡ **R2DBC** (Reactive PostgreSQL Driver)
- 🧪 **Liquibase** (DB Migrations)
- 🐳 **Docker** + **Docker Compose**
- 🛡️ **Resilience4j** (Circuit Breaker, Retry)
- 🐳 **Testcontainers**
- 📄 **Swagger UI** (OpenAPI Documentation)
- 🧪 **Junit 5**
- 🧙 **Mockito**
- 🔬 **StepVerifier (from Reactor Test)**

---

### 📂 Move the Keystore to Project Root

```bash
mv wallet-keystore.p12 .
```

---

### 🐳 Run the Application

```bash
docker compose up --build
```

This command will start the following containers:

| Container       | Description                      | Port(s)                        |
|----------------|----------------------------------|--------------------------------|
| `wallet-db`     | PostgreSQL DB                    | `5432`                         |
| `pgadmin`       | PostgreSQL Web Admin Console     | `5050`                         |
| `wallet-service`| Backend service with HTTPS & Auth| `8080 (HTTP)`, `8443 (HTTPS)` |

---

### 🔐 Accessing the Application

#### 🔸 Swagger UI

| Protocol | URL                                                               |
|----------|-------------------------------------------------------------------|
| HTTP     | http://localhost:8080/webjars/swagger-ui/index.html              |
| HTTPS    | https://localhost:8443/webjars/swagger-ui/index.html                         |

> 🧠 **Note:** For HTTPS, you may need to trust the self-signed certificate manually in your browser.

---

### 🧑‍💻 Authentication

Basic Auth is enabled and configured in `application.yml`:

```yaml
spring:
  security:
    user:
      name: admin
      password: admin123
```

| Username | Password  |
|----------|-----------|
| `admin`  | `admin123`|

Use these credentials when prompted by Swagger UI or any HTTP client.

---

### 🔁 API Endpoints

| Method | Endpoint                                         | Description                          |
|--------|--------------------------------------------------|--------------------------------------|
| POST   | `/api/v1/wallet/transaction`                     | Process a WIN or PURCHASE event      |
| GET    | `/api/v1/wallet/{playerId}/balance`              | Check a player's wallet balance      |
| GET    | `/api/v1/wallet/transactions/{eventId}`          | Get a transaction by its event ID    |

✅ All endpoints are visible and testable directly via Swagger UI.

---

### 🔒 SSL Configuration

SSL is configured in `application.yml` as follows:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:/app/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: wallet-ssl
```

---

### 🔐 Reminder

Ensure the keystore file (`wallet-keystore.p12`) is placed in the **project root** before running `docker compose up`. It will be copied into the Docker image and used to serve HTTPS.

---

### 📁 Project Structure & Key Files

| File                  | Purpose                                                        |
|-----------------------|----------------------------------------------------------------|
| `Dockerfile`          | Multi-stage build with HTTPS-ready app packaging               |
| `docker-compose.yml`  | Orchestrates wallet-db, pgAdmin, and wallet-service            |
| `application.yml`     | Full Spring Boot config: SSL, DB, Swagger, Auth, Resilience4j  |
| `wallet-keystore.p12` | Your self-signed SSL certificate (not committed to Git)        |

---

### 📂 Important Notes

- The `.p12` file **must not** be committed to Git.

Add this to your `.gitignore`:

```gitignore
wallet-keystore.p12
```

- **pgAdmin** is accessible at: http://localhost:5050
    - **Email:** `admin@admin.com`
    - **Password:** `admin`

- The wallet-service logs SQL queries and R2DBC parameters for debugging.

- Circuit breaker and retry patterns are enabled via **Resilience4j**.

---

### 💡 Dev Tips

Use this to verify running containers:

```bash
docker ps
```

Tail logs from the service:

```bash
docker logs -f wallet_service
```

If database errors occur during boot:

- Wait for `wallet-db` to become healthy (Docker Compose handles this with healthchecks).
- Restart `wallet-service` if it boots too early.

---

### 🧪 Testing HTTPS Endpoints

You can test HTTPS endpoints using `curl`:

```bash
curl -k -u admin:admin123 https://localhost:8443/api/v1/wallet/player-001/balance
```

> `-k` allows use of the self-signed certificate  
> `-u` provides the username and password for basic auth

---

### ✅ Summary

- ✔️ Reactive microservice built with Spring WebFlux and R2DBC
- 🔐 Encrypted traffic using `.p12` keystore and HTTPS
- 🐳 Fully containerized using Docker Compose
- 📘 Swagger UI for API testing and docs
- 🔁 Resilience via circuit breakers, retries, and health checks
- 👮 Secured with Basic Auth
- 🐳 Testcontainers
- 🛠️ Developer-friendly setup

Prepared by **Peter Togara**  🇿🇼 

_  Documented for future generations _
