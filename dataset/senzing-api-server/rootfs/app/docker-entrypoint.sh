#!/usr/bin/env bash

# Based on version of Senzing, run the correct Senzing API Server.

export SENZING_VERSION_MAJOR=$(jq --raw-output .VERSION /opt/senzing/g2/g2BuildVersion.json | awk -F '.' '{print $1}')

if [[ ${SENZING_VERSION_MAJOR} -eq "2" ]]
then
  echo "INFO: Senzing Version 2 detected"
  cd /appV2
fi

"$@"
