#!/bin/bash
PROFILE=$1
if [ -z "$1" ]
  then
    echo "PROFILE not set. running with 'dev' as a fallback"
    PROFILE=dev
fi

docker run -e "SPRING_PROFILES_ACTIVE=${PROFILE}"  -ti snripa/parking-lot:master -p 8080:8080
