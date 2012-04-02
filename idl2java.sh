#!/bin/sh

set -e

CLASSPATH=$HOME/.m2/repository/com/bitmechanic/barrister/1.0-SNAPSHOT/barrister-1.0-SNAPSHOT.jar:$HOME/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.4/jackson-mapper-asl-1.9.4.jar:$HOME/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.4/jackson-core-asl-1.9.4.jar:$CLASSPATH

MAIN=com.bitmechanic.barrister.Idl2Java

java -cp $CLASSPATH $MAIN "$@"
