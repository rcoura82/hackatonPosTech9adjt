# Demonstração Prática — RNDS Ressarcimento SUS

## Visão Geral

O sistema implementa o fluxo completo de **ressarcimento ao SUS** pelas operadoras de planos de saúde, conforme a RN 358 da ANS. A demonstração percorre todas as funcionalidades em sequência lógica.

---

## 1. Iniciar a Aplicação

### Opção A — Docker Compose (recomendado)

```bash
# Subir PostgreSQL + aplicação em container
docker-compose up --build

# Aguardar aparecer no log:
# Started RndsRessarcimentoApplication in X seconds
```

### Opção B — Maven local (requer PostgreSQL rodando em localhost:5432)

```bash
# Criar banco se necessário
createdb -U postgres rnds_ressarcimento

# Executar
mvn spring-boot:run
```

A API fica disponível em: **http://localhost:8080**

---

## 2. Swagger UI — Exploração Interativa

Abra no navegador:

```
http://localhost:8080/swagger-ui.html
```

![Swagger Overview](https://via.placeholder.com/800x400?text=Swagger+UI+em+localhost:8080/swagger-ui.html)

Você verá **10 grupos de endpoints** documentados:

| Grupo | Endpoints |
|---|---|
| auth-controller | `POST /api/v1/auth/login` |
| operator-controller | `POST / GET /api/v1/operadoras` |
| beneficiary-controller | `POST / GET /api/v1/beneficiarios` |
| procedure-controller | `POST / GET /api/v1/procedimentos` |
| authorization-controller | `POST / GET /api/v1/autorizacoes` |
| reimbursement-controller | `POST / PATCH / GET /api/v1/ressarcimentos` |
| cost-controller | `GET /api/v1/custos/**` |
| sus-report-controller | `GET /api/v1/sus/relatorios/**` |
| configuration-controller | `POST / GET /api/v1/configuracoes/**` |
| blockchain-controller | `GET /api/v1/blockchain/**` |

### Autenticando no Swagger

1. No Swagger UI, clique em **POST /api/v1/auth/login** → **Try it out**
2. Body:
   ```json
   { "username": "sus.admin", "password": "sus123" }
   ```
3. Execute → copie o `accessToken` da resposta
4. Clique no botão **Authorize 🔒** (topo direito)
5. Cole o token no campo `bearerAuth` → **Authorize**

---

## 3. Fluxo Completo via Postman

### Importar Collections

1. Abra o Postman
2. **Import** → selecione ambos os arquivos:
   - `postman/RNDS-Ressarcimento.postman_environment.json` (variables)
   - `postman/RNDS-HappyPath.postman_collection.json` (**fluxo automático**)
3. Selecione o environment **RNDS Ressarcimento** no canto superior direito

### Executar Happy Path Automatizado

Use o **Collection Runner**:

1. Clique em `...` na collection → **Run collection**
2. Mantenha a ordem padrão (requests 01→10)
3. Clique **Run RNDS Ressarcimento — Happy Path**

Cada request salva automaticamente o ID criado para o próximo:

```
01 Login         → salva {{token}}
02 Operadora     → salva {{operatorId}}
03 Beneficiário  → salva {{beneficiaryId}}
04 Procedimento  → salva {{procedureId}}
05 Autorização   → salva {{authorizationId}}
06 Ressarcimento → salva {{reimbursementId}}
07 Pagamento     → valida status PAGO
08a-d Relatórios → valida respostas
09a-b Blockchain → valida integridade
10 RBAC test     → confirma 403 para operadora
```

---

## 4. Fluxo via cURL (passo a passo)

### Passo 1 — Autenticação JWT

```bash
# Login como admin SUS
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sus.admin","password":"sus123"}'
```

**Resposta esperada:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 14400,
  "role": "SUS"
}
```

```bash
# Exportar token para as próximas chamadas
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

---

### Passo 2 — Cadastrar Operadora

```bash
curl -X POST http://localhost:8080/api/v1/operadoras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Unimed Centro-Oeste",
    "cnpj": "12345678000199",
    "registrationNumber": "302584"
  }'
```

**Resposta (201 Created):**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Unimed Centro-Oeste",
  "cnpj": "12345678000199",
  "registrationNumber": "302584",
  "createdAt": "2026-04-15T10:00:00Z"
}
```

```bash
OP_ID="3fa85f64-5717-4562-b3fc-2c963f66afa6"
```

---

### Passo 3 — Cadastrar Beneficiário

```bash
curl -X POST http://localhost:8080/api/v1/beneficiarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Maria das Graças Oliveira",
    "document": "12345678900",
    "cns": "700000000000001",
    "operatorId": "'"$OP_ID"'"
  }'
```

**Resposta (201 Created):**
```json
{
  "id": "b1c2d3e4-...",
  "name": "Maria das Graças Oliveira",
  "document": "12345678900",
  "cns": "700000000000001",
  "operatorId": "3fa85f64-..."
}
```

```bash
BEN_ID="b1c2d3e4-..."
```

---

### Passo 4 — Cadastrar Procedimento (SIGTAP)

```bash
curl -X POST http://localhost:8080/api/v1/procedimentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "code": "0407010064",
    "description": "Apendicectomia",
    "tableReference": "SIGTAP",
    "unitValue": 1250.00
  }'
```

**Resposta (201 Created):**
```json
{
  "id": "p1q2r3s4-...",
  "code": "0407010064",
  "description": "Apendicectomia",
  "tableReference": "SIGTAP",
  "unitValue": 1250.00
}
```

```bash
PROC_ID="p1q2r3s4-..."
```

---

### Passo 5 — Emitir Autorização AIH

> AIH = Autorização de Internação Hospitalar (o formulário obrigatório do SUS)

```bash
curl -X POST http://localhost:8080/api/v1/autorizacoes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "beneficiaryId": "'"$BEN_ID"'",
    "procedureId":  "'"$PROC_ID"'",
    "operatorId":   "'"$OP_ID"'",
    "type": "AIH",
    "issueDate": "2026-04-15",
    "quantity": 1
  }'
```

**Resposta (201 Created):**
```json
{
  "id": "a1b2c3d4-...",
  "type": "AIH",
  "status": "AUTORIZADO",
  "beneficiaryId": "b1c2d3e4-...",
  "procedureId": "p1q2r3s4-...",
  "operatorId": "3fa85f64-...",
  "issueDate": "2026-04-15",
  "quantity": 1
}
```

```bash
AUTH_ID="a1b2c3d4-..."
```

> **Smart Contract:** A lógica de negócio verifica elegibilidade do beneficiário, valida o código SIGTAP e registra o evento na blockchain.

---

### Passo 6 — Gerar Ressarcimento

```bash
curl -X POST http://localhost:8080/api/v1/ressarcimentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "authorizationId": "'"$AUTH_ID"'",
    "operatorId":       "'"$OP_ID"'",
    "beneficiaryId":    "'"$BEN_ID"'",
    "amount":           1250.00,
    "competenceMonth":  "2026-04"
  }'
```

**Resposta (201 Created):**
```json
{
  "id": "r1s2t3u4-...",
  "authorizationId": "a1b2c3d4-...",
  "operatorId": "3fa85f64-...",
  "beneficiaryId": "b1c2d3e4-...",
  "amount": 1250.00,
  "competenceMonth": "2026-04",
  "status": "PENDENTE",
  "createdAt": "2026-04-15T10:05:00Z"
}
```

```bash
REIMB_ID="r1s2t3u4-..."
```

---

### Passo 7 — Registrar Pagamento

> Apenas o perfil **SUS** pode confirmar pagamentos.

```bash
curl -X PATCH http://localhost:8080/api/v1/ressarcimentos/$REIMB_ID/pagar \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta (200 OK):**
```json
{
  "id": "r1s2t3u4-...",
  "status": "PAGO",
  "paidAt": "2026-04-15T10:06:00Z",
  "amount": 1250.00
}
```

---

### Passo 8 — Relatórios SUS

```bash
# Relatório acumulado de ressarcimentos
curl http://localhost:8080/api/v1/sus/relatorios/ressarcimento-acumulado \
  -H "Authorization: Bearer $TOKEN"

# Análise por procedimento
curl http://localhost:8080/api/v1/sus/relatorios/analise-procedimentos \
  -H "Authorization: Bearer $TOKEN"

# Exportar CSV
curl "http://localhost:8080/api/v1/sus/relatorios/exportar?formato=csv" \
  -H "Authorization: Bearer $TOKEN" \
  -o relatorio.csv

# Custo total por beneficiário
curl http://localhost:8080/api/v1/custos/beneficiarios/$BEN_ID \
  -H "Authorization: Bearer $TOKEN"

# Custo total por operadora
curl http://localhost:8080/api/v1/custos/operadoras/$OP_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

### Passo 9 — Auditoria Blockchain

```bash
# Listar todos os blocos do ledger imutável
curl http://localhost:8080/api/v1/blockchain/ledger \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta:**
```json
[
  {
    "index": 0,
    "timestamp": "2026-04-15T10:00:00Z",
    "data": "GENESIS",
    "hash": "0000000000000000...",
    "previousHash": "0"
  },
  {
    "index": 1,
    "timestamp": "2026-04-15T10:01:00Z",
    "data": "{\"event\":\"OPERADORA_CADASTRADA\",\"id\":\"3fa85f64...\"}",
    "hash": "a3f7b2c1d4e5...",
    "previousHash": "0000000000000000..."
  },
  ...
]
```

```bash
# Validar integridade criptográfica da cadeia (SHA-256)
curl http://localhost:8080/api/v1/blockchain/validacao \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta:**
```json
{
  "valid": true,
  "blockCount": 6,
  "message": "Ledger íntegro — todos os hashes SHA-256 verificados com sucesso"
}
```

---

## 5. Teste de RBAC (Controle de Acesso)

```bash
# Login como operadora
OP_TOKEN=$(curl -sf -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"operadora.admin","password":"op123"}' \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# ✅ Operadora PODE consultar ressarcimentos
curl http://localhost:8080/api/v1/ressarcimentos \
  -H "Authorization: Bearer $OP_TOKEN"
# → 200 OK

# ❌ Operadora NÃO PODE registrar pagamento
curl -X PATCH http://localhost:8080/api/v1/ressarcimentos/$REIMB_ID/pagar \
  -H "Authorization: Bearer $OP_TOKEN"
# → 403 Forbidden

# ❌ Operadora NÃO PODE acessar blockchain
curl http://localhost:8080/api/v1/blockchain/ledger \
  -H "Authorization: Bearer $OP_TOKEN"
# → 403 Forbidden

# ❌ Operadora NÃO PODE ver relatórios SUS
curl http://localhost:8080/api/v1/sus/relatorios/ressarcimento-acumulado \
  -H "Authorization: Bearer $OP_TOKEN"
# → 403 Forbidden
```

---

## 6. Executar Demo Automatizada

```bash
# Tornar o script executável
chmod +x demo.sh

# Executar o happy path completo (app já deve estar rodando)
./demo.sh
```

O script exibe cada etapa com indicadores coloridos e validações automáticas.

---

## 7. Tabela de Credenciais e RBAC

| Usuário | Senha | Role | Pode fazer |
|---|---|---|---|
| `sus.admin` | `sus123` | `SUS` | Tudo — cadastros, relatórios, pagamentos, blockchain |
| `operadora.admin` | `op123` | `OPERADORA` | Consultas, emitir autorizações, criar ressarcimentos |

### Matriz completa de permissões

| Endpoint | SUS | OPERADORA |
|---|:---:|:---:|
| `POST /api/v1/operadoras` | ✅ | ❌ |
| `GET /api/v1/operadoras` | ✅ | ✅ |
| `POST /api/v1/procedimentos` | ✅ | ❌ |
| `GET /api/v1/procedimentos` | ✅ | ✅ |
| `POST /api/v1/beneficiarios` | ✅ | ✅ |
| `POST /api/v1/autorizacoes` | ✅ | ✅ |
| `POST /api/v1/ressarcimentos` | ✅ | ✅ |
| `PATCH /api/v1/ressarcimentos/{id}/pagar` | ✅ | ❌ |
| `GET /api/v1/sus/relatorios/**` | ✅ | ❌ |
| `GET /api/v1/custos/**` | ✅ | ✅ |
| `GET /api/v1/blockchain/**` | ✅ | ❌ |
| `POST /api/v1/auth/login` | ✅ | ✅ |

---

## 8. Arquitetura Resumida

```
Cliente (Postman / cURL / Swagger)
        │
        ▼
  [ Spring Security + JWT Filter ]
        │  valida Bearer token → extrai perfil
        ▼
  [ Controllers REST ]        ← @PreAuthorize(roles)
        │
        ▼
  [ Smart Contracts / Services ]   ← lógica de negócio + regras ANS
        │                ├──────────────────────────────┐
        ▼                ▼                              ▼
  [ PostgreSQL ]   [ BlockchainService ]         [ ReportService ]
    9 tabelas       ledger SHA-256 in-memory        relatórios SUS
                         │
                         ▼
               [ PermissionedBlockchainGateway ]
                (HTTP → RNDS se habilitado)
```

---

## 9. OpenAPI / api-docs

O schema OpenAPI completo em JSON está disponível em:

```
GET http://localhost:8080/api-docs
```

Pode ser importado diretamente no **Postman** via _Import → Link_ ou usado para gerar SDKs client com o OpenAPI Generator.
