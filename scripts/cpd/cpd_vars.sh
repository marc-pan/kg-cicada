## Notices:
## Before running the install script, it's better to install following binaries, vim, tmux, skopeo and net-tools
## As for OLM Utils for 5.0.0, please use `skopeo list-tags docker://cp.stg.icr.io/cp/cpd/olm-utils-v3` command to
## any available docker tag.
## There is another way to check out the tag, by running `skopeo inspect docker://cp.stg.icr.io/cp/cpd/olm-utils-v3:5.0.x-DEV | egrep -i "Digest"`
## and then run `skopeo inspect docker://cp.stg.icr.io/cp/cpd/olm-utils-v3:5.0.x-DEV@sha256:<digest>` to see the tag in the output

# ENV
KUBEADMIN_PASS="xxx"
KUBEADMIN_USER="kubeadmin"
INFRA_NODE="api.xpan-cluster-2.cp.fyre.ibm.com"
OLM_UTILS_IMAGE="cp.stg.icr.io/cp/cpd/olm-utils-v3:5.0.x-DEV"
CPD_CLI_VERSION="cpd-cli-linux-EE-14.0.0-124"
USE_IP="no"
CLUSTER_IP="10.17.34.1"

NAMESPACE="dpx"
NAMESPACE_OPERATOR="cpd-operatorx"
NAMESPACE_SCHEDULER="scheduler-ns"
NAMESPACE_CERT_MANAGER="ibm-cert-manager"
NAMESPACE_LICENSE_SERVICE="ibm-licensing"
IAM_ENABLED="no"
SC_NAME="managed-nfs-provision"

# Version
VERSION="5.0.0"
BRANCH="dev"

# Steps
PRE_STEP="yes"
REFRESH_OLMUTILS="yes"
PREPARE_MIRROR="yes"
CERT_MANAGER_INSTALL="yes"
NFS_CLIENT="yes"
CPD_INSTALL="yes"

SCHEDULER_INSTALL="no"
FORCE_UNINSTALL="no"
GENERATE_CERT="no"

# Password
GITHUB_TOKEN="6e20eeddd72b231aeadb21f5b916fd1b5c431604"
WMLA_LOCAL_REGISTRY="docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-wml-accelerator-docker-local"
ARTIFACTORY_USER="panxiny@cn.ibm.com"
ARTIFACTORY_TOKEN="xxx"
CPD_STG_USER="iamapikey"
CPD_STG_TOKEN="I_Nq6ofH3ZxAaQDP4t3EmkYQFb0fwWJNCKfSCdBS5PhK"
