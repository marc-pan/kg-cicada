#!/usr/bin/env bash

# Configuration
source ./cpd_vars.sh
export CPD_CLI_MANAGE_WORKSPACE=`pwd`/$CPD_CLI_VERSION/cpd-cli-workspace/olm-utils-workspace
export OLM_UTILS_IMAGE=$OLM_UTILS_IMAGE
oc login --username=$KUBEADMIN_USER --password=$KUBEADMIN_PASS --server=https://$INFRA_NODE:6443
if [ $USE_IP == "yes" ]; then
    export INTERNAL_IP=$CLUSTER_IP
fi

# Functions
function prepare_case_resolver() {
    cat > $WORK_DIR/play_env.sh << EOF
export CASECTL_RESOLVERS_LOCATION=/tmp/work/resolvers.yaml
export CASECTL_RESOLVERS_AUTH_LOCATION=/tmp/work/resolvers_auth.yaml
export CASE_TOLERATION='--skip-verify'
export GITHUB_TOKEN=$GITHUB_TOKEN
export CLOUDCTL_TRACE=true
export CASE_REPO_PATH=https://\$GITHUB_TOKEN@raw.github.ibm.com/PrivateCloud-analytics/cpd-case-repo/$VERSION/$BRANCH/case-repo-$BRANCH
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
            url: "https://raw.github.ibm.com/PrivateCloud-analytics/cpd-case-repo/$VERSION/$BRANCH/case-repo-$BRANCH"
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
              password: $CPD_STG_TOKEN
EOF
}

function prepare_mirror() {
    cat > $WORK_DIR/mirror-bedrock.yaml << EOF
apiVersion: operator.openshift.io/v1alpha1
kind: ImageContentSourcePolicy
metadata:
  name: cpd-icsp
spec:
  repositoryDigestMirrors:
  - mirrors:
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    - docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-bootstrap-2-docker-local
    - cp.stg.icr.io/cp/cpd
    - hyc-cloud-private-daily-docker-local.artifactory.swg-devops.com/ibmcom
    - hyc-cp4d-team-bootstrap-docker-local.artifactory.swg-devops.com
    - hyc-cp4d-team-bootstrap-2-docker-local.artifactory.swg-devops.com
    - hyc-cp4d-releng-team-andrew-docker-local.artifactory.swg-devops.com
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    source: quay.io/opencloudio
  - mirrors:
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    - docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-bootstrap-2-docker-local
    - docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-wml-accelerator-docker-local
    - cp.stg.icr.io/cp/cpd
    - cp.stg.icr.io/cp
    - hyc-cloud-private-daily-docker-local.artifactory.swg-devops.com/ibmcom
    - hyc-cp4d-team-bootstrap-docker-local.artifactory.swg-devops.com
    - hyc-cp4d-team-bootstrap-2-docker-local.artifactory.swg-devops.com
    - hyc-cp4d-releng-team-andrew-docker-local.artifactory.swg-devops.com
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    source: icr.io/cpopen
  - mirrors:
    - cp.stg.icr.io/cp/cpd
    - cp.stg.icr.io/cp
    - docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-wml-accelerator-docker-local
    source: cp.icr.io/cp
  - mirrors:
    - cp.stg.icr.io/cp/cpd
    source: cp.icr.io/cp/cpd
  - mirrors:
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    - cp.stg.icr.io/cp/cpd
    - hyc-cloud-private-daily-docker-local.artifactory.swg-devops.com/ibmcom
    - hyc-cp4d-team-bootstrap-docker-local.artifactory.swg-devops.com
    - hyc-cp4d-team-bootstrap-2-docker-local.artifactory.swg-devops.com
    - hyc-cp4d-releng-team-andrew-docker-local.artifactory.swg-devops.com
    source: icr.io/cpopen/cpfs
  - mirrors:
    - docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom
    source: hyc-cloud-private-daily-docker-local.artifactory.swg-devops.com/ibmcom
EOF

    cat > $WORK_DIR/mirror.yaml << EOF
apiVersion: operator.openshift.io/v1alpha1
kind: ImageContentSourcePolicy
metadata:
    name: mirror-config
spec:
    repositoryDigestMirrors:
    - mirrors:
      - docker-na-public.artifactory.swg-devops.com/sys-conductor-wmla-stage-docker-local
      - $WMLA_LOCAL_REGISTRY
      - cp.stg.icr.io/cp/cpd
      source: cp.icr.io/cp/cpd
    - mirrors:
      - docker-na-public.artifactory.swg-devops.com/sys-conductor-wmla-stage-docker-local
      - $WMLA_LOCAL_REGISTRY
      - cp.stg.icr.io/cp/cpd
      - cp.stg.icr.io/cp
      source: icr.io/cpopen
EOF
}

function prepare_wmla_local(){
  cat > $WORK_DIR/wmla_local.yaml << EOF
---
apiVersion: spectrumcomputing.ibm.com/v1
kind: Wmla-add-on
metadata:
  name: wmla
  namespace: $NAMESPACE
spec:
  version: $VERSION
  wmlaNamespace: $NAMESPACE
  imageRegistry: $WMLA_LOCAL_REGISTRY
---
apiVersion: spectrumcomputing.ibm.com/v1
kind: Wmla
metadata:
  name: wmla
  namespace: $NAMESPACE
spec:
  cpdNamespace: $NAMESPACE
  fileStorageClass: $SC_NAME
  license:
    accept: true
    license: Enterprise
  version: $VERSION
  global:
    imageRegistry: $WMLA_LOCAL_REGISTRY
EOF
}

function prepare_edb_postgres() {
    cat > $WORK_DIR/edb_postgres.yaml << EOF
apiVersion: operators.coreos.com/v1alpha1
kind: CatalogSource
metadata:
  name: cloud-native-postgresql-catalog
  namespace: $NAMESPACE_OPERATOR
spec:
  displayName: Cloud Native Postgresql Catalog
  publisher: IBM
  sourceType: grpc
  image: icr.io/cpopen/ibm-cpd-cloud-native-postgresql-operator-catalog@sha256:f12d3a6607555c61fa7a50c4b82e67b54e477ff958e34fb0b5b2e9361f1a4d96
  updateStrategy:
    registryPoll:
      interval: 45m
EOF
}

# PreSteps
if [ $PRE_STEP == "yes" ]; then
    echo "######## PRE STEP ########"
    yum install -y git net-tools podman jq
    if [ ! -d $CPD_CLI_VERSION ]; then
        wget http://icpfs1.svl.ibm.com/zen/cp4d-builds/${VERSION}/dev/cpd-cli/latest/$CPD_CLI_VERSION.tgz
        tar zxvf $CPD_CLI_VERSION.tgz
    fi

    if [ ! -f ${CPD_CLI_VERSION}/cpd-cli ]; then
        echo "The cpd-cli file is not found, please install it."
        exit 1
    fi
fi

# olm-utils refresh
if [ $REFRESH_OLMUTILS == "yes" ]; then
    echo "######## Refresh olm-utils tool ########"
    podman login cp.stg.icr.io -u ${CPD_STG_USER} -p ${CPD_STG_TOKEN}
    podman rm olm-utils-play-v3 --force ## stop and delete running olm-utils container if any
    podman rmi $OLM_UTILS_IMAGE --force ## remove existing olm-utils image if any
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
fi

# prepare mirror
if [ $PREPARE_MIRROR == "yes" ]; then
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    # prepare mirror
    echo "######## Prepare mirror and secret ########"
    WORK_DIR=./$CPD_CLI_VERSION/cpd-cli-workspace/olm-utils-workspace/work
    mkdir -p $WORK_DIR
    rm -rf $WORK_DIR/play_env.sh
    rm -rf $WORK_DIR/resolvers.yaml
    rm -rf $WORK_DIR/resolvers_auth.yaml
    oc login --username=$KUBEADMIN_USER --password=$KUBEADMIN_PASS --server=https://$INFRA_NODE:6443
    oc delete ImageContentSourcePolicy mirror-config
    oc delete ImageContentSourcePolicy cpd-icsp
    prepare_case_resolver
    prepare_mirror
    oc apply -f $WORK_DIR/mirror-bedrock.yaml
    oc apply -f $WORK_DIR/mirror.yaml
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    $CPD_CLI_VERSION/cpd-cli manage add-cred-to-global-pull-secret --registry=$WMLA_LOCAL_REGISTRY --registry_pull_user=$ARTIFACTORY_USER --registry_pull_password=$ARTIFACTORY_TOKEN
    $CPD_CLI_VERSION/cpd-cli manage add-cred-to-global-pull-secret --registry=cp.stg.icr.io --registry_pull_user=$CPD_STG_USER --registry_pull_password=$CPD_STG_TOKEN
    $CPD_CLI_VERSION/cpd-cli manage add-cred-to-global-pull-secret --registry=docker-na-public.artifactory.swg-devops.com/hyc-cloud-private-daily-docker-local/ibmcom --registry_pull_user=$ARTIFACTORY_USER --registry_pull_password=$ARTIFACTORY_TOKEN
    $CPD_CLI_VERSION/cpd-cli manage add-cred-to-global-pull-secret --registry=docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-bootstrap-2-docker-local --registry_pull_user=$ARTIFACTORY_USER --registry_pull_password=$ARTIFACTORY_TOKEN
    $CPD_CLI_VERSION/cpd-cli manage add-cred-to-global-pull-secret --registry=docker-na-public.artifactory.swg-devops.com/sys-conductor-wmla-stage-docker-local --registry_pull_user=$ARTIFACTORY_USER --registry_pull_password=$ARTIFACTORY_TOKEN
    $CPD_CLI_VERSION/cpd-cli manage add-cred-to-global-pull-secret --registry=docker-na-public.artifactory.swg-devops.com/hyc-cp4d-team-wml-accelerator-docker-local --registry_pull_user=$ARTIFACTORY_USER --registry_pull_password=$ARTIFACTORY_TOKEN
fi

if [ $CERT_MANAGER_INSTALL == "yes" ]; then
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS

    # Setup cluster scope ibm-cert-manager-operator and ibm-licensing-operator
    echo "######## Setup cluster scope ibm-cert-manager-operator ########"
    $CPD_CLI_VERSION/cpd-cli manage apply-cluster-components --release=$VERSION --license_acceptance=true --cert_manager_ns=${NAMESPACE_CERT_MANAGER} --licensing_ns=${NAMESPACE_LICENSE_SERVICE}

    # Check ibm-cert-manager-operator is up
    oc get po -n ${NAMESPACE_CERT_MANAGER}
    for i in {1..5}
    do
        echo "Waiting for ibm-cert-manager-operator to be ready. Sleep 30 seconds, attempt: $i"
        sleep 30
        cmd="oc get po -n ${NAMESPACE_CERT_MANAGER} | grep cert-manager | grep 1/1 | grep Running | wc -l"
        ret=`eval $cmd`
        echo "$ret"
        if [[ $ret -eq '5' ]]; then
            echo "ibm-cert-manager-operator is ready."
            break
        elif [[ $i -eq 5 ]]; then
            echo "ERROR: ibm-cert-manager-operator is not ready. Exit..."
            exit 1
        fi
    done

    # Checking ibm-licensing is up
    oc get pod -n ${NAMESPACE_LICENSE_SERVICE}
    for i in {1..5}
    do
        echo "Waiting for ibm-licensing-operator to be ready. Sleep 30 seconds, attempt: $i"
        sleep 30
        cmd="oc logs --tail=3 deploy/ibm-licensing-operator -n ${NAMESPACE_LICENSE_SERVICE} | grep 'reconcile all done'"
        ret=`eval $cmd`
        if [ -n "$ret" ]; then
          echo "ibm-licensing-operator is ready."
          break
        elif [ $i -eq 5 ]; then
          echo "ERROR: ibm-licensing-operator is not ready. Exit..."
          exit 1
        fi
    done
fi

if [ $NFS_CLIENT == "yes" ]; then
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    echo "######## Install nfs-client ########"
    # create nfs_client
    ip_addr=$(ip addr show |grep "inet 10" | grep -v podman | grep -v docker | awk '{print $2}' | cut -d '/' -f 1)
    INTERNAL_IP=${INTERNAL_IP:-${ip_addr}}
    [[ ! -d /data/cloudpak ]] && mkdir -p /data/cloudpak && chmod 777 /data/cloudpak
    $CPD_CLI_VERSION/cpd-cli manage setup-nfs-provisioner --nfs_storageclass_name=$SC_NAME --nfs_server=$INTERNAL_IP --nfs_path=/data/cloudpak
    oc get sc
    # Check nfs-client is ready
    for i in {1..5}
    do
        echo "Waiting for nfs-clinet to be ready. Sleep 5 seconds, attempt: $i"
        sleep 5
        cmd="oc get sc | grep $SC_NAME | wc -l"
        ret=`eval $cmd`
        echo "$ret"
        if [[ $ret -eq '1' ]]; then
            echo "$SC_NAME is ready."
            break
        elif [[ $i -eq 5 ]]; then
            echo "ERROR: $SC_NAME is not ready. Exit..."
            exit 1
        fi
    done
fi

if [ $CPD_INSTALL == "yes" ]; then
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    echo "######## Install CPD ########"
    $CPD_CLI_VERSION/cpd-cli manage authorize-instance-topology --cpd_operator_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE
    $CPD_CLI_VERSION/cpd-cli manage setup-instance-topology --release=$VERSION --cpd_operator_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE --license_acceptance=true
    $CPD_CLI_VERSION/cpd-cli manage apply-olm --components=cpd_platform  --release=$VERSION --cpd_operator_ns=$NAMESPACE_OPERATOR
    #$CPD_CLI_VERSION/cpd-cli manage apply-cr --components=cpd_platform --release=$VERSION --license_acceptance=true --cpd_operator_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE --file_storage_class=$SC_NAME --extra-vars='{"component_custom_spec":{"generateAdminPassword":"false","iamIntegration":"false"}}'
    if [ $IAM_ENABLED = "yes" ]; then
        #$CPD_CLI_VERSION/cpd-cli manage setup-iam-integration --enable=true --cpd_instance_ns=$NAMESPACE
        $CPD_CLI_VERSION/cpd-cli manage apply-cr --components=cpd_platform --release=$VERSION --license_acceptance=true --cpd_operator_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE --file_storage_class=$SC_NAME
    fi
    if [ $IAM_ENABLED = "no" ]; then
        $CPD_CLI_VERSION/cpd-cli manage apply-cr --components=cpd_platform --release=$VERSION --license_acceptance=true --cpd_operator_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE --file_storage_class=$SC_NAME --extra-vars='{"component_custom_spec":{"generateAdminPassword":"false","iamIntegration":"false"}}'
    fi

    # Checking Cloud Pak foundational services are up
    oc get pod -n ${NAMESPACE_OPERATOR}
    for i in {1..5}
    do
        echo "Waiting for foundational services to be ready. Sleep 30 seconds, attempt: $i"
        sleep 30
        cmd="oc logs --tail=3 deploy/ibm-zen-operator -n ${NAMESPACE_OPERATOR} | grep 'failed=0'"
        ret=`eval $cmd`
        if [ -n "$ret" ]; then
          echo "The foundational services is ready."
          break
        elif [ $i -eq 5 ]; then
          echo "ERROR: foundational services is not ready. Exit..."
          exit 1
        fi
    done
fi

if [ $SCHEDULER_INSTALL == "yes" ]; then
    oc create ns $NAMESPACE_SCHEDULER
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    echo "######## Install Scheduler ########"
    $CPD_CLI_VERSION/cpd-cli manage apply-scheduler --release=$VERSION --license_acceptance=true --scheduler_ns=$NAMESPACE_SCHEDULER
fi

if [ $FORCE_UNINSTALL == "yes" ]; then
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    echo "######## Uninstall CPD/Scheduler ########"
    $CPD_CLI_VERSION/cpd-cli manage delete-cr --components=cpd_platform --cpd_operator_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE
    $CPD_CLI_VERSION/cpd-cli manage delete-olm-artifacts --components=cpd_platform --cpd_operator_ns=$NAMESPACE_OPERATOR
    $CPD_CLI_VERSION/cpd-cli manage delete-cr --components=cpfs --cpd_instance_ns=$NAMESPACE_OPERATOR --cpd_instance_ns=$NAMESPACE
    $CPD_CLI_VERSION/cpd-cli manage delete-olm-artifacts --components=cpfs --cpd_operator_ns=$NAMESPACE_OPERATOR

    # For scheduler
    oc project $NAMESPACE_SCHEDULER
    oc delete sched --all
    oc delete mcsched --all
    oc delete sub ibm-cpd-scheduling-catalog-subscription
    oc delete csv ibm-cpd-scheduling-operator.v1.13.0
    oc delete catsrc ibm-cpd-scheduling-catalog
    oc delete crds paralleljobs.ibm.com resourcematches.ibm.com resourceplans.ibm.com scheduling.scheduler.spectrumcomputing.ibm.com mcscheduling.scheduler.spectrumcomputing.ibm.com
    oc delete clusterrolebindings ibm-cpd-scheduler-volume-sched-crb ibm-cpd-scheduling-operator ibm-cpd-scheduling-operator-kube-scheduler-crb
    oc delete sa ibm-cpd-scheduling-operator
    oc delete role ibm-cpd-scheduling-operator
    oc delete rolebindings ibm-cpd-scheduling-operator
    oc delete clusterroles ibm-cpd-scheduling-operator
    oc delete lease cpd-scheduler parallel-job-controller-lease
    oc delete project ${NAMESPACE_SCHEDULER}
    oc delete crd mcschedulings.scheduler.spectrumcomputing.ibm.com
    oc delete crd scheduling.scheduler.spectrumcomputing.ibm.com

    # For cpd
    oc project ${NAMESPACE}
    oc delete project ${NAMESPACE}
    for i in $(oc get analyticsengine -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get bigsqls -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get ccs -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get datarefinery -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get datastage -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get db2aaserviceService -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get db2aaserviceservices -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get endpoints -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get iis -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get notebookruntimes -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get operandbindinfo -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get operandrequest -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get pxruntimes -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get ug -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get wkc -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get wmlbases -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get zenextension -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get rabbitmqclusters -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get authentications.operator.ibm.com -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get services -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get client -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done

    # For operator
    oc project ${NAMESPACE_OPERATOR}
    oc delete project ${NAMESPACE_OPERATOR}
    for i in $(oc get operandbindinfo -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get operandrequest -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done
    for i in $(oc get namespacescope -o name); do $(oc patch $i -p '{"metadata":{"finalizers":[]}}' --type=merge); done

    # For catalog
    oc delete catalogsource cpd-platform -n openshift-marketplace
    oc delete catalogsource ibm-zen-operator-catalog -n openshift-marketplace
    oc delete catalogsource opencloud-operators -n openshift-marketplace

fi


if [ $GENERATE_CERT == "yes" ]; then
    echo "######## Generate cert file ########"
    mkdir -p cert/
    cd cert/
    mydns=`echo ${INFRA_NODE#*.}`
    echo "my dns is: $mydns"
    openssl genpkey -algorithm RSA -out ca.key -pkeyopt rsa_keygen_bits:2048
    openssl req -new -x509 -key ca.key -out ca.crt -days 365 -subj "/C=CH/ST=BEIJING/L=BEIJING/O=IBM/OU=IBM/CN=cpd" -addext "subjectAltName=DNS:*.apps.$mydns"
    openssl genpkey -algorithm RSA -out tls.key -pkeyopt rsa_keygen_bits:2048
    openssl req -new -key tls.key -out tls.csr -subj "/C=CH/ST=BEIJING/L=BEIJING/O=IBM/OU=IBM/CN=cpd" -addext "subjectAltName=DNS:*.apps.$mydns"
    openssl x509 -req -in tls.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out tls.crt -days 365 -extfile <$(printf "subjectAltName=DNS:*.apps.$mydns")
    openssl ecparam -genkey -name secp384r1 -out tls-ecdsa.key
    openssl req -new -key tls-ecdsa.key -out tls-ecdsa.csr -subj "/C=CH/ST=BEIJING/L=BEIJING/O=IBM/OU=IBM/CN=cpd" -addext "subjectAltName=DNS:*.apps.$mydns"
    openssl x509 -req -in tls-ecdsa.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out tls-ecdsa.crt -days 365 -extfile <$(printf "subjectAltName=DNS:*.apps.$mydn")
    ls
    oc create secret generic cpd-tls-secret --namespace ${NAMESPACE} --from-file=ca.crt=./ca.crt --from-file=tls.crt=./tls.crt --from-file=tls.key=./tls.key
    oc project
    oc get secret |grep cpd
    cd ..
    $CPD_CLI_VERSION/cpd-cli manage login-to-ocp --server=https://$INFRA_NODE:6443 -u $KUBEADMIN_USER -p $KUBEADMIN_PASS
    $CPD_CLI_VERSION/cpd-cli manage setup-route --cpd_instance_ns=${NAMESPACE} --route_secret=cpd-tls-secret
    echo quit | openssl s_client -showcerts -connect cpd-${NAMESPACE}.apps.$mydns:443 > cacert.pem
fi
