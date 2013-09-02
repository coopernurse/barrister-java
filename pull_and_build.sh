#!/bin/sh

set -e

export JAVA_HOME=/usr/local/java
export PATH=$JAVA_HOME/bin:/usr/local/maven/bin:$PATH

git pull
mvn clean compile
./conform_idl2java.sh
mvn install
rsync -avz target/site/javadoc/apidocs/ james@barrister.bitmechanic.com:/home/james/barrister-site/api/java/latest/

cd conform
mvn clean package
