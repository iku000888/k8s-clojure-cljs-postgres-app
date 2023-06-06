# App

## Prerequisites

- minikube https://minikube.sigs.k8s.io/docs/start/
- docker cli
- login set up to ghcr.io where the built images are hosted
- (for local dev)
  - leiningen
  - npm


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

## Automated Testing

Software Testing has many degrees of freedom in terms of what to test at which layer.
While the optimal choice is highly contextual in real life, in this project I followed the test [pyramid philosophy](https://martinfowler.com/articles/practical-test-pyramid.html) as top level guideline.

### End To End test

- objective:
  - assure all systems involved work together
  - make sure the entire stack is working from user interface down to the infrastructure layer
    - In the context of this project, want to test integration between UI <-> API <-> Infrastructure
- avoid:
  - testing every possible combination - volume of tests should be minimum as this is the top of the test pyramid
    - e.g.
      - not every browser
      - not every error/invalid state
- how:
  - `make run-headless-e2e`
    - test/e2e/cypress/e2e/spec.cy.js

### Integration test

- objective:
  - Assure the system on its own is not broken
    - For UI, this could be a good place to run cross browser checks
  - Test a system in its entirety without other other systems involved.
    - In this project I reify this as:
      - UI integration test -> make sure UI code functions minus API calls.
      - API integration test -> invoke against running api with http client & check responses.

### Unit test

#### API

#### Frontend

### Performance test
