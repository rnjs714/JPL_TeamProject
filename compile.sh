#!/bin/zsh
set -e

CLASSPATH="lib/*"

mkdir -p out
javac -encoding UTF-8 -cp "$CLASSPATH" -d out $(find src -name "*.java")

echo "Compiled successfully to out/"
