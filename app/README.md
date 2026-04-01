# 🛡️ Admin Dashboard — Spring Boot 3

A production-ready Java Admin Dashboard built with **Spring Boot 3**, **Spring Security**, **Thymeleaf**, and **Bootstrap 5**.
Includes full User Management (CRUD), Role-Based Access Control (RBAC), Charts & Analytics, and DevOps-ready deployment files.

---

## 📁 Project Structure

```
admin-dashboard/
├── src/
│   ├── main/
│   │   ├── java/com/admin/
│   │   │   ├── AdminDashboardApplication.java   ← Entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          ← Spring Security + RBAC
│   │   │   │   └── GlobalExceptionHandler.java  ← Error handling
│   │   │   ├── controller/
│   │   │   │   ├── DashboardController.java     ← Dashboard & login
│   │   │   │   └── UserController.java          ← User CRUD
│   │   │   ├── dto/
│   │   │   │   └── UserDto.java                 ← Form data transfer object
│   │   │   ├── model/
│   │   │   │   ├── User.java                    ← User JPA entity
│   │   │   │   └── Role.java                    ← Role enum (ADMIN/MANAGER/USER)
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java          ← Spring Data JPA
│   │   │   └── service/
│   │   │       └── UserService.java             ← Business logic + auth
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── fragments/layout.html        ← Sidebar, head, alerts
│   │       │   ├── login.html                   ← Login page
│   │       │   ├── dashboard.html               ← Dashboard + charts
│   │       │   ├── error.html                   ← 403/500 error page
│   │       │   └── users/
│   │       │       ├── list.html                ← User list + search
│   │       │       └── form.html                ← Create/Edit form
│   │       └── application.properties
│   └── test/
│       └── java/com/admin/
│           ├── AdminDashboardApplicationTest.java
│           └── service/UserServiceTest.java
├── Dockerfile                                   ← Multi-stage Docker build
├── docker-compose.yml                           ← App + PostgreSQL + pgAdmin
├── k8s-deployment.yaml                          ← Kubernetes manifests
└── pom.xml
```

---

## ⚡ Quick Start

### Option 1 — Run Locally (Java required)

```bash
# Prerequisites: Java 17+, Maven 3.8+
java -version   # Must be 17+
mvn -version

# Clone and run
cd admin-dashboard
mvn spring-boot:run

# Open browser
open http://localhost:8080
```

### Option 2 — Run with Docker (recommended)

```bash
# Build and start everything
docker compose up --build

# Or run in background
docker compose up --build -d

# View logs
docker compose logs -f app

# Stop everything
docker compose down
```

### Option 3 — Run with Docker (app only, H2 in-memory DB)

```bash
# Build image
docker build -t admin-dashboard:latest .

# Run container
docker run -p 8080:8080 admin-dashboard:latest
```

---

## 🔐 Default Login Credentials

| Role    | Username   | Password     | Access Level          |
|---------|------------|------------- |-----------------------|
| Admin   | `admin`    | `admin123`   | Full access           |
| Manager | `manager1` | `manager123` | View + Edit users     |
| User    | `user1`    | `user123`    | Dashboard only        |

> **⚠️ Remove the demo credentials hint from `login.html` before production!**

---

## 🔒 Role-Based Access Control (RBAC)

| Page / Action          | ADMIN | MANAGER | USER |
|------------------------|-------|---------|------|
| Dashboard              | ✅    | ✅      | ✅   |
| View Users             | ✅    | ✅      | ❌   |
| Search Users           | ✅    | ✅      | ❌   |
| Edit User              | ✅    | ✅      | ❌   |
| Toggle Status          | ✅    | ✅      | ❌   |
| Create User            | ✅    | ❌      | ❌   |
| Delete User            | ✅    | ❌      | ❌   |

---

## 🗄️ Database Configuration

### Development (default) — H2 In-Memory
No setup required. H2 console available at:
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:admindb
Username: sa   Password: (empty)
```

### Production — PostgreSQL
Update `application.properties` or set environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/admindb
spring.datasource.username=admin
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.h2.console.enabled=false
```

### Production — MySQL
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/admindb
spring.datasource.username=admin
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

---

## 🐳 Docker & DevOps

### Build Docker Image
```bash
docker build -t admin-dashboard:1.0.0 .
docker tag admin-dashboard:1.0.0 your-registry/admin-dashboard:1.0.0
docker push your-registry/admin-dashboard:1.0.0
```

### Docker Compose with pgAdmin (DB GUI)
```bash
docker compose --profile tools up -d
# pgAdmin available at: http://localhost:5050
# Email: admin@admin.com   Password: admin123
```

### Environment Variables (Docker / K8s)

| Variable                         | Description                  | Default               |
|----------------------------------|------------------------------|-----------------------|
| `SPRING_DATASOURCE_URL`          | JDBC connection string        | H2 in-memory          |
| `SPRING_DATASOURCE_USERNAME`     | DB username                   | `sa`                  |
| `SPRING_DATASOURCE_PASSWORD`     | DB password                   | (empty)               |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`  | Schema update strategy        | `create-drop`         |
| `SERVER_PORT`                    | HTTP port                     | `8080`                |
| `JAVA_OPTS`                      | JVM flags                     | `-Xms256m -Xmx512m`  |

---

## ☸️ Kubernetes Deployment

```bash
# Update image name in k8s-deployment.yaml first!
# Then apply:
kubectl apply -f k8s-deployment.yaml

# Check status
kubectl get pods -n admin-dashboard
kubectl get svc -n admin-dashboard

# View logs
kubectl logs -f deployment/admin-dashboard -n admin-dashboard

# Scale up
kubectl scale deployment admin-dashboard --replicas=3 -n admin-dashboard
```

### Health & Readiness Probes (already configured in k8s-deployment.yaml)
```
Liveness:  GET /actuator/health  → restarts pod if DOWN
Readiness: GET /actuator/health  → removes pod from load balancer if DOWN
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=UserServiceTest

# Run with coverage report
mvn verify
# Report: target/site/jacoco/index.html
```

---

## 📊 Actuator Endpoints (DevOps Monitoring)

```
GET /actuator/health    → Application health status
GET /actuator/info      → App version and info
GET /actuator/metrics   → JVM + HTTP metrics
```

---

## 🔧 Tech Stack

| Layer       | Technology                        | Version |
|-------------|-----------------------------------|---------|
| Framework   | Spring Boot                       | 3.2.3   |
| Language    | Java                              | 17      |
| Security    | Spring Security                   | 6.x     |
| Template    | Thymeleaf                         | 3.x     |
| UI          | Bootstrap 5 + Bootstrap Icons     | 5.3.2   |
| Charts      | Chart.js                          | 4.x     |
| ORM         | Spring Data JPA + Hibernate       | 6.x     |
| DB (dev)    | H2 In-Memory                      | 2.x     |
| DB (prod)   | PostgreSQL / MySQL                | any     |
| Build       | Maven                             | 3.8+    |
| Container   | Docker + Docker Compose           | any     |
| Orchestration | Kubernetes                      | 1.25+   |

---

## 🚀 Production Checklist

- [ ] Change default credentials in `UserService.seedDefaultUsers()`
- [ ] Remove demo credentials hint from `login.html`
- [ ] Switch from H2 to PostgreSQL/MySQL
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Set `spring.thymeleaf.cache=true`
- [ ] Set `spring.h2.console.enabled=false`
- [ ] Use Kubernetes Secrets for DB credentials
- [ ] Enable HTTPS (TLS termination at Ingress/Load Balancer)
- [ ] Configure log aggregation (ELK / Loki / CloudWatch)
- [ ] Set up Prometheus + Grafana for metrics
