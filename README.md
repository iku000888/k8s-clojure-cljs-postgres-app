# App

## Prerequisites

- minikube https://minikube.sigs.k8s.io/docs/start/
- docker cli
- login set up to ghcr.io where the built images are hosted

## Running@k8s

- `minikube start`
- `make pull-images TAG=main` OR `make locally-build-images TAG=main`
- `make locally-load-images TAG=main`
- `minikube kubectl -- apply -f k8s/deployment.yml`
- tab 1 `make api-port-forward`
- tab 2 `make frontend-port-forward`
- open http://localhost:8700

## Running@ locally for dev

### API

- tab 1 `make postgres-port-forward`
- tab 2
  - cd `health-record-app`
  - `lein repl` -> `(start)`

### Frontend

- cd `health-record-frontend`
- `lein shadow watch app`
