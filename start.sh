#!/usr/bin/env bash
#
# start.sh — bring the docgen stack up locally: PostgreSQL + Redis (Docker),
# the Spring Boot backend (:8080), and the React/Vite frontend (:5173).
#
# Usage:
#   ./start.sh            # start everything
#   ./start.sh stop       # stop backend/frontend and the docker containers
#   ./start.sh logs       # tail backend + frontend logs
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT/.run"
PID_DIR="$ROOT/.run"
mkdir -p "$LOG_DIR"

BACKEND_PORT="${SERVER_PORT:-8080}"
FRONTEND_PORT=5173

cyan()  { printf '\033[36m%s\033[0m\n' "$*"; }
green() { printf '\033[32m%s\033[0m\n' "$*"; }
yellow(){ printf '\033[33m%s\033[0m\n' "$*"; }
red()   { printf '\033[31m%s\033[0m\n' "$*" >&2; }

# --- Pin JDK 21 (project requires Java 21+; default `java` here may be 17) -----
setup_java() {
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if J="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
      export JAVA_HOME="$J"
      export PATH="$JAVA_HOME/bin:$PATH"
    fi
  fi
  local ver
  ver="$(java -version 2>&1 | head -1)"
  case "$ver" in
    *\"21*|*\"22*|*\"23*|*\"24*|*\"25*) green "Using $ver (JAVA_HOME=${JAVA_HOME:-default})" ;;
    *) red "Java 21+ required but found: $ver"
       red "Install a JDK 21+ (e.g. 'brew install --cask temurin@21') and re-run."
       exit 1 ;;
  esac
}

# --- Stop ---------------------------------------------------------------------
stop_stack() {
  for name in backend frontend; do
    if [ -f "$PID_DIR/$name.pid" ]; then
      pid="$(cat "$PID_DIR/$name.pid")"
      if kill -0 "$pid" 2>/dev/null; then
        yellow "Stopping $name (pid $pid)…"
        # kill the process group so child node/java procs die too
        kill "$pid" 2>/dev/null || true
        pkill -P "$pid" 2>/dev/null || true
      fi
      rm -f "$PID_DIR/$name.pid"
    fi
  done
  yellow "Stopping docker containers…"
  (cd "$ROOT" && docker compose down) || true
  green "Stopped."
}

# --- Wait for a TCP port to accept connections --------------------------------
wait_for_port() {
  local host="$1" port="$2" name="$3" timeout="${4:-90}"
  cyan "Waiting for $name on $host:$port (up to ${timeout}s)…"
  for _ in $(seq "$timeout"); do
    if nc -z "$host" "$port" 2>/dev/null; then green "$name is up."; return 0; fi
    sleep 1
  done
  red "$name did not come up within ${timeout}s. See $LOG_DIR for logs."
  return 1
}

# --- Start --------------------------------------------------------------------
start_stack() {
  setup_java

  command -v docker >/dev/null 2>&1 || { red "docker not found"; exit 1; }
  command -v node   >/dev/null 2>&1 || { red "node not found"; exit 1; }

  cyan "==> Starting PostgreSQL + Redis (docker compose)…"
  (cd "$ROOT" && docker compose up -d)
  wait_for_port localhost 5432 "PostgreSQL" 60

  cyan "==> Starting backend (Spring Boot) on :${BACKEND_PORT}…"
  (cd "$ROOT/backend" && JAVA_HOME="$JAVA_HOME" ./mvnw -q spring-boot:run \
      > "$LOG_DIR/backend.log" 2>&1) &
  echo $! > "$PID_DIR/backend.pid"

  cyan "==> Installing frontend deps (if needed)…"
  if [ ! -d "$ROOT/frontend/node_modules" ]; then
    (cd "$ROOT/frontend" && npm install)
  fi

  cyan "==> Starting frontend (Vite) on :${FRONTEND_PORT}…"
  (cd "$ROOT/frontend" && npm run dev > "$LOG_DIR/frontend.log" 2>&1) &
  echo $! > "$PID_DIR/frontend.pid"

  # Backend takes longest (Flyway + JVM warmup) — wait on it for a clear signal.
  wait_for_port localhost "$BACKEND_PORT" "Backend" 120 || true
  wait_for_port localhost "$FRONTEND_PORT" "Frontend" 60 || true

  echo
  green "docgen is up:"
  echo "  Frontend : http://localhost:$FRONTEND_PORT"
  echo "  API      : http://localhost:$BACKEND_PORT"
  echo "  Swagger  : http://localhost:$BACKEND_PORT/swagger-ui.html"
  echo "  Health   : http://localhost:$BACKEND_PORT/actuator/health"
  echo
  echo "Logs : $LOG_DIR/{backend,frontend}.log   (./start.sh logs to tail)"
  echo "Stop : ./start.sh stop"
}

case "${1:-up}" in
  stop|down) stop_stack ;;
  logs)      tail -f "$LOG_DIR/backend.log" "$LOG_DIR/frontend.log" ;;
  up|start|"") start_stack ;;
  *) red "Usage: ./start.sh [up|stop|logs]"; exit 1 ;;
esac
