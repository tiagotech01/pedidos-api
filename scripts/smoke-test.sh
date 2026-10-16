#!/usr/bin/env bash
# Teste de fumaca: confere se a API em execucao responde como esperado.
# Uso: ./scripts/smoke-test.sh [URL_BASE]    (padrao: http://localhost:8080)
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
FALHAS=0

ok()    { echo "  [OK]    $1"; }
falha() { echo "  [FALHA] $1"; FALHAS=$((FALHAS + 1)); }

status_http() {
  curl -s -o /dev/null -w '%{http_code}' "$@" || true
}

echo "Smoke test em ${BASE_URL}"

# 1. Health check
SAUDE=$(curl -s "${BASE_URL}/actuator/health" || true)
if echo "$SAUDE" | grep -q '"status":"UP"'; then
  ok "health check esta UP"
else
  falha "health check nao esta UP"
fi

# 2. Cadastro de produto (nome unico a cada execucao)
NOME="Produto smoke $(date +%s)"
RESPOSTA=$(curl -s -w '\n%{http_code}' -X POST "${BASE_URL}/api/produtos" \
  -H 'Content-Type: application/json' \
  -d "{\"nome\":\"${NOME}\",\"preco\":10.50,\"estoque\":5}" || true)
CODIGO=$(echo "$RESPOSTA" | tail -n 1)
CORPO=$(echo "$RESPOSTA" | sed '$d')
if [ "$CODIGO" = "201" ]; then
  ok "POST /api/produtos retornou 201"
else
  falha "POST /api/produtos retornou ${CODIGO}"
fi

# 3. Busca do produto recem-criado
ID=$(echo "$CORPO" | grep -o '"id":[0-9]*' | head -n 1 | cut -d: -f2 || true)
if [ -n "$ID" ] && [ "$(status_http "${BASE_URL}/api/produtos/${ID}")" = "200" ]; then
  ok "GET /api/produtos/${ID} retornou 200"
else
  falha "nao foi possivel buscar o produto criado"
fi

# 4. Dados invalidos devem ser recusados
CODIGO=$(status_http -X POST "${BASE_URL}/api/produtos" \
  -H 'Content-Type: application/json' -d '{"nome":"","preco":-1,"estoque":1}')
if [ "$CODIGO" = "400" ]; then
  ok "dados invalidos retornaram 400"
else
  falha "dados invalidos retornaram ${CODIGO} (esperado 400)"
fi

# 5. Recurso inexistente deve retornar 404
CODIGO=$(status_http "${BASE_URL}/api/produtos/999999")
if [ "$CODIGO" = "404" ]; then
  ok "produto inexistente retornou 404"
else
  falha "produto inexistente retornou ${CODIGO} (esperado 404)"
fi

echo
if [ "$FALHAS" -eq 0 ]; then
  echo "Todos os testes passaram."
else
  echo "${FALHAS} teste(s) falharam."
  exit 1
fi
