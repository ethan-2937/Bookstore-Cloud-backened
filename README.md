# Bookstore Cloud Backend

An online bookstore backend with books, orders, reviews, support tickets, PageHelper pagination, and a DeepSeek-powered AI customer service endpoint. A vector-store RAG version can be added in a later release.

## Tech Stack

- Java 21
- Spring Boot 4
- MyBatis + PageHelper
- MySQL 8
- DeepSeek Chat API
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

To enable DeepSeek answers in Docker:

```powershell
$env:DEEPSEEK_API_KEY="sk-your-key"
docker compose up -d --build
```

If no API key is configured, `/api/ai/chat` still returns a local bookstore recommendation based on MySQL book data.

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
- `POST /api/ai/chat?username=&role=`

## Environment Variables

| Name | Default |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/bookstore?...` |
| `SPRING_DATASOURCE_USERNAME` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | `root` |
| `SPRING_SQL_INIT_MODE` | `always` |
| `SERVER_PORT` | `8090` |
| `DEEPSEEK_API_KEY` | empty |
| `DEEPSEEK_BASE_URL` | `https://api.deepseek.com` |
| `DEEPSEEK_MODEL` | `deepseek-chat` |
| `DEEPSEEK_TEMPERATURE` | `0.4` |
| `DEEPSEEK_MAX_TOKENS` | `900` |
