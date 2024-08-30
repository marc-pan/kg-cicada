######################################################### {COPYRIGHT-TOP} ###
# IBM Confidential
# OCO Source Materials
# 5737-H76
#
# Copyright IBM Corp. 2023
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
######################################################### {COPYRIGHT-END} ###
# ARG REGISTRY=docker-na-public.artifactory.swg-devops.com/dataconn-docker-local
ARG REGISTRY=docker.io

# FROM ${REGISTRY}/ubi/ibm-jre:17-ubi9-latest
FROM ${REGISTRY}/ibm-semeru-runtimes:open-17.0.10_7-jre

ARG USER_ID=1000321001

COPY --chown=${USER_ID}:${USER_ID} ./build/libs/batch-0.0.1.jar /metering/batch.jar
COPY --chown=${USER_ID}:${USER_ID} ./build/resources/main/application.properties /metering/application.properties

USER 1000321001

RUN ["sh", "-c", "ls -dl /metering"]
ENTRYPOINT ["java", "-jar", "/metering/batch.jar"]
