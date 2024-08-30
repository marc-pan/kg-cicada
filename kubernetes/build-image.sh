#!/bin/bash

JANUSGRAPH_VERSION="0.6.0"
export NAMESPACE="wkc"
export REGISTRY_HOST=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')
export IMAGE_NAME="kg-graph-export"
export IMAGE_VERSION="latest"

function prechecking() {
  for cmd in podman oc
  do
    command -v ${cmd} 2>&1 > /dev/null
    if [ $? -ne 0 ]
    then
      echo "The podman ${cmd} is not found, please install it first."
      exit -1
    fi
  done

  if [ ! -f janusgraph-${JANUSGRAPH_VERSION}.tar ]
  then
    echo "The file janusgraph-${JANUSGRAPH_VERSION}.tar is not found."
    exit -1
  fi

  _deploy_name=$(oc get deploy/${IMAGE_NAME} -n wkc -o jsonpath='{.metadata.name}')
  if [ x"${_deploy_name}" = x"${IMAGE_NAME}" ]
  then
    oc delete -f ./kg-export-deploy.yaml -n ${NAMESPACE}
  fi

  _image_id=$(podman images --filter "label=${IMAGE_NAME}" --noheading --format json | jq .[].Id | tr -d '"')
  if [ ! -z "${_image_id}" ]
  then
    podman rmi --force ${_image_id}
  fi
}

prechecking

# Build and push the docker image to OCP registry repostiry
# Reference to this topic https://docs.openshift.com/container-platform/4.10/registry/index.html

podman login --username kubeadmin --password $(oc whoami -t) --tls-verify=false ${REGISTRY_HOST}
podman build --force-rm --file ./Dockerfile.kg --tag ${IMAGE_NAME} --tls-verify=false --label ${IMAGE_NAME} .
podman tag localhost/${IMAGE_NAME}:${IMAGE_VERSION} ${REGISTRY_HOST}/${NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION}
podman push --tls-verify=false ${REGISTRY_HOST}/${NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION}

# Notice: internal registry is image-registry.openshift-image-registry.svc:5000

# Deploy the pod as root running in specified namespace
oc adm policy remove-scc-from-user anyuid -z default
oc adm policy add-scc-to-user anyuid -z default

oc apply -f ./kg-export-deploy.yaml -n ${NAMESPACE}

while true
do
  _pod_num=$(oc get deploy -n ${NAMESPACE} ${IMAGE_NAME} -o jsonpath='{.status.availableReplicas}')
  if [ x"${_pod_num}" = "x1" ]
  then
    echo "The graph export pod is running now."
    oc get pod -n ${NAMESPACE} | grep -i -e "^${IMAGE_NAME}"
    break
  fi
  sleep 3
done
