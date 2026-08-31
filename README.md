<div align="center">

# LogiTrack

Sistema full stack para gestao logistica, com API Java/Spring Boot, frontend React e banco PostgreSQL.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=0f172a)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Vercel](https://img.shields.io/badge/Frontend-Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)

[Frontend online](https://logitrack-web.vercel.app/login)

</div>

---

## Objetivo

O LogiTrack foi criado para centralizar a gestao de uma operacao logistica em uma aplicacao web. O sistema permite controlar clientes, entregadores, veiculos, pedidos, entregas, usuarios e indicadores operacionais em um unico painel.

A ideia principal e transformar uma rotina que normalmente depende de planilhas e controles manuais em um fluxo mais organizado, seguro e rastreavel.

## Funcionalidades

- Autenticacao com JWT.
- Controle de usuarios e perfis de acesso.
- Cadastro e manutencao de clientes.
- Cadastro e manutencao de entregadores.
- Cadastro e manutencao de veiculos.
- Registro de pedidos.
- Atribuicao e acompanhamento de entregas.
- Historico de status das entregas.
- Dashboard operacional com indicadores.
- Codigos amigaveis para facilitar identificacao de registros.
- Migracoes de banco com Flyway.
- API documentada com OpenAPI/Swagger.

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring Security, Spring Data JPA |
| Banco de dados | PostgreSQL, Flyway |
| Frontend | React, TypeScript, Vite |
| Autenticacao | JWT |
| Deploy | Render, Vercel, Supabase PostgreSQL |

## Estrutura

```text
logitrack/
+-- logitrack-api/       API REST em Java com Spring Boot
+-- logitrack-web/       Interface web em React + Vite
+-- docs/                Documentacao auxiliar de deploy
+-- scripts/             Scripts auxiliares
+-- render.yaml          Configuracao de deploy no Render
+-- vercel.json          Configuracao de deploy
```

## Como Rodar Localmente

### Requisitos

- Java 21
- Node.js 18+
- npm
- PostgreSQL local ou banco PostgreSQL remoto

### 1. Clonar o repositorio

```bash
git clone https://github.com/Gortona-dev/logitrack.git
cd logitrack
```

### 2. Configurar o backend

Entre na pasta da API:

```bash
cd logitrack-api
```

Crie um arquivo `.env` ou configure variaveis de ambiente equivalentes. Use `.env.example` como base.

Principais variaveis:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/logitrack
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
JWT_SECRET=sua_chave_secreta
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Execute a API:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A API ficara disponivel em:

```text
http://localhost:8080
```

### 3. Configurar o frontend

Em outro terminal:

```bash
cd logitrack-web
npm install
```

Crie um arquivo `.env` com:

```text
VITE_API_URL=http://localhost:8080
```

Execute:

```bash
npm run dev
```

O frontend normalmente ficara disponivel em:

```text
http://localhost:5173
```

## Acesso de Demonstracao

O projeto possui seed opcional para criar usuarios locais de teste durante desenvolvimento. Em producao, nao publique credenciais administrativas no README, no frontend ou em commits.

Para exibir credenciais de demonstracao no frontend local, configure:

```text
VITE_SHOW_DEMO_CREDENTIALS=true
```

Em ambientes publicos, mantenha essa variavel ausente ou com valor `false`.

## Scripts Uteis

Backend:

```bash
./mvnw test
./mvnw spring-boot:run
```

Frontend:

```bash
npm run dev
npm run build
npm run preview
```

## Deploy

- Frontend: Vercel.
- Backend: Render Web Service.
- Banco recomendado: Supabase PostgreSQL.

Em producao, nunca versionar senhas no Git. Configure `DATABASE_URL`, credenciais do banco e `JWT_SECRET` diretamente nos paineis de deploy.

## Autor

Desenvolvido por [Gabriel Ortona](https://github.com/Gortona-dev).

## Licenca

Codigo disponibilizado para fins de portfolio e avaliacao tecnica. Todos os direitos reservados ao autor. Consulte o arquivo [LICENSE](LICENSE) antes de reutilizar qualquer parte deste projeto.
