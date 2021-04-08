#!/bin/sh

while ! `nc -z $CONFIGSERVER_HOST $CONFIGSERVER_PORT`; do 
    echo "*********************************************************************"
    echo "Waiting for $CONFIGSERVER_HOST server to start on port $CONFIGSERVER_PORT"
    echo "*********************************************************************"
    sleep 10; 
done

echo `nc -z $CONFIGSERVER_HOST $CONFIGSERVER_PORT`
echo "Config Server $CONFIGSERVER_HOST up and running at $CONFIGSERVER_PORT"


while ! `nc -z -v $GATEWAYSERVER_HOST $GATEWAYSERVER_PORT`; do 
    echo "*********************************************************************"
    echo "Waiting for $GATEWAYSERVER_HOST server to start on port $GATEWAYSERVER_PORT"
    echo "*********************************************************************"
    sleep 10; 
done

echo `nc -z -v $GATEWAYSERVER_HOST $GATEWAYSERVER_PORT`
echo "Gateway Server $GATEWAYSERVER_HOST up and running at $GATEWAYSERVER_PORT"


./cnb/process/web


