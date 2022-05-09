#!/usr/bin/env bash

# AWS ACM: arn:aws:acm:xxxxxxxxxxx
#####Put ACM ARN########
export ACM="arn:aws:acm:xxxxxxxxxxx"
# domain name for your docker registry without schema (http/https): your.docker-registry.com
export DOMAIN="domain"

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.2.0/deploy/static/provider/cloud/deploy.yaml
envsubst < ./ingress-nginx/service-nlb.yaml.tmpl > /tmp/service-nlb.yaml
kubectl apply -f /tmp/service-nlb.yaml

kubectl apply -f ./namespace/namespace.yaml

kubectl apply -f ./redis/deployment.yaml
kubectl apply -f ./redis/service.yaml

kubectl apply -f ./registry/pvc.yaml
kubectl apply -f ./registry/deployment.yaml
kubectl apply -f ./registry/service.yaml
envsubst < ./registry/ingress.yaml.tmpl > /tmp/ingress.yaml
kubectl apply -f /tmp/ingress.yaml
kubectl apply -f ./registry/garbagecron.yaml
