#!/bin/sh

set -e

CLASSPATH=target/test-classes:target/classes:$HOME/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.4/jackson-mapper-asl-1.9.4.jar:$HOME/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.4/jackson-core-asl-1.9.4.jar

MAIN=com.bitmechanic.barrister.conform.ConformServer

java -cp $CLASSPATH $MAIN "$@"
