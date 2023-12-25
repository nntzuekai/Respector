#!/bin/bash

cd management-api-for-apache-cassandra
mvn clean
cd ..

cd catwatch
cd catwatch-backend
mvn clean
cd ..
cd ..

cd cwa-verification-server
mvn clean
cd ..

cd digdag
./gradlew clean
cd ..

cd enviroCar-server
mvn clean
cd ..

cd features-service
mvn clean
cd ..

cd gravitee-api-management
mvn clean
cd ..

cd kafka-rest
mvn clean
cd ..

cd ocvn
mvn clean
cd ..

cd ohsome-api
mvn clean
cd ..

cd proxyprint-kitchen
mvn clean
cd ..

cd quartz-manager
cd quartz-manager-parent
mvn clean
cd ..
cd ..

cd restcountries
mvn clean
cd ..

cd senzing-api-server
mvn clean
cd ..

cd Ur-Codebin-API
mvn clean
cd ..