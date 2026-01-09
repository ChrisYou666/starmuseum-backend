# StarMuseum (Stage 0)

## Tech Stack
- Backend: Spring Boot 3 + MyBatis-Plus + MySQL
- Frontend: Vue 3 + Vite
- DB: MySQL (local)

## 1. Database init
Run `scripts/init.sql` in your MySQL database `starmuseum`.

## 2. Start Backend
Open `backend/starmuseum-backend` in IntelliJ IDEA and run:
- `com.starmuseum.StarmuseumBackendApplication`

Endpoints:
- Ping: http://localhost:8080/api/ping
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
