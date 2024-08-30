#!/bin/env bash

# Download cpd-cli package from http://icpfs1.svl.ibm.com/zen/cp4d-builds
# Download cloudctl package from https://github.ibm.com/IBMPrivateCloud/cloudctl
# Download DPH CASE bundle package from https://github.ibm.com/PrivateCloud-analytics/cpd-case-repo/tree/5.0.0/local/case-repo-local/ibm-data-product
# Install DPH Operator using cloudctl command
# cloudctl case launch --case ibm-data-product-5.0.0+20240409.063217.106.tgz --tolerance 1 --namespace ibm-common-services --action installOperator --inventory dataProductOperatorSetup
# Install DPH CR using cloudctl command
# cloudctl case launch --case ibm-data-product-5.0.0+20240409.063217.106.tgz --tolerance 1 --namespace ibm-common-services --action install --inventory dataProductOperatorSetup --args "--storageclass managed-nfs-provision --scaleConfig small --license-accept --license-type Enterprise"
# CPD CLI official documentation https://www.ibm.com/docs/en/cloud-paks/cp-data/4.8.x?topic=reference-cpd-cli-command
# Internal CP4D installation doc link https://github.ibm.com/PrivateCloud-analytics/zen-dev-test-utils/blob/cpd-v5-private-install/docs/CPD/CPD-private-install.md

set -euxo pipefail

VER=${1:-5}

prechecking() {
  local ifExit=false
  [[ ! -x ${CPD_CLI_EXEC} ]] && echo "Not found cpd_cli command, please download it at first!" && ifExit=true
  if [[ -z ${STG_TOKEN} ]]; then
    read -t 15 -p "Enter a token for Github access credential: " STG_TOKEN
    [[ -z ${STG_TOKEN} ]] && echo "Not found environment variable STG_TOKEN, please set it at first!" && ifExit=true
  fi

  if [[ ${ifExit} == true ]]; then
    exit 1
  fi
}

# Setup environment variables
if [ x"$VER" == "x5" ]; then
  # for CP4D version >= v5.0
  export OLM_UTILS_IMAGE="cp.stg.icr.io/cp/cpd/olm-utils-v3:5.0.x"
  export OLM_CONTAINER_NAME="olm-utils-play-v3"
  export CPD_CLI_PACKAGE="cpd-cli-linux-EE-14.0.0"
  export CPD_VERSION="5.0.0"
else
  # for CP4D version < v5.0
  export OLM_UTILS_IMAGE="cp.stg.icr.io/cp/cpd/olm-utils-v2:4.8.4"
  export OLM_CONTAINER_NAME="olm-utils-play-v2"
  export CPD_CLI_PACKAGE="cpd-cli-linux-EE-13.1.4-109"
  export CPD_VERSION="4.8.4"
fi

export INFRA_NODE="$(hostname --fqdn)"
export STG_USER="iamapikey"

export STG_PASSWORD="${STG_TOKEN}"
export KUBEADMIN="kubeadmin"
export KUBEPASSWORD="xxxxx"
export NFS_STORAGE_CLASS="managed-nfs-storage"
export OPERATOR_NAMESPACE="ibm-common-services"
export INSTANCE_NAMESPACE="dph"


# Download CPD CLI package
export CPD_BUILD_NUMBER=$(curl -s http://icpfs1.svl.ibm.com/zen/cp4d-builds/${CPD_VERSION}/dev/cpd-cli/latest/VERSIONS.txt | grep 'Build Number' | sed -n '1p' | awk '{print $NF}')
export CPD_CLI_DIR=$(dirname $0)/${CPD_CLI_PACKAGE}-${CPD_BUILD_NUMBER}
export CPD_CLI_EXEC=${CPD_CLI_DIR}/cpd-cli
export WORK_DIR=cpd-cli-workspace/olm-utils-workspace/work
wget http://icpfs1.svl.ibm.com/zen/cp4d-builds/${CPD_VERSION}/dev/cpd-cli/latest/${CPD_CLI_PACKAGE}.tgz && tar xzf ${CPD_CLI_PACKAGE}.tgz && rm -f ${CPD_CLI_PACKAGE}.tgz
prechecking

# Essential packages installation
yum install -y podman skopeo net-tools git vim tmux

# Mandatory: Login to OCP
oc login https://${INFRA_NODE}:6443 -u ${KUBEADMIN} -p ${KUBEPASSWORD}
# oc login https://${INFRA_NODE}:6443 --token=$(oc whoami -t) --insecure-skip-tls-verify=false
podman login --username ${STG_USER} --password ${STG_PASSWORD} cp.stg.icr.io
# [[ x$(podman ps --noheading) ]] && podman stop --time 60 ${OLM_CONTAINER_NAME} && podman rm ${OLM_CONTAINER_NAME} --force
# [[ x$(podman images --noheading ${OLM_UTILS_IMAGE}) != "x" ]] && podman rmi $OLM_UTILS_IMAGE
# ${CPD_CLI_EXEC} manage restart-container
${CPD_CLI_EXEC} manage login-to-ocp --server=https://${INFRA_NODE}:6443 -u ${KUBEADMIN} -p ${KUBEPASSWORD}


# Mandatory: Setup NFS Provision
ip_addr=$(ip addr show |grep "inet 10" | grep -v podman | grep -v docker | awk '{print $2}' | cut -d '/' -f 1)
export INTERNAL_IP=${INTERNAL_IP:-${ip_addr}}
[[ ! -d /data/cloudpak ]] && mkdir -p /data/cloudpak && chmod 777 /data/cloudpak
${CPD_CLI_EXEC} manage setup-nfs-provisioner --nfs_storageclass_name=$NFS_STORAGE_CLASS --nfs_server=$INTERNAL_IP --nfs_path=/data/cloudpak
oc get pod -n nfs-provisioner
oc get sc -n nfs-provisioner


# Mandatory: only for v5.0
prepare_case_resolver() {
  export GITHUB_TOKEN="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"
  cat > $WORK_DIR/play_env.sh << EOF
export CASECTL_RESOLVERS_LOCATION=/tmp/work/resolvers.yaml
export CASECTL_RESOLVERS_AUTH_LOCATION=/tmp/work/resolvers_auth.yaml
export CASE_TOLERATION='--skip-verify'
export GITHUB_TOKEN=$GITHUB_TOKEN
export CLOUDCTL_TRACE=true
export CASE_REPO_PATH=https://\$GITHUB_TOKEN@raw.github.ibm.com/PrivateCloud-analytics/cpd-case-repo/5.0.0/dev/case-repo-dev
export CPFS_CASE_REPO_PATH=https://\$GITHUB_TOKEN@raw.github.ibm.com/IBMPrivateCloud/cloud-pak/master/repo/case
export OPENCONTENT_CASE_REPO_PATH=https://\$GITHUB_TOKEN@raw.github.ibm.com/IBMPrivateCloud/cloud-pak/master/repo/case
EOF

  cat > $WORK_DIR/resolvers.yaml << EOF
resolvers:
  metadata:
    description:  resolver file to map cases and registries. Used to get dependency cases
  resources:
    cases:
      repositories:
        DevGitHub:
          repositoryInfo:
            url: "https://raw.github.ibm.com/PrivateCloud-analytics/cpd-case-repo/5.0.0/dev/case-repo-dev"
        cloudPakCertRepo:
          repositoryInfo:
            url: "https://raw.github.ibm.com/IBMPrivateCloud/cloud-pak/master/repo/case"
      caseRepositoryMap:
      - cases:
        - case: "ibm-ccs"
          version: "*"
        - case: "ibm-datarefinery"
          version: "*"
        - case: "ibm-wsl-runtimes"
          version: "*"
        - case: "ibm-db2uoperator"
          version: "*"
        - case: "ibm-iis"
          version: "*"
        - case: "ibm-db2aaservice"
          version: "*"
        - case: "ibm-wsl"
          version: "*"
        - case: "*"
          version: "*"
        repositories:
        - DevGitHub
      - cases:
        - case: "*"
          version: "*"
        repositories:
        - cloudPakCertRepo
EOF

  export ARTIFACTORY_USER=panxiny@cn.ibm.com
  cat > $WORK_DIR/resolvers_auth.yaml << EOF
resolversAuth:
  metadata:
    description:  This is the INTERNAL authorization file for downloading CASE packages from an internal repo
  resources:
    cases:
      repositories:
        DevGitHub:
          credentials:
            basic:
              username: $ARTIFACTORY_USER
              password: $GITHUB_TOKEN
        cloudPakCertRepo:
          credentials:
            basic:
              username: $ARTIFACTORY_USER
              password: $GITHUB_TOKEN
    containerImages:
      registries:
        entitledStage:
          credentials:
            basic:
              username: iamapikey
              password: $STG_TOKEN
EOF
}
prepare_case_resolver


# Mandatory: Prepare mirror file
cat > /tmp/mirror-icsp.yaml << EOF
apiVersion: config.openshift.io/v1
kind: ImageDigestMirrorSet
metadata:
  name: cdp-icsp
spec:
  imageDigestMirrors:
  - mirrors:
      - docker-na.artifactory.swg-devops.com/hyc-cp4d-team-bootstrap-docker-local
      - docker-na.artifactory.swg-devops.com/hyc-cp4d-team-bootstrap-2-docker-local
      - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
      - na-proxy-svl.artifactory.swg-devops.com/hyc-cloudpak-team-cp-cpstgicr-docker-remote/cp
      - hyc-cp4d-team-bootstrap-2-docker-local.artifactory.swg-devops.com
      - cp.stg.icr.io/cp
    source: icr.io/cpopen
  - mirrors:
      - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    source: icr.io/cpopen/cpfs
  - mirrors:
      - cp.stg.icr.io/cp/cpd
    source: cp.icr.io/cp/cpd
  - mirrors:
      - cp.stg.icr.io/cp
    source: cp.icr.io/cp
EOF
oc apply -f /tmp/mirror-icsp.yaml

cat > /tmp/mirror-bedrock.yaml << EOF
apiVersion: operator.openshift.io/v1alpha1
kind: ImageContentSourcePolicy
metadata:
  name: dph-mirror
spec:
  repositoryDigestMirrors:
  - mirrors:
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    source: quay.io/opencloudio
  - mirrors:
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    source: hyc-cloud-private-daily-docker-local.artifactory.swg-devops.com/ibmcom
  - mirrors:
    - na-proxy-svl.artifactory.swg-devops.com/hyc-cloudpak-team-cp-cpstgicr-docker-remote/cp
    - cp.stg.icr.io/cp
    source: repo.getmanta.com/manta-ubi8-ibm
  - mirrors:
    - na-proxy-svl.artifactory.swg-devops.com/hyc-cloudpak-team-cp-cpstgicr-docker-remote/cp
    - cp.stg.icr.io/cp
    source: icr.io/db2u
  - mirrors:
    - na-proxy-svl.artifactory.swg-devops.com/hyc-cloudpak-team-cp-cpstgicr-docker-remote
    - cp.stg.icr.io
    source: cp.icr.io
EOF
oc apply -f /tmp/mirror-bedrock.yaml

${CPD_CLI_EXEC} manage add-cred-to-global-pull-secret --registry=cp.stg.icr.io --registry_pull_user=${STG_USER} --registry_pull_password=${STG_PASSWORD}
${CPD_CLI_EXEC} manage add-cred-to-global-pull-secret --registry=docker-na.artifactory.swg-devops.com --registry_pull_user=${STG_USER} --registry_pull_password=${STG_PASSWORD}
${CPD_CLI_EXEC} manage add-cred-to-global-pull-secret --registry=docker-na-public.artifactory.swg-devops.com --registry_pull_user=${STG_USER} --registry_pull_password=${STG_PASSWORD}

# Optional: Prepare override.yaml file
cat > /tmp/override.yaml << EOF
storage_class_name: managed-nfs-storage
docker_registry_prefix: cp.stg.icr.io/cp/cpd
namespace: dpx
use_dynamic_provisioning: true
ansible_python_interpreter: /usr/bin/python3
EOF

podman cp /tmp/override.yaml ${OLM_CONTAINER_NAME}:/tmp/override.yaml

# Mandatory: Prepare settings for DB2U
oc apply -f - << EOF
apiVersion: machineconfiguration.openshift.io/v1
kind: KubeletConfig
metadata:
  name: cpd-pidslimit-kubeletconfig
spec:
  kubeletConfig:
    podPidsLimit: 16384
  machineConfigPoolSelector:
    matchExpressions:
    - key: pools.operator.machineconfiguration.openshift.io/worker
      operator: Exists
EOF

${CPD_CLI_EXEC} manage apply-db2-kubelet


# Mandatory: Install IBM Cert Manager and License service
${CPD_CLI_EXEC} manage apply-cluster-components --release=${CPD_VERSION} --license_acceptance=true -vvv

# Mandatory: Install CPD foundational service
${CPD_CLI_EXEC} manage authorize-instance-topology --cpd_operator_ns=${OPERATOR_NAMESPACE} --cpd_instance_ns=${INSTANCE_NAMESPACE}
${CPD_CLI_EXEC} manage setup-instance-topology --release=${CPD_VERSION} --cpd_operator_ns=${OPERATOR_NAMESPACE} --cpd_instance_ns=${INSTANCE_NAMESPACE} --block_storage_class=${NFS_STORAGE_CLASS} --license_acceptance=true
${CPD_CLI_EXEC} manage apply-olm --release=${CPD_VERSION} --components=cpd_platform --cpd_operator_ns=${OPERATOR_NAMESPACE} # --catsrc=false --sub=false --param-file=/tmp/override.yaml -vvv
${CPD_CLI_EXEC} manage apply-cr --release=${CPD_VERSION} --components=cpd_platform --license_acceptance=true --cpd_operator_ns=${OPERATOR_NAMESPACE} --cpd_instance_ns=${INSTANCE_NAMESPACE} --file_storage_class=${NFS_STORAGE_CLASS} --block_storage_class=${NFS_STORAGE_CLASS} # -vvv

# Mandatory: Install CPD IKC service
${CPD_CLI_EXEC} manage apply-olm --release=${CPD_VERSION} --components=wkc --cpd_operator_ns=${OPERATOR_NAMESPACE} # --catsrc=false --sub=false --param-file=/tmp/override.yaml -vvv
${CPD_CLI_EXEC} manage apply-cr --release=${CPD_VERSION} --components=wkc --license_acceptance=true --cpd_operator_ns=${OPERATOR_NAMESPACE} --cpd_instance_ns=${INSTANCE_NAMESPACE} --file_storage_class=${NFS_STORAGE_CLASS} --block_storage_class=${NFS_STORAGE_CLASS} --preview=true -vvv


# Apply -olm
${CPD_CLI_EXEC} manage apply-olm --release=${CPD_VERSION} --components=dataproduct --catsrc=false --sub=false -vvv --cpd_operator_ns=${OPERATOR_NAMESPACE}

${CPD_CLI_EXEC} manage apply-olm --release=${CPD_VERSION} --components=dataproduct --preview=true --cpd_operator_ns=${OPERATOR_NAMESPACE}

${CPD_CLI_EXEC} manage apply-olm --release=${CPD_VERSION} --components=dataproduct --cpd_operator_ns=${OPERATOR_NAMESPACE}


# Verify
${CPD_CLI_EXEC} manage get-cr-status --cpd_instance_ns=${INSTANCE_NAMESPACE}
oc get sub -A
oc get catsrc -A
oc get ip -A
oc get operandregistry -A
oc get operandrequests -A
oc get po -n openshift-marketplace
oc get po -n ${OPERATOR_NAMESPACE}

# Apply-CR DPX

${CPD_CLI_EXEC} manage apply-cr --components=dataproduct --release=${CPD_VERSION} --license_acceptance=true --cpd_instance_ns=${INSTANCE_NAMESPACE} --storage_class=managed-nfs-storage --preview=true --cpd_operator_ns=${OPERATOR_NAMESPACE}

${CPD_CLI_EXEC} manage apply-cr --components=dataproduct --release=${CPD_VERSION} --license_acceptance=true --cpd_instance_ns=${INSTANCE_NAMESPACE} --storage_class=managed-nfs-storage --cpd_operator_ns=${OPERATOR_NAMESPACE}

# Verify
oc get dataproduct dataproduct-cr


#Delete DPX artifacts

${CPD_CLI_EXEC} manage delete-cr --components=dataproduct --cpd_instance_ns=${INSTANCE_NAMESPACE}

${CPD_CLI_EXEC} manage delete-olm-artifacts --components=dataproduct --preview=true

${CPD_CLI_EXEC} manage delete-olm-artifacts --components=dataproduct -vvv
