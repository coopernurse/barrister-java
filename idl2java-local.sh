#!/bin/sh

set -e

CLASSPATH=target/classes:$HOME/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.4/jackson-mapper-asl-1.9.4.jar:$HOME/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.4/jackson-core-asl-1.9.4.jar:$CLASSPATH

MAIN=com.bitmechanic.barrister.Idl2Java

java -cp $CLASSPATH $MAIN "$@"
