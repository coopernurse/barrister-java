#!/bin/sh

export CLASSPATH=$HOME/.m2/repository/com/bitmechanic/barrister/1.0-SNAPSHOT/barrister-1.0-SNAPSHOT.jar

../idl2java.sh -i ./src/main/resources/conform.json -p com.bitmechanic.test -o ./src/main/java
