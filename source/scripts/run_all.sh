#!/bin/bash

OUTPUT_DIR=$1
DATASET_DIR=$2


if [ -z "$Z3_HOME" ]; then
    echo "Z3_HOME is not set. Exiting the script."
    exit 1
else
    echo "Z3_HOME is set to $Z3_HOME"
fi




date

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/management-api-for-apache-cassandra/management-api-server/target/classes $OUTPUT_DIR/cassandra.json ; }  &> ./logs/cassandra.log


# 6 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/catwatch/catwatch-backend/target/classes/ $DATASET_DIR/lib/guava-19.0/  $OUTPUT_DIR/catwatch.json ; }  &> ./logs/catwatch.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/cwa-verification-server/target/classes/ $OUTPUT_DIR/cwa.json ; }  &> ./logs/cwa.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/digdag/digdag-server/build/classes/java/main/ $OUTPUT_DIR/digdag.json ; }  &> ./logs/digdag.log

# 5 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/enviroCar-server/rest/target/classes/  $OUTPUT_DIR/enviro.json ; }  &> ./logs/enviro.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/features-service/target/classes/  $OUTPUT_DIR/feature_service.json ; }  &> ./logs/feature.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/gravitee-api-management/gravitee-apim-rest-api/gravitee-apim-rest-api-management-v4/gravitee-apim-rest-api-management-v4-rest/target/classes/ $DATASET_DIR/gravitee-api-management/gravitee-apim-rest-api/gravitee-apim-rest-api-model/target/classes/ $OUTPUT_DIR/gravitee_manage_v4.json ; }  &> ./logs/gravitee.log

# 3 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/kafka-rest/kafka-rest/target/classes/  $OUTPUT_DIR/kafka.json ; }  &> ./logs/kafka.log

# 183 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/ocvn/web/target/classes/  $OUTPUT_DIR/ocvn.json ; }  &> ./logs/ocvn.log

# 144 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/ohsome-api/target/classes/  $OUTPUT_DIR/ohsome.json ; }  &> ./logs/ohsome.log

# 7 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/proxyprint-kitchen/target/classes/   $OUTPUT_DIR/proxyprint.json ; }  &> ./logs/proxyprint.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/quartz-manager/quartz-manager-parent/quartz-manager-starter-api/target/classes/ $DATASET_DIR/quartz-manager/quartz-manager-parent/quartz-manager-starter-security/target/classes/ $DATASET_DIR/quartz-manager/quartz-manager-parent/quartz-manager-web-showcase/target/classes/  $OUTPUT_DIR/quartz.json ; }  &> ./logs/quartz.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/restcountries/target/classes/  $OUTPUT_DIR/restcountries.json ; }  &> ./logs/restcountries.log

# 37 min
{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/senzing-api-server/target/classes/  $OUTPUT_DIR/senzing.json ; }  &> ./logs/senzing.log

{ time java  -Djava.library.path=$Z3_HOME/build/  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar org.rest.Respector.AppMain.Main $DATASET_DIR/Ur-Codebin-API/target/classes/  $OUTPUT_DIR/ur_codebin.json ; }  &> ./logs/ur_codebin.log

date
