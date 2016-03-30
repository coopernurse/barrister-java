#!/bin/sh

set -e

VERSION=0.1.16

CLASSPATH=$HOME/.m2/repository/com/bitmechanic/barrister/$VERSION/barrister-$VERSION.jar:$HOME/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.4/jackson-mapper-asl-1.9.4.jar:$HOME/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.4/jackson-core-asl-1.9.4.jar:$CLASSPATH

MAIN=com.bitmechanic.barrister.Idl2Java

java -cp $CLASSPATH $MAIN "$@"
