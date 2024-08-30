#!/bin/env bash

set -exau pipefail

./gradlew clean build test -Dtest.maxParallelForks=4
./gradlew jacocoTestReport
./gradlew sonar   -Dsonar.projectKey=dph_broker   -Dsonar.projectName='dph_broker'   -Dsonar.host.url=http://teal-vm.fyre.ibm.com:9000  -Dsonar.login=admin -Dsonar.password=password
./gradlew sonar -Dsonar.projectKey=data-product-api -Dsonar.host.url=http://teal-vm.fyre.ibm.com:9000 -Dsonar.login=sqp_83fc9dacbfb5f2c1e89f0c98d87b85c5df579f7d
