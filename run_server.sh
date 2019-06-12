#!/bin/bash

OUT="bin/"
LIB="lib/"
LIBNAME="json-lib.jar"

java -classpath "$OUT":"$LIB$LIBNAME" -Djava.net.preferIPv4Stack=true turing.server.Server $1
