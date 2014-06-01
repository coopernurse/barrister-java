#!/bin/sh

set -e

git pull
mvn clean compile
./conform_idl2java.sh
mvn install
#rsync -avz target/site/javadoc/apidocs/ james@barrister.bitmechanic.com:/home/james/barrister-site/api/java/latest/

cd conform
mvn clean package
