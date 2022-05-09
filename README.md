# Docker registry with Redis cache based on Kubernetes

`image_retention.groovy` file - Jenkins pipeline.
Setup proper value for DOMAIN variable in `image_retention.groovy`, i.e.:
DOMAIN="your.docker-registry.com"

All k8s manifests are in subfolders.

Docker registry without authentication - can be enabled in deployment.

## How to deploy

AWS ACM and Network Load Balancer are used.

Setup proper values for ACM and DOMAIN variables in deploy.sh

```
# AWS ACM: arn:aws:acm:xxxxxxxxxxx
export ACM="arn:aws:acm:xxxxxxxxxxxxx"
# domain name for your docker registry without schema (http/https): your.docker-registry.com
export DOMAIN="your.docker-registry.com"
```

Run `bash deploy.sh`

## How to remove deployment

Run `bash undeploy.sh`

## Issues

Encountered issues related to using more than 1 replica and emptyDir using for Volumes (instead of PVC).

1) for Deployment with 2 replicas and emptyDir in Volumes - data consistency issue:

```
        - name: image-data
          emptyDir: {}
```

During the docker push have retries and blob upload unknown:
```
with 2 replicas:
f1b5933fe4b5: Pushing [==================================================>]  5.796MB
blob upload unknown
The push refers to repository [MYDOMAIN.com/alpine]
f1b5933fe4b5: Retrying in 10 second 
```

2) for Deployment with 2 replicas and PVC, PVC accessMode should be ReadWriteMany (RWX) (ReadWriteOnce - RWO can be attached to one node only) - so, NFS, GlusterFS, Ceph, etc should be used:

```
Events:
  Type     Reason              Age   From                     Message
  ----     ------              ----  ----                     -------
  Normal   Scheduled           27s   default-scheduler        Successfully assigned docker-registry/docker-registry-cron-1562070600-jpgr8 to ip-172-20-33-53.us-east-2.compute.internal
  Warning  FailedAttachVolume  27s   attachdetach-controller  Multi-Attach error for volume "pvc-70a143c3-82a6-4c82-a45c-94fe607942c7" Volume is already used by pod(s) docker-registry-579469cd77-hv4zg

  Warning  FailedMount  95s    kubelet, ip-172-20-33-53.us-east-2.compute.internal  Unable to mount volumes for pod "docker-registry-cron-1562070000-l7dwz_docker-registry(2c8d33e3-9220-411a-8a6c-5afd3f7d3080)": timeout expired waiting for volumes to attach or mount for pod "docker-registry"/"docker-registry-cron-1562070000-l7dwz". list of unmounted volumes=[image-data]. list of unattached volumes=[image-data default-token-dxktg]
```
