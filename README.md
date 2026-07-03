# LogiTrack

Sistema full stack para gestao logistica de clientes, entregadores, veiculos, pedidos e entregas.

## Projetos

- `logitrack-api`: API REST em Java 21 com Spring Boot, PostgreSQL, Flyway e JWT.
- `logitrack-web`: interface web em React com Vite.

## Hospedagem

- Backend: Render Web Service.
- Frontend: Vercel.
- Banco de dados recomendado: Supabase PostgreSQL.

O backend le a variavel `DATABASE_URL` em producao. Para Supabase no Render, prefira a connection string do Shared Pooler em session mode:

```text
postgres://usuario:senha@aws-regiao.pooler.supabase.com:5432/postgres?sslmode=require
```

Nunca versione a senha do banco no Git. Configure `DATABASE_URL` apenas no painel do Render.

## Execucao local

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

## Variaveis principais

Backend:

- `DATABASE_URL`
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
