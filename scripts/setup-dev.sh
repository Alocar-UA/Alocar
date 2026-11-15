#!/usr/bin/env bash
# Checks the tool versions listed in requirements.txt and installs the missing or outdated ones.
#
# Usage:
#   ./scripts/setup-dev.sh          check, then ask before installing anything
#   ./scripts/setup-dev.sh --check  only check (exit code 1 if something is missing)
#   ./scripts/setup-dev.sh --yes    install without asking
#
# Supported installers: apt (Ubuntu/Debian) and Homebrew (macOS). Node is installed with nvm.
# Docker is never installed automatically: follow the link printed by the script.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REQUIREMENTS="$REPO_ROOT/requirements.txt"
NVM_VERSION="v0.40.3"

MODE="ask"
case "${1:-}" in
  --check) MODE="check" ;;
  --yes) MODE="yes" ;;
  "") ;;
  *) echo "Unknown option: $1" >&2; exit 2 ;;
esac

required_version() {
  grep -E "^$1=" "$REQUIREMENTS" | cut -d= -f2
}

# Prints the installed version of a tool, or nothing if it is not installed.
installed_version() {
  case "$1" in
    git) git --version 2>/dev/null | grep -oE '[0-9]+(\.[0-9]+)+' | head -1 ;;
    java) java -version 2>&1 | grep -oE 'version "[0-9.]+' | grep -oE '[0-9.]+' | head -1 ;;
    node) node --version 2>/dev/null | tr -d v ;;
    docker) docker version --format '{{.Client.Version}}' 2>/dev/null ;;
    docker-compose) docker compose version --short 2>/dev/null | tr -d v ;;
  esac
}

# True if version $1 >= version $2.
version_ok() {
  [ "$(printf '%s\n%s\n' "$2" "$1" | sort -V | head -1)" = "$2" ]
}

package_manager() {
  if command -v apt-get >/dev/null 2>&1; then echo apt
  elif command -v brew >/dev/null 2>&1; then echo brew
  else echo none
  fi
}

install_package() {
  local apt_pkg="$1" brew_pkg="$2"
  case "$(package_manager)" in
    apt) sudo apt-get update && sudo apt-get install -y "$apt_pkg" ;;
    brew) brew install "$brew_pkg" ;;
    *) echo "No supported package manager found; install $apt_pkg manually."; return 1 ;;
  esac
}

install_tool() {
  case "$1" in
    git) install_package git git ;;
    java) install_package openjdk-21-jdk openjdk@21 ;;
    node)
      export NVM_DIR="${NVM_DIR:-$HOME/.nvm}"
      if [ ! -s "$NVM_DIR/nvm.sh" ]; then
        curl -fsSL "https://raw.githubusercontent.com/nvm-sh/nvm/$NVM_VERSION/install.sh" | bash
      fi
      # shellcheck disable=SC1091
      . "$NVM_DIR/nvm.sh"
      nvm install 22
      nvm alias default 22
      echo "Node 22 installed with nvm. Open a new terminal (or run 'nvm use 22') to use it."
      ;;
    docker | docker-compose)
      echo "Install Docker Engine with the Compose plugin: https://docs.docker.com/engine/install/"
      return 1
      ;;
  esac
}

missing=()
while IFS='=' read -r tool _; do
  [[ -z "$tool" || "$tool" == \#* ]] && continue
  required="$(required_version "$tool")"
  current="$(installed_version "$tool" || true)"
  if [ -n "$current" ] && version_ok "$current" "$required"; then
    printf '  ok       %-15s %s (>= %s)\n' "$tool" "$current" "$required"
  else
    printf '  MISSING  %-15s %s (>= %s)\n' "$tool" "${current:-not installed}" "$required"
    missing+=("$tool")
  fi
done < "$REQUIREMENTS"

if [ ${#missing[@]} -eq 0 ]; then
  echo "All requirements are met."
  exit 0
fi

if [ "$MODE" = check ]; then
  exit 1
fi

if [ "$MODE" = ask ]; then
  read -r -p "Install/upgrade: ${missing[*]}? [y/N] " answer
  [[ "$answer" =~ ^[Yy]$ ]] || { echo "Nothing installed."; exit 1; }
fi

status=0
for tool in "${missing[@]}"; do
  echo "==> $tool"
  install_tool "$tool" || status=1
done
exit $status
