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
9. Teste `https://logitrack-api-etd1.onrender.com/api/v1/health/db`.
10. Teste o login no frontend.

## Observacoes

- Nao coloque a connection string no Git.
- O Flyway cria as tabelas automaticamente no primeiro deploy.
- Para Render no plano free, prefira o `Shared Pooler` em `Session mode`, porque a doc do Supabase indica esse modo para backends persistentes em redes IPv4.
- Se a URL do Supabase incluir `?sslmode=require`, o backend preserva esse parametro.
- O formato correto do Supavisor e:

```text
postgresql://postgres.PROJECT_REF:SENHA@HOST_DO_POOLER:5432/postgres?sslmode=require
```

- `PROJECT_REF` e o id do seu projeto Supabase, por exemplo o trecho que aparece em `https://PROJECT_REF.supabase.co`.
- `HOST_DO_POOLER` deve ser copiado do Supabase. Nao monte manualmente o host, porque projetos em regioes diferentes podem usar hosts diferentes.
- Se o Render mostrar `FATAL: (ENOTFOUND) tenant/user ... not found`, confira principalmente:
  - se o usuario esta como `postgres.PROJECT_REF`, nao apenas `postgres`;
  - se o host do pooler foi copiado do dashboard correto;
  - se o projeto Supabase esta ativo, sem pausa no plano gratuito;
  - se a senha do banco foi colada exatamente como esta no Supabase.

## Evitando pausa por inatividade no plano gratuito

O projeto possui uma GitHub Action em `.github/workflows/keep-supabase-awake.yml`.

Ela roda a cada 2 dias e chama:

```text
https://logitrack-api-etd1.onrender.com/api/v1/health/db
```

Esse endpoint executa uma consulta leve no Postgres (`select 1`). Assim, alem de acordar o Render, ele gera atividade real no Supabase.

Para testar manualmente:

1. Abra o repositorio no GitHub.
2. Va em `Actions`.
3. Clique em `Keep Supabase Awake`.
4. Clique em `Run workflow`.

Observacao: isso e uma estrategia adequada para portfolio no plano gratuito. Nao substitui garantia de disponibilidade de um plano pago.
