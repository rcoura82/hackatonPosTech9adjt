#!/usr/bin/env bash
# =============================================================
#  RNDS Ressarcimento SUS — Demo Happy Path (curl completo)
#  Executa o fluxo completo: Login → Operadora → Beneficiário
#  → Procedimento → Autorização → Ressarcimento → Pagamento
#  → Relatórios → Blockchain
# =============================================================

set -euo pipefail

BASE="http://localhost:8080"
BOLD='\033[1m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

step() { echo -e "\n${BOLD}${CYAN}══ $1 ══${NC}"; }
ok()   { echo -e "${GREEN}✔ $1${NC}"; }
info() { echo -e "${YELLOW}  $1${NC}"; }
err()  { echo -e "${RED}✘ $1${NC}" >&2; exit 1; }

# Verifica se a app está de pé
step "0/9 — Verificando disponibilidade da API"
until curl -sf "$BASE/actuator/health" | grep -q '"status":"UP"'; do
  info "Aguardando app iniciar..."
  sleep 3
done
ok "API disponível em $BASE"

# ------------------------------------------------------------------
# 1. Login como admin SUS
# ------------------------------------------------------------------
step "1/9 — Autenticação JWT (perfil SUS)"
info "POST $BASE/api/v1/auth/login"
TOKEN_RESPONSE=$(curl -sf -X POST "$BASE/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"sus.admin","password":"sus123"}')
echo "$TOKEN_RESPONSE" | python3 -m json.tool
SUS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
[[ -z "$SUS_TOKEN" ]] && err "Token JWT não obtido!"
ok "Token JWT capturado (${#SUS_TOKEN} chars)"

AUTH="Authorization: Bearer $SUS_TOKEN"

# ------------------------------------------------------------------
# 2. Cadastrar Operadora
# ------------------------------------------------------------------
step "2/9 — Cadastrar Operadora de Saúde"
info "POST $BASE/api/v1/operadoras"
OP_RESPONSE=$(curl -sf -X POST "$BASE/api/v1/operadoras" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "name": "Unimed Centro-Oeste",
    "cnpj": "12345678000199",
    "registrationNumber": "302584"
  }')
echo "$OP_RESPONSE" | python3 -m json.tool
OP_ID=$(echo "$OP_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
ok "Operadora criada — ID: $OP_ID"

# ------------------------------------------------------------------
# 3. Cadastrar Beneficiário
# ------------------------------------------------------------------
step "3/9 — Cadastrar Beneficiário (paciente SUS)"
info "POST $BASE/api/v1/beneficiarios"
BEN_RESPONSE=$(curl -sf -X POST "$BASE/api/v1/beneficiarios" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "name": "Maria das Graças Oliveira",
    "document": "12345678900",
    "cns": "700000000000001",
    "operatorId": "'"$OP_ID"'"
  }')
echo "$BEN_RESPONSE" | python3 -m json.tool
BEN_ID=$(echo "$BEN_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
ok "Beneficiário criado — ID: $BEN_ID"

# ------------------------------------------------------------------
# 4. Cadastrar Procedimento
# ------------------------------------------------------------------
step "4/9 — Cadastrar Procedimento na tabela SUS (SIGTAP)"
info "POST $BASE/api/v1/procedimentos"
PROC_RESPONSE=$(curl -sf -X POST "$BASE/api/v1/procedimentos" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "code": "0407010064",
    "description": "Apendicectomia",
    "tableReference": "SIGTAP",
    "unitValue": 1250.00
  }')
echo "$PROC_RESPONSE" | python3 -m json.tool
PROC_ID=$(echo "$PROC_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
ok "Procedimento criado — ID: $PROC_ID"

# ------------------------------------------------------------------
# 5. Emitir Autorização AIH
# ------------------------------------------------------------------
step "5/9 — Emitir Autorização Internação Hospitalar (AIH)"
info "POST $BASE/api/v1/autorizacoes"
AUTH_RESPONSE=$(curl -sf -X POST "$BASE/api/v1/autorizacoes" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "beneficiaryId": "'"$BEN_ID"'",
    "procedureId":  "'"$PROC_ID"'",
    "operatorId":   "'"$OP_ID"'",
    "type": "AIH",
    "issueDate": "2026-04-15",
    "quantity": 1
  }')
echo "$AUTH_RESPONSE" | python3 -m json.tool
AUTHO_ID=$(echo "$AUTH_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
ok "Autorização AIH emitida — ID: $AUTHO_ID"

# ------------------------------------------------------------------
# 6. Gerar Ressarcimento
# ------------------------------------------------------------------
step "6/9 — Gerar Cobrança de Ressarcimento"
info "POST $BASE/api/v1/ressarcimentos"
REIMB_RESPONSE=$(curl -sf -X POST "$BASE/api/v1/ressarcimentos" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "authorizationId": "'"$AUTHO_ID"'",
    "operatorId":       "'"$OP_ID"'",
    "beneficiaryId":    "'"$BEN_ID"'",
    "amount":           1250.00,
    "competenceMonth":  "2026-04"
  }')
echo "$REIMB_RESPONSE" | python3 -m json.tool
REIMB_ID=$(echo "$REIMB_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
ok "Ressarcimento gerado — ID: $REIMB_ID  (status: PENDENTE)"

# ------------------------------------------------------------------
# 7. Registrar Pagamento
# ------------------------------------------------------------------
step "7/9 — Registrar Pagamento (somente perfil SUS)"
info "PATCH $BASE/api/v1/ressarcimentos/$REIMB_ID/pagar"
PAY_RESPONSE=$(curl -sf -X PATCH "$BASE/api/v1/ressarcimentos/$REIMB_ID/pagar" \
  -H "$AUTH")
echo "$PAY_RESPONSE" | python3 -m json.tool
ok "Ressarcimento PAGO  (status: PAGO)"

# ------------------------------------------------------------------
# 8. Relatórios SUS
# ------------------------------------------------------------------
step "8/9 — Relatórios Gerenciais SUS"

info "GET /api/v1/sus/relatorios/ressarcimento-acumulado"
curl -sf "$BASE/api/v1/sus/relatorios/ressarcimento-acumulado" \
  -H "$AUTH" | python3 -m json.tool
ok "Relatório acumulado OK"

info "GET /api/v1/sus/relatorios/analise-procedimentos"
curl -sf "$BASE/api/v1/sus/relatorios/analise-procedimentos" \
  -H "$AUTH" | python3 -m json.tool
ok "Análise de procedimentos OK"

info "GET /api/v1/custos/beneficiarios/$BEN_ID"
curl -sf "$BASE/api/v1/custos/beneficiarios/$BEN_ID" \
  -H "$AUTH" | python3 -m json.tool
ok "Custo por beneficiário OK"

info "GET /api/v1/custos/operadoras/$OP_ID"
curl -sf "$BASE/api/v1/custos/operadoras/$OP_ID" \
  -H "$AUTH" | python3 -m json.tool
ok "Custo por operadora OK"

# ------------------------------------------------------------------
# 9. Auditoria Blockchain
# ------------------------------------------------------------------
step "9/9 — Auditoria via Blockchain Imutável"

info "GET /api/v1/blockchain/ledger"
curl -sf "$BASE/api/v1/blockchain/ledger" \
  -H "$AUTH" | python3 -m json.tool
ok "Ledger blockchain listado"

info "GET /api/v1/blockchain/validacao"
VAL=$(curl -sf "$BASE/api/v1/blockchain/validacao" -H "$AUTH")
echo "$VAL" | python3 -m json.tool
VALID=$(echo "$VAL" | grep -o '"valid":[a-z]*' | cut -d':' -f2)
[[ "$VALID" == "true" ]] && ok "Integridade blockchain: VÁLIDA ✔" \
                          || err  "Integridade blockchain: CORROMPIDA ✘"

# ------------------------------------------------------------------
# Bônus: Exportar relatório CSV
# ------------------------------------------------------------------
step "Bônus — Exportar Relatório CSV"
info "GET /api/v1/sus/relatorios/exportar?formato=csv"
curl -sf "$BASE/api/v1/sus/relatorios/exportar?formato=csv" \
  -H "$AUTH" -o /tmp/relatorio_rnds.csv
ok "CSV salvo em /tmp/relatorio_rnds.csv"
head -5 /tmp/relatorio_rnds.csv

echo -e "\n${BOLD}${GREEN}╔══════════════════════════════════════╗"
echo -e "║  Demo concluída com sucesso!  ✔      ║"
echo -e "║  Swagger: http://localhost:8080/      ║"
echo -e "║          swagger-ui.html             ║"
echo -e "╚══════════════════════════════════════╝${NC}\n"
