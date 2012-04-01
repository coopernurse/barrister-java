#!/bin/sh

./idl2java.sh -j ./src/test/resources/conform.json -p com.bitmechanic.test -o ./src/test/java

cp -f src/test/resources/conform.json conform/src/main/resources/conform.json
./idl2java.sh -j conform/src/main/resources/conform.json -p com.bitmechanic.test -o ./conform/src/main/java
