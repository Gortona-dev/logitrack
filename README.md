# LogiTrack

Sistema full stack para gestão logística de clientes, entregadores, veículos, pedidos e entregas.

## Projetos

- `logitrack-api`: API REST em Java 21 com Spring Boot, PostgreSQL, Flyway e JWT.
- `logitrack-web`: interface web em React com Vite.

## Execução local

Backend:

```bash
cd logitrack-api
DB_PASSWORD=sua_senha ./mvnw spring-boot:run
```

Frontend:

```bash
cd logitrack-web
npm install
npm run dev
```

## Variáveis principais

Backend:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`

Frontend:

- `VITE_API_URL`

## Perfis de teste

- `admin@logitrack.com` / `admin123`
- `operador@logitrack.com` / `operador123`
- `entregador@logitrack.com` / `entregador123`
- `cliente@logitrack.com` / `cliente123`
