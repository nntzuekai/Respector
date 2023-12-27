#!/bin/bash

if [ -z "$JAVA8_HOME" ]; then
    echo "JAVA8_HOME is not set (usually /usr/lib/jvm/java-8-openjdk-amd64/). Exiting the script."
    exit 1
else
    echo "JAVA8_HOME is set to $JAVA8_HOME"
fi

cd management-api-for-apache-cassandra
mvn -DskipTests package
cd ..

cd catwatch
cd catwatch-backend
mvn compile
cd ..
cd ..

cd cwa-verification-server
mvn compile
cd ..

cd digdag
./gradlew build -x test -x check
cd ..

cd enviroCar-server
mvn compile
cd ..

cd features-service
mvn compile
cd ..

cd gravitee-api-management
mvn compile
cd ..

cd kafka-rest
mvn compile
cd ..

cd ocvn
JAVA_HOME=$JAVA8_HOME mvn compile
cd ..

cd ohsome-api
mvn compile
cd ..

cd proxyprint-kitchen
mvn compile
cd ..

cd quartz-manager
cd quartz-manager-parent
mvn compile
cd ..
cd ..

cd restcountries
mvn package -DskipTests
cd ..

cd senzing-api-server
mvn compile
cd ..

cd Ur-Codebin-API
mvn compile
cd ..