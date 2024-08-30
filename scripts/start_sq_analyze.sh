#!/bin/env bash

##################################################################
# This is script is used to start SQ report for a maven projects.
# Because SQ community version has functional limition, it does
# support master version only, so comment out the parameters for
# muliptle version support.
# The SQ server is running on 9.30.118.153 as a docker container.
# 2023-03-06 at Ring office, Beijing by Marc
##################################################################

DEFAULT_BRANCH="dev"
branch_name="${1:-$DEFAULT_BRANCH}"
AUTH_TOKEN="2a0e2c45e0216eb09513feaa149871f80ccb3d53"

if [ "${branch_name}" = "${DEFAULT_BRANCH}" ]
then
#  mvn -f DatalakeServiceAggregator/pom.xml clean install -Dtest="AssetTypeControllerTest" -DfailIfNoTests=false -Dmaven.test.failure.ignore=true
  mvn -f DatalakeServiceAggregator/pom.xml sonar:sonar \
    -Dsonar.projectKey=wkc_cams \
    -Dsonar.host.url=http://9.30.118.153:9000 \
    -Dsonar.login="${AUTH_TOKEN}" \
    -Dsonar.newCode.referenceBranch="${DEFAULT_BRANCH}" \
    -Dsonar.branch.name="${branch_name}"
else
  mvn -f DatalakeServiceAggregator/pom.xml clean install -DskipTests # -Dtest="AssetTypePropertyTest,AssetTypeControllerTest" -DfailIfNoTests=false -Dmaven.test.failure.ignore=true
  mvn -f DatalakeServiceAggregator/pom.xml sonar:sonar \
    -Dsonar.projectKey=wkc_cams \
    -Dsonar.host.url=http://9.30.118.153:9000 \
    -Dsonar.login="${AUTH_TOKEN}"
#    -Dsonar.newCode.referenceBranch="${DEFAULT_BRANCH}" \
#    -Dsonar.branch.name="${branch_name}" \
#    -Dsonar.pullrequest.key=0 \
#    -Dsonar.pullrequest.branch="${branch_name}" \
#    -Dsonar.pullrequest.base="${DEFAULT_BRANCH}"
fi
