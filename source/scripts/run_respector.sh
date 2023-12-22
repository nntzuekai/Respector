#!/bin/bash


array=${@:1}

# last_arg="${!#}"


if [ -z "$Z3_HOME" ]; then
    echo "Z3_HOME is not set. Exiting the script."
    exit 1
else
    echo "Z3_HOME is set to $Z3_HOME"
fi



java -Djava.library.path=$Z3_HOME/build/  \
  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar \
  org.rest.Respector.AppMain.Main $array
