#!/bin/env bash

export OLM_UTILS_IMAGE="cp.stg.icr.io/cp/cpd/olm-utils-v3:5.0.x"
export OLM_CONTAINER_NAME="olm-utils-play-v3"
export CPD_CLI_PACKAGE="cpd-cli-linux-EE-14.0.0"
export CPD_VERSION="5.0.0"
export INFRA_NODE="$(hostname --fqdn)"
export STG_USER="iamapikey"

export STG_PASSWORD="${STG_TOKEN}"
export KUBEADMIN="kubeadmin"
export KUBEPASSWORD="xxxxx"
export NFS_STORAGE_CLASS="managed-nfs-provision"
export OPERATOR_NAMESPACE="ibm-common-services"
export INSTANCE_NAMESPACE="dph"
export CPD_BUILD_NUMBER=$(curl -s http://icpfs1.svl.ibm.com/zen/cp4d-builds/${CPD_VERSION}/dev/cpd-cli/latest/VERSIONS.txt | grep 'Build Number' | sed -n '1p' | awk '{print $NF}')

if [ "$(basename ${PWD})" == "${CPD_CLI_PACKAGE}-${CPD_BUILD_NUMBER}" ]; then
  export CPD_CLI_DIR=${PWD}
else
  export CPD_CLI_DIR=${PWD}/${CPD_CLI_PACKAGE}-${CPD_BUILD_NUMBER}
fi

export CPD_CLI_EXEC=${CPD_CLI_DIR}/cpd-cli
export WORK_DIR=cpd-cli-workspace/olm-utils-workspace/work
