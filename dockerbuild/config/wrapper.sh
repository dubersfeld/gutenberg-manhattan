#!/bin/sh

echo "********************************************************"
echo "Waiting for the eureka server to start on port $EUREKASERVER_PORT"
echo "********************************************************"
while ! `nc -z $EUREKASERVER_HOST $EUREKASERVER_PORT`; do sleep 10; done
echo "******* Eureka Server has started"

echo `nc -z -v $EUREKASERVER_HOST $EUREKASERVER_PORT`
echo "********************************************************"
echo "Starting Configuration Service with Eureka Endpoint:  $EUREKASERVER_URI";
echo "********************************************************"

pwd

ls

./cnb/process/web




