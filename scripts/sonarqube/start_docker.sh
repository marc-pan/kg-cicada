#!/bin/env bash

podman run -d --name sonarqube \
	-p 9000:9000 \
	-v sonarqube_data:/opt/sonarqube/data \
	-v sonarqube_extensions:/opt/sonarqube/extensions \
	-v sonarqube_logs:/opt/sonarqube/logs \
	sonarqube:lts-community

