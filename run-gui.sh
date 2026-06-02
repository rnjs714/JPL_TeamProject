#!/bin/zsh
set -e

CLASSPATH="out:lib/*"

./compile.sh
java -cp "$CLASSPATH" gui.MovieBookingGui
