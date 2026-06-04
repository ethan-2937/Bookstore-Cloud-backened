# Bookstore Cloud Backend

An online bookstore backend with books, orders, reviews, support tickets, and PageHelper pagination. Spring AI/RAG customer service can be added in a later version.

## Tech Stack

- Java 21
- Spring Boot 4
- MyBatis + PageHelper
- MySQL 8
- Docker Compose

## Local Run

The default local database connection is `jdbc:mysql://localhost:3306/bookstore` with `root/root`.

```powershell
mvn spring-boot:run
```

Backend API runs at:

```text
http://localhost:8090
```

## Docker Run

This starts a MySQL container and the backend service. MySQL is exposed on host port `33306`, and the backend is exposed on host port `18090`, so it can run beside local development services.

```powershell
docker compose up -d --build
```

Useful commands:

```powershell
docker compose logs -f backend
docker compose down
```

Docker backend API runs at:

```text
http://localhost:18090
```

## Demo Accounts

- Admin: `admin` / `admin`
- Users can register from the frontend.

## API Highlights

- `GET /api/books?pageNum=1&pageSize=8&keyword=&category=`
- `GET /api/books/categories`
- `POST /api/user/register`
- `POST /api/user/login`
- `POST /api/orders?username=`
- `GET /api/orders/my?username=`
- `POST /api/reviews?username=`
- `POST /api/support/tickets?username=`

## Environment Variables

| Name | Default |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/bookstore?...` |
| `SPRING_DATASOURCE_USERNAME` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | `root` |
| `SPRING_SQL_INIT_MODE` | `always` |
| `SERVER_PORT` | `8090` |
