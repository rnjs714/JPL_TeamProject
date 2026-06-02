#!/bin/zsh
set -e

CLASSPATH="lib/*"

mkdir -p out
javac -cp "$CLASSPATH" -d out $(find src -name "*.java")

echo "Compiled successfully to out/"
