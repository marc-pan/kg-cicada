## Kubernetes Pods in YS1Dev

In some cases, we may need to access pods in YS1Dev for debugging. For example, for debugging a problem that happens on startup, it may be required to bounce a pod or temporarily create an additional replica to watch the startup happen. Or if a pod is down, it may be necessary to see why.

1. Download the required CLI tools:

    ibmcloud
    ibmcloud ks
    kubectl

2. Log into IBM cloud using ibmcloud login --sso
When prompted, select the account DAP Deployment's Account and the region us-south

3. Use the kubernetes service to get the cluster config - this will return a path to the config file location:

ibmcloud ks cluster config --cluster 31cccad0d921437c901eea15e74babf5

4. Set KUBECONFIG to the file path returned above:

export KUBECONFIG=<path-to-config>

5. Use kubectl commands to navigate the pods and perform desired actions.
