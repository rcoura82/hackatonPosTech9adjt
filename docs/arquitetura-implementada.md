# Arquitetura Implementada

## Visao Geral

A implementacao segue arquitetura em camadas com API REST, servicos de negocio (smart contracts), persistencia PostgreSQL, seguranca JWT com RBAC e modulo de blockchain com opcao de publicacao para rede permissionada.

## Diagrama

```mermaid
flowchart TD
    A[Clientes API<br/>SUS e Operadora] --> B[Controllers REST<br/>Spring Web + Swagger]
    B --> C[Security Filter Chain<br/>JWT Stateless]
    C --> D[Smart Contracts / Services]

    D --> E[Domain + DTOs]
    D --> F[(PostgreSQL)]
    F --> F1[operators]
    F --> F2[beneficiaries]
    F --> F3[procedures]
    F --> F4[authorizations]
    F --> F5[reimbursements]
    F --> F6[credential_configs]
    F --> F7[integration_configs]
    F --> F8[app_users]
    F --> F9[blockchain_events]

    D --> G[BlockchainService<br/>Chain in-memory + Hash SHA-256]
    G --> F9
    G --> H{Gateway permissionado ativo?}
    H -- Sim --> I[PermissionedBlockchainGatewayHttp]
    I --> J[RNDS / Rede Permissionada]
    H -- Nao --> K[Somente ledger local + persistencia]

    D --> L[ReportService]
    L --> B

    M[AuthService] --> F8
    M --> N[JwtService]
    N --> C
```

## Pacotes Principais

- br.gov.sus.rndsressarcimento.controller
- br.gov.sus.rndsressarcimento.service
- br.gov.sus.rndsressarcimento.security
- br.gov.sus.rndsressarcimento.blockchain
- br.gov.sus.rndsressarcimento.persistence.entity
- br.gov.sus.rndsressarcimento.persistence.repository
- br.gov.sus.rndsressarcimento.domain
- br.gov.sus.rndsressarcimento.dto

## Regras de Acesso RBAC

- ROLE_SUS: acesso total, incluindo auditoria blockchain e relatorios SUS.
- ROLE_OPERADORA: acesso operacional a fluxo de negocio e configuracoes permitidas.

## Artefatos Gerados

- Collection Postman: postman/RNDS-Ressarcimento.postman_collection.json
- Environment Postman: postman/RNDS-Ressarcimento.postman_environment.json
