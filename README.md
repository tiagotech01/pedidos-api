# Pedidos API

[![CI](https://github.com/tiagotech01/pedidos-api/actions/workflows/ci.yml/badge.svg)](https://github.com/tiagotech01/pedidos-api/actions/workflows/ci.yml)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)

API REST de gestão de pedidos: cadastro de **clientes** com endereço preenchido automaticamente pelo CEP (integração com o **ViaCEP**), **produtos** com controle de estoque e **pedidos** que baixam o estoque de forma transacional e o devolvem no cancelamento.

## Destaques

- **71 testes automatizados** e **87% de cobertura de linhas**, com meta mínima de 80% que quebra o build
- **Quality Gate do SonarQube aprovado**: 0 bugs, 0 vulnerabilidades, 0 code smells, 0% de duplicação
- **Pipeline CI/CD** com verificação de qualidade, testes, publicação de imagem Docker e smoke test de ponta a ponta
- **Sobe com um único comando** via Docker Compose, sem instalar Java ou PostgreSQL
- **Manifestos Kubernetes** com health probes, limites de recursos e atualização sem indisponibilidade

## Tecnologias

| Área | Ferramentas |
|---|---|
| Linguagem e framework | Java 21, Spring Boot 4 (Web, Data JPA, Validation, Actuator) |
| Banco de dados | PostgreSQL 17, Flyway (migrations versionadas), H2 nos testes |
| Integração | `RestClient` consumindo a API pública do ViaCEP |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, MockRestServiceServer |
| Qualidade | JaCoCo, SonarQube, Checkstyle, Spotless |
| Build e versionamento | Maven (Wrapper), Git com Conventional Commits |
| Contêineres e orquestração | Docker (multi-stage), Docker Compose, Kubernetes |
| CI/CD | GitHub Actions, GitHub Container Registry |
| Scripts | Bash |

## O que este projeto demonstra

- **Orientação a objetos e modelagem de domínio**: regras de negócio encapsuladas nas entidades ([`Pedido`](src/main/java/br/com/portfolio/pedidos/api/domain/Pedido.java), [`Produto`](src/main/java/br/com/portfolio/pedidos/api/domain/Produto.java)), sem setters, com `Endereco` como Value Object.
- **Design de APIs REST**: verbos e status HTTP adequados, validação de entrada e erros padronizados (RFC 9457).
- **Integração com serviços externos**: [`ViaCepClient`](src/main/java/br/com/portfolio/pedidos/api/integration/viacep/ViaCepClient.java) com timeout, tratamento de falhas e testes que simulam o serviço.
- **Persistência e SQL**: modelagem relacional com chaves estrangeiras, constraints e índices em [`db/migration/`](src/main/resources/db/migration); transações e prevenção do problema N+1.
- **Testes em várias camadas**: domínio, services com mocks, controllers e cliente HTTP, em [`src/test/`](src/test/java/br/com/portfolio/pedidos/api).
- **Qualidade contínua**: formatação, padrões de código e cobertura verificados automaticamente a cada build.
- **Entrega e operação**: imagem Docker enxuta e sem root, ambiente completo no [`compose.yaml`](compose.yaml), manifestos em [`k8s/`](k8s) e pipeline em [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Como executar

Pré-requisito: **Docker**.

```bash
git clone https://github.com/tiagotech01/pedidos-api.git
cd pedidos-api
docker compose up -d --build
```

A API sobe em `http://localhost:8080` (health check em `/actuator/health`). Para conferir se está tudo funcionando:

```bash
./scripts/smoke-test.sh
```

Para rodar os testes e as verificações de qualidade (requer Java 21):

```bash
./mvnw verify
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/produtos` | Cadastra um produto |
| `GET` | `/api/produtos` | Lista os produtos ativos |
| `GET` | `/api/produtos/{id}` | Busca um produto |
| `PUT` | `/api/produtos/{id}` | Atualiza nome, descrição e preço |
| `DELETE` | `/api/produtos/{id}` | Desativa o produto (exclusão lógica) |
| `POST` | `/api/clientes` | Cadastra um cliente; o endereço vem do ViaCEP |
| `GET` | `/api/clientes` e `/api/clientes/{id}` | Lista e busca clientes |
| `POST` | `/api/pedidos` | Cria um pedido e baixa o estoque |
| `GET` | `/api/pedidos/{id}` e `/api/pedidos?clienteId={id}` | Busca um pedido e lista os de um cliente |
| `POST` | `/api/pedidos/{id}/cancelamento` | Cancela o pedido e devolve o estoque |

Exemplo de pedido:

```bash
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId": 1, "itens": [{"produtoId": 1, "quantidade": 2}]}'
```

Erros seguem o padrão **Problem Details (RFC 9457)**:

```json
{
  "status": 400,
  "title": "Dados inválidos",
  "detail": "Um ou mais campos estão inválidos",
  "campos": { "preco": "deve ser maior que 0" }
}
```

## Arquitetura

```
Controller  ->  Service  ->  Domínio  ->  Repository  ->  PostgreSQL
(HTTP/DTOs)    (orquestra,   (regras de    (Spring Data
               transações)    negócio)      JPA)
                   |
                   +-->  ViaCepClient  ->  API externa ViaCEP
```

## Pipeline CI/CD

A cada push e pull request na `main`:

1. **Build, testes e qualidade**: Spotless, Checkstyle, compilação, testes e meta de cobertura
2. **Validação dos manifestos Kubernetes** com kubeconform, sem precisar de cluster
3. **Imagem Docker** construída e publicada no GitHub Container Registry (somente na `main`)
4. **Smoke test**: sobe a aplicação com Docker Compose e testa a API de ponta a ponta

## Decisões técnicas

- **Regras de negócio no domínio, não no service.** `Pedido` calcula o total, baixa e devolve estoque e controla o status; o service apenas orquestra.
- **Transação "tudo ou nada" na criação do pedido.** Se um item não tiver estoque, nenhuma baixa anterior é mantida.
- **Chamada externa fora da transação.** O cadastro de cliente consulta o ViaCEP sem segurar uma conexão do banco.
- **Preço congelado no item.** Mudar o preço de um produto não altera pedidos já feitos.
- **`BigDecimal` para valores monetários** e **`@EntityGraph`** para evitar o problema N+1.
- **Flyway controla o esquema**; o Hibernate só valida (`ddl-auto=validate`).
- **Configuração por variáveis de ambiente**: a mesma imagem roda local, no Docker Compose e no Kubernetes.

## Próximos passos

- Controle de concorrência no estoque com *optimistic locking* (`@Version`)
- Autenticação com Spring Security e JWT
- Documentação interativa com OpenAPI/Swagger
- Testes de integração com Testcontainers

---

Desenvolvido por **Tiago Souza** · [GitHub](https://github.com/tiagotech01)
