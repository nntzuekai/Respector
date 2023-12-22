This directory containts the source code of 15 Java REST APIs used for evaluating Respector:

digdag
enviroCar
features-service
gravitee
kafka
cassandra
senzing
ur-codebin
catwatch
cwa
ocvn
ohsome
proxyprint
quartz
restcountries


# Build Instruction

The following are instructions on how to build the services and find their class files. Commands should be executed in the root directory of each service. The JDK version is **Java 11** unless specified otherwise.

## Cassandra

To build the API:

 ```
mvn -DskipTests package
 ```
 
 The path to class files is
 
 ```
 ./management-api-server/target/classes/
 ```

## catwatch

The Java code are in subfolder `catwatch-backend`.

To build the API:

```
cd catwatch-backend
mvn compile
```

The path to class files is

```
./catwatch-backend/target/classes/
```

To facilitate Respector in getting more precise path constraints, you can optionally provide the class file of methods from Guava library that are used extensively in this API.

```
<Respector_root>/services/lib/guava-19.0/
```

## cwa-verification-server

To build the API:

```
mvn compile
```

Path to class files:

```
./target/classes/
```

## digdag

To build:

```
./gradlew build
```

Path to class files of the API:

```
./digdag-server/build/classes/java/main/
```

## enviroCar Server

To build:

```
mvn compile
```

Path to class files of the API:

```
.rest/target/classes/
```


## features-service

To build the API:

```
mvn compile
```

Path to class files:

```
./target/classes/
```

## gravitee-api-management

To build:

```
mvn compile
```

Path to class files of the API:

```
./gravitee-apim-rest-api/gravitee-apim-rest-api-management-v4/gravitee-apim-rest-api-management-v4-rest/target/classes/
```

## kafka-rest

To build:

```
mvn compile
```

Path to class files of the API:

```
./kafka-rest/target/classes/
```

## ocvn

Use **Java 8**

To build:

```
mvn compile
```

Path to class files of the API:

```
./web/target/classes/
```

## ohsome-api

To build:

```
mvn compile
```

Path to class files of the API:

```
./target/classes/
```

## proxyprint

To build:

```
mvn compile
```

Path to class files of the API:

```
./target/classes/
```

## quartz-manager

The Java code are in subfolder `quartz-manager-parent`.

To build the API:

```
cd quartz-manager-parent
mvn compile
```

NOTE: There are three target folders (listed below) that you need to provide as input to Respector.

```
./quartz-manager-parent/quartz-manager-starter-api/target/classes/
./quartz-manager-parent/quartz-manager-starter-security/target/classes/
./quartz-manager-parent/quartz-manager-web-showcase/target/classes/
```

## restcountries

To build:

```
mvn package -DskipTests
```

Path to class files of the API:

```
./target/classes/
```

## Senzing

To build:

```
mvn compile
```

Path to class files of the API:

```
./target/classes/
```

## Ur-Codebin

To build:

```
mvn compile
```

Path to class files of the API:

```
./target/classes/
```
