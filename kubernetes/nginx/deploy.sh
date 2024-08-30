#!/bin/env bash

set -euxo pipefail

export NGINX_NAMESPACE="dpx"
export NGINX_DEPLOY_NAME="nginx-hello-world"

# sed -i "s/nginx-hello-world/${NGINX_DEPLOY_NAME}/g" buildconfig.yaml deployment.yaml service.yaml route.yaml

expose_registry() {
  oc patch configs.imageregistry.operator.openshift.io cluster --type merge --patch '{"spec":{"managementState":"Managed"}}'
  oc patch configs.imageregistry.operator.openshift.io cluster --type merge --patch '{"spec":{"storage":{"emptyDir":{}}}}'
  oc get pod -n openshift-image-registry -l docker-registry=default
  export REGISTRY_HOST=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')
  [[ -z ${REGISTRY_HOST} ]] && echo "Not found default route in openshift-image-registry project." && exit 1

  podman login --tls-verify=false --username kubeadmin --password $(oc whoami -t) $REGISTRY_HOST
  oc login --token=$(oc whoami -t) https://${REGISTRY_HOST}:6443 --insecure-skip-tls-verify=true
}

deploy_nginx() {
  oc create -f buildconfig.yaml -n ${NGINX_NAMESPACE}
  oc create imagestream ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
  oc start-build ${NGINX_DEPLOY_NAME} --from-dir=./ --follow -n ${NGINX_NAMESPACE}

  oc apply -f deployment.yaml -n ${NGINX_NAMESPACE}
  oc get deployment ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}

  oc apply -f service.yaml -n ${NGINX_NAMESPACE}

  oc apply -f route -n ${NGINX_NAMESPACE}
  oc get route ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
}

delete_nginx() {
  oc delete buildconfig ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
  oc delete imagestream ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
  oc delete deployment ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
  oc delete service ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
  oc delete route ${NGINX_DEPLOY_NAME} -n ${NGINX_NAMESPACE}
}

usage() {
  echo "Usage: ./deploy.sh <deploy | delete>"
  exit 0
}

ACTION=${1:-0}

case "$ACTION" in
  "deploy")
    expose_registry
    deploy_nginx
    ;;
  "delete")
    expose_registry
    delete_nginx
    ;;
  *)
    usage
    ;;
esac
