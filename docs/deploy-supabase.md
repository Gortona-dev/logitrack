# Migracao para Supabase

Este projeto usa PostgreSQL com Flyway. Para manter o portfolio em servicos gratuitos, a arquitetura recomendada e:

- Frontend: Vercel
- Backend: Render
- Banco: Supabase PostgreSQL

## Passos

1. Crie um projeto no Supabase.
2. Abra `Project Settings` > `Database`.
3. Clique em `Connect` e copie a connection string do `Shared Pooler` em `Session mode`.
4. No Render, abra o servico `logitrack-api`.
5. Va em `Environment`.
6. Edite `DATABASE_URL` com a connection string do Supabase.
7. Salve e faca redeploy do backend.
8. Teste `https://logitrack-api-etd1.onrender.com/api/v1/health`.
9. Teste o login no frontend.

## Observacoes

- Nao coloque a connection string no Git.
- O Flyway cria as tabelas automaticamente no primeiro deploy.
- Para Render no plano free, prefira o `Shared Pooler` em `Session mode`, porque a doc do Supabase indica esse modo para backends persistentes em redes IPv4.
- Se a URL do Supabase incluir `?sslmode=require`, o backend preserva esse parametro.
