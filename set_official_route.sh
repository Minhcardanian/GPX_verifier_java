#!/usr/bin/env bash
# set_official_route.sh
# Usage: ./set_official_route.sh /full/path/to/your.gpx

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 /path/to/file.gpx"
  exit 1
fi

SRC="$1"
DEST="src/main/resources/gpx/route_official.gpx"

if [ ! -f "$SRC" ]; then
  echo "Error: '$SRC' does not exist or is not a file."
  exit 1
fi

echo "Copying:"
echo "  $SRC"
echo "to:"
echo "  $DEST"

cp "$SRC" "$DEST"

echo "Done. New official route is now '$SRC'."
echo "Restart Spring Boot for it to take effect if the app is running."
