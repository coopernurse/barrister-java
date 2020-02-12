#!/bin/sh

set -e

CLASSPATH=target/classes:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.8.10/jackson-core-2.8.10.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.8.10/jackson-databind-2.8.10.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.8.10/jackson-annotations-2.8.10.jar:$CLASSPATH

MAIN=com.bitmechanic.barrister.Idl2Java

java -cp $CLASSPATH $MAIN "$@"
