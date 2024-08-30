#!/bin/env bash
# This is a script to start up Rabbit MQ for WKC CAMS

if [ `whoami` != "root" ]; then
        echo "ERROR: Forgot to sudo. This script must be run as root."
        exit 1
fi

WEB_PORT="15672"
WEB_PORT_SSL="15671"
AMQP_PORT="5672"
AMQP_PORT_SSL="5671"
USERNAME="guest"
PASSWORD="guest"
RABBIT_TLS="true"

#Start RabbitMQ
if [ "xtrue" = "x${RABBIT_TLS}" ]; then
        podman run --cgroup-manager=cgroupfs -d -p ${WEB_PORT}:15672 -p ${WEB_PORT_SSL}:15671 -p ${AMQP_PORT}:5672 -p ${AMQP_PORT_SSL}:5671 --hostname rabbitmq --name rabbitmq -e RABBITMQ_DEFAULT_USER=${USERNAME} -e RABBITMQ_DEFAULT_PASS=${PASSWORD} -v /security:/certs -v ./rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf rabbitmq:3-management
else
        podman run --cgroup-manager=cgroupfs -d -p ${WEB_PORT}:15672 -p ${AMQP_PORT}:5672 --hostname rabbitmq --name rabbitmq -e RABBITMQ_DEFAULT_USER=${USERNAME} -e RABBITMQ_DEFAULT_PASS=${PASSWORD} rabbitmq:3-management
fi

#Check if it is started
podman ps | grep rabbitmq > /dev/null

if [ "$?" -eq 0 ]; then
        echo ""
        echo "Started RabbitMQ - http://localhost:${WEB_PORT} - Login with ${USERNAME} / ${PASSWORD}. AMQP port is ${AMQP_PORT}"
else
        echo "Failed to start RabbitMQ"

        #Clean up old pod if it exists
        container_id=`podman ps -a | grep rabbitmq | awk {'print $1'}`
        if [ -n "${container_id}" ]; then
                echo "Removing the failed rabbitmq container ${container_id}."
                podman rm ${container_id}
                podman ps -a | grep rabbitmq > /dev/null
                if [ "$?" -eq 1 ]; then
                        echo "Container removed successfully."
                fi
        fi
fi
