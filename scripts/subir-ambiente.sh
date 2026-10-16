#!/usr/bin/env bash
# Sobe PostgreSQL + aplicacao com Docker Compose e espera a API ficar pronta.
# Uso: ./scripts/subir-ambiente.sh
set -euo pipefail

URL_HEALTH="http://localhost:8080/actuator/health"
TEMPO_MAXIMO=180

cd "$(dirname "$0")/.."

echo "Construindo e subindo os conteineres..."
docker compose up -d --build postgres app

echo -n "Aguardando a API ficar pronta"
for ((segundos = 0; segundos < TEMPO_MAXIMO; segundos += 5)); do
  if curl -s "$URL_HEALTH" | grep -q '"status":"UP"'; then
    echo
    echo "API pronta em http://localhost:8080"
    exit 0
  fi
  echo -n "."
  sleep 5
done

echo
echo "A API nao ficou pronta em ${TEMPO_MAXIMO}s. Ultimas linhas do log:"
docker compose logs --tail 50 app
exit 1
