
# How to list all resources and custom resources in OpenShift

## Environment

Red Hat OpenShift Container Platform (RHOCP)
  - 3.11
  - 4

## Issue

- How to list all resources in an OpenShift cluster.
- How to list all resources in an OCP namespace.
- How to list all the non-namespaced resources in an OCP cluster.
- How to see all the custom resources in an OpenShift cluster.
- `oc get all` does not return all the resources on a namespace.

## Resolution
### List all CRDs with CR name and Scope

```
#   oc get crd -o=custom-columns=NAME:.metadata.name,CR_NAME:.spec.names.singular,SCOPE:.spec.scope
```

### List every single custom resources in the cluster

```
#  oc get $(oc get crd -o=custom-columns=CR_NAME:.spec.names.singular --no-headers | awk '{printf "%s%s",sep,$0; sep=","}') --ignore-not-found --all-namespaces -o=custom-columns=KIND:.kind,NAME:.metadata.name,NAMESPACE:.metadata.namespace
```
> Note: It's possible to change `-o=custom-columns=KIND:.kind,NAME:.metadata.name,NAMESPACE:.metadata.namespace` with whatever display options needed. It's also possible to change that output and use `--show-kind` for an output similar to the `oc get all` command.

### List every single resource in the cluster (custom and non-custom)

```
# oc get $(oc api-resources --verbs=list -o name | awk '{printf "%s%s",sep,$0;sep=","}')  --ignore-not-found --all-namespaces -o=custom-columns=KIND:.kind,NAME:.metadata.name,NAMESPACE:.metadata.namespace --sort-by='metadata.namespace'
```
> Note: It's possible to change `-o=custom-columns=KIND:.kind,NAME:.metadata.name,NAMESPACE:.metadata.namespace --sort-by='metadata.namespace'` with whatever display options needed. It's also possible to change that output and use `--show-kind` for an output similar to the `oc get all` command.

### List every single non-namespaced resource in the cluster

```
# oc get $(oc api-resources --namespaced=false --verbs=list -o name | awk '{printf "%s%s",sep,$0;sep=","}')  --ignore-not-found --all-namespaces -o=custom-columns=KIND:.kind,NAME:.metadata.name --sort-by='kind'
```
> Note: It's possible to change `-o=custom-columns=KIND:.kind,NAME:.metadata.name --sort-by='kind'` with whatever display options needed. It's also possible to change that output and use `--show-kind` for an output similar to the `oc get all` command.

### List every single namespaced resource in a namespace

Replace ${NAMESPACE} with the correct namespace (or omit -n ${NAMESPACE} for the current namespace):

```
# oc get $(oc api-resources --namespaced=true --verbs=list -o name | awk '{printf "%s%s",sep,$0;sep=","}')  --ignore-not-found -n ${NAMESPACE} -o=custom-columns=KIND:.kind,NAME:.metadata.name --sort-by='kind'
```
> Note: It's possible to change `-o=custom-columns=KIND:.kind,NAME:.metadata.name --sort-by='kind'` with whatever display options needed. It's also possible to change that output and use `--show-kind` for an output similar to the `oc get all` command.

## Root Cause

The `oc get all` command only shows a subset of resources.


## References
- [How to list all resources and custom resources in OpenShift ](https://access.redhat.com/solutions/3986301)
