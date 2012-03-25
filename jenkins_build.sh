#!/bin/sh

set -e

mvn clean compile
./conform_idl2java.sh
mvn install

cd conform
./conform_idl2java.sh
mvn clean package
