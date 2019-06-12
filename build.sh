#!/bin/bash

SCRIPTPATH=$(cd $(dirname "$0") && pwd)

SRC="src/"
OUT="bin/"
LIB="lib/"
LIBNAME="json-lib.jar"

cd "$SCRIPTPATH"

mkdir -p "$LIB"
mkdir -p "$OUT"

cd "$LIB"
if [ ! -f "$LIBNAME" ]; then
	curl http://central.maven.org/maven2/org/json/json/20180813/json-20180813.jar -o "$LIBNAME"
fi

cd "$SCRIPTPATH"

javac -sourcepath "$SRC" -classpath "$LIB$LIBNAME" "$SRC"turing/server/*.java -d "$OUT"
javac -sourcepath "$SRC" -classpath "$LIB$LIBNAME" "$SRC"turing/client/*.java -d "$OUT"
