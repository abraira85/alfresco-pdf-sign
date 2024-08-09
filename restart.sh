#!/bin/bash

 ./alfresco.sh stop -p

docker system prune -f

./alfresco.sh start -v -b
