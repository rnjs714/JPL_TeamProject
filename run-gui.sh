#!/bin/zsh
set -e

JACKSON_VERSION="2.17.2"
JACKSON_REPO="$HOME/.m2/repository/com/fasterxml/jackson"

CLASSPATH="\
out:\
$JACKSON_REPO/core/jackson-databind/$JACKSON_VERSION/jackson-databind-$JACKSON_VERSION.jar:\
$JACKSON_REPO/core/jackson-core/$JACKSON_VERSION/jackson-core-$JACKSON_VERSION.jar:\
$JACKSON_REPO/core/jackson-annotations/$JACKSON_VERSION/jackson-annotations-$JACKSON_VERSION.jar:\
$JACKSON_REPO/datatype/jackson-datatype-jsr310/$JACKSON_VERSION/jackson-datatype-jsr310-$JACKSON_VERSION.jar"

./compile.sh
java -cp "$CLASSPATH" gui.MovieBookingGui
