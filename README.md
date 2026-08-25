# LogiTrack

Sistema full stack para gestao logistica de clientes, entregadores, veiculos, pedidos e entregas.

## Projetos

- `logitrack-api`: API REST em Java 21 com Spring Boot, PostgreSQL, Flyway e JWT.
- `logitrack-web`: interface web em React com Vite.

## Hospedagem

- Backend: Render Web Service.
- Frontend: Vercel.
- Banco de dados recomendado: Supabase PostgreSQL.

O backend le a variavel `DATABASE_URL` em producao. Para Supabase no Render, use a connection string do `Shared Pooler` em `Session mode`, copiada diretamente do dashboard do seu projeto:

```text
postgresql://postgres.PROJECT_REF:SENHA@HOST_DO_POOLER:5432/postgres?sslmode=require
```

Importante:

- O host do pooler deve ser copiado do Supabase. Nao assuma `aws-0`, porque a regiao pode usar outro host.
- No Supavisor, o usuario deve ter o formato `postgres.PROJECT_REF`.
- Se aparecer `tenant/user ... not found`, a `DATABASE_URL` do Render esta apontando para usuario, project ref ou host de pooler incorreto.
- Para o plano gratuito, o pool do backend fica pequeno por padrao para reduzir risco de esgotar conexoes.

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

## Keep-alive gratuito

O workflow `.github/workflows/keep-supabase-awake.yml` chama o endpoint publico `/api/v1/health/db` a cada 2 dias para reduzir pausas por inatividade no Supabase Free e acordar o backend no Render.

Endpoint:

```text
https://logitrack-api-etd1.onrender.com/api/v1/health/db
```
