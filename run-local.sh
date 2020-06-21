#!/bin/bash
PROFILE=$1
if [ -z "$1" ]
  then
    echo "PROFILE not set. running with 'dev' as a fallback"
    PROFILE=dev
fi

java -Dspring.profiles.active=$PROFILE -jar target/parking-lot-0.0.1-SNAPSHOT.jar