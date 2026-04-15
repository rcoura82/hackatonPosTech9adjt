# RNDS Ressarcimento SUS - MVP Backend Java

Backend Java para facilitar o ressarcimento ao SUS entre operadoras privadas e orgaos reguladores, com trilha de auditoria imutavel em blockchain.

## Objetivo

A solucao automatiza e valida o fluxo de autorizacoes AIH, APAC e ABI, alem do processo de ressarcimento, oferecendo:

- Seguranca com registro imutavel de transacoes.
- Transparencia para SUS e operadoras.
- Agilidade na validacao de regras de negocio e geracao de contratos/eventos.
- Visao gerencial e financeira com relatorios para o SUS.

## Arquitetura

Arquitetura em camadas com foco em simplicidade para MVP:

- controller: endpoints REST e documentacao Swagger.
- service: regras de negocio e smart contracts.
- domain/dto: modelos e contratos da API.
- blockchain: ledger distribuido em memoria, com hash SHA-256 e encadeamento de blocos.
- persistence: PostgreSQL via Spring Data JPA (dados de negocio, eventos blockchain e usuarios).
- security: autenticacao JWT stateless com RBAC por perfis SUS e OPERADORA.
- exception: tratamento padronizado de erros.

### Smart contracts implementados

- BeneficiarySmartContract: gestao da base de beneficiarios.
- ProcedureSmartContract: base de procedimentos AIH/APAC/ABI.
- OperatorSmartContract: base de operadoras.
- AuthorizationSmartContract: autorizacoes AIH/APAC/ABI com validacoes.
- ReimbursementSmartContract: ciclo de ressarcimento.
- CostSmartContract: custos por beneficiario e por operadora.
- ConfigurationSmartContract (ConfigurationService): credenciais, perfis e integracoes.

## Blockchain do MVP

Cada operacao de negocio gera um bloco contendo:

- smart contract responsavel.
- operacao executada.
- payload da transacao.
- hash do bloco anterior.
- hash atual para garantir integridade.
- persistencia do evento em PostgreSQL.
- opcionalmente, publicacao para rede permissionada via gateway HTTP.

Endpoint para auditoria:

- GET /api/v1/blockchain/ledger
- GET /api/v1/blockchain/validacao

## Endpoints principais

### Bases cadastrais

- POST /api/v1/operadoras
- GET /api/v1/operadoras
- POST /api/v1/beneficiarios
- GET /api/v1/beneficiarios
- POST /api/v1/procedimentos
- GET /api/v1/procedimentos

### Autorizacoes AIH APAC ABI

- POST /api/v1/autorizacoes
- GET /api/v1/autorizacoes?beneficiaryId=&type=

### Ressarcimento

- POST /api/v1/ressarcimentos
- PATCH /api/v1/ressarcimentos/{id}/pagar
- GET /api/v1/ressarcimentos

### Custos e bases financeiras

- GET /api/v1/custos/beneficiarios/{id}
- GET /api/v1/custos/operadoras/{id}

### Relatorios gerenciais e financeiros (SUS)

- GET /api/v1/sus/relatorios/ressarcimento-acumulado
- GET /api/v1/sus/relatorios/analise-procedimentos
- GET /api/v1/sus/relatorios/exportar?formato=csv|json

### Configuracoes (SUS e Operadoras)

- POST /api/v1/configuracoes/credenciais
- GET /api/v1/configuracoes/credenciais
- POST /api/v1/configuracoes/integracoes
- GET /api/v1/configuracoes/integracoes

### Autenticacao

- POST /api/v1/auth/login

## Swagger

Com a aplicacao em execucao:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Postman e Arquitetura

- Collection Postman: postman/RNDS-Ressarcimento.postman_collection.json
- Environment Postman: postman/RNDS-Ressarcimento.postman_environment.json
- Desenho da arquitetura implementada: docs/arquitetura-implementada.md

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 14+

## Variaveis de ambiente

```bash
export DB_URL=jdbc:postgresql://localhost:5432/rnds_ressarcimento
export DB_USER=postgres
export DB_PASSWORD=postgres

export JWT_SECRET=0123456789012345678901234567890123456789012345678901234567890123
export JWT_EXPIRATION_MINUTES=240

export BLOCKCHAIN_PERMISSIONED_ENABLED=false
export BLOCKCHAIN_PERMISSIONED_ENDPOINT=http://localhost:8081/api/v1/ledger/events
export BLOCKCHAIN_PERMISSIONED_API_KEY=dev-key
```

## Execucao local

```bash
mvn spring-boot:run
```

## Build

```bash
mvn clean package
```

## Credenciais iniciais (seed)

Na inicializacao, o sistema cria usuarios padrao:

- SUS: username `sus.admin` | password `sus123`
- Operadora: username `operadora.admin` | password `op123`

## Exemplo rapido de login e uso

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
	-H "Content-Type: application/json" \
	-d '{"username":"sus.admin","password":"sus123"}'
```

Use o token retornado no header Authorization:

```bash
curl http://localhost:8080/api/v1/sus/relatorios/ressarcimento-acumulado \
	-H "Authorization: Bearer SEU_TOKEN"
```

## Regras de acesso (RBAC)

- SUS: acesso total, incluindo relatorios SUS, auditoria blockchain e cadastro de operadoras/procedimentos.
- OPERADORA: acesso operacional a beneficiarios, autorizacoes, ressarcimentos e configuracoes permitidas.

## Observacoes de MVP

- O ledger blockchain continua em memoria para imutabilidade e auditoria em runtime.
- Cada transacao do ledger tambem e persistida em PostgreSQL (tabela `blockchain_events`).
- Ja existe adaptador para rede permissionada via HTTP, habilitavel por variavel de ambiente.
- O backend foi desenvolvido sem frontend, conforme escopo solicitado.
