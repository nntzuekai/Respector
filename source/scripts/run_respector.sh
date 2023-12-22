#!/bin/bash


Z3_HOME=$1

array=${@:2}

# last_arg="${!#}"

java -Djava.library.path=$Z3_HOME/build/  \
  -cp ./target/Respector-0.1-SNAPSHOT.jar:$Z3_HOME/build/com.microsoft.z3.jar \
  org.rest.Respector.AppMain.Main $array