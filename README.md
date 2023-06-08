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

## Architectural choices

TBD

## CI/CD

Docker images deployable to k8s for the frontend and the backend are produced on every commit by github actions.
[See it working](https://github.com/iku000888/k8s-clojure-cljs-postgres-app/actions)

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

End to end tests are the final line of defense against shipping something broken to prod.
Generally these are more expensive to run and sometimes compromises are made to not run them om every commit but rather are run as a 'before deploy to prod' check.

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

Integration tests can overlap overlap in terms of scope with e2e/unit tests, but for this project
I define it as tests serving the following objectives.

- objective:
  - Assure the system on its own is not broken
    - API: test all the helper functions/db access functions/http handlers work together
    - UI: test all components and helper functions work together.
    - For UI, this could be a good place to run cross browser checks
    - OK to test error states esp. if it is significant to consumer.
  - Test a system in its entirety without other other systems involved.
    - In this project I reify this as:
      - UI integration test -> make sure UI code functions minus actual API calls.
      - API integration test -> invoke against running api with http client & check responses.

#### Backend

Look at BDD section

#### Frontend

- The UI pact test achieves the above objective with completely controlled api responses, so the UI pact test is double dipped as the UI integration test.

### Unit test

Unit tests are at the lowest level of the test pyramid.
Generally these should be run on every commit and block a PR from merging when failing.

- objective:
  - fast
    - avoid real world I/O
  - test small parts
    - i.e. individual functions
  - strive to catch as many bugs at this layer

#### API

TBD

#### Frontend

In this project I opted not to author unit tests for frontend for 3 reasons.
1. Prioritize e2e and pact test
2. Most of the lower level components are third party and well tested
3. How the components are combined with stateful interaction is a integration test notion

### Performance/load test

Regardless of how correct the system is, it will immediately face dissapointment if it can only handle 1 request every minute or it suddenly starts blowing up upon crossing some threshold. Ideally we could check how the sytem handles load so we can identify and address it before it goes out the door.
I added a k6 script that hits the api with get requests and post requests with variable duration and concurrency(called vus). I got the api to fail 25% of the requests at 1000vus.

### Generative testing

TBD

### Acceptance test/Functional test/BDD

I explored BDD+Cucumber with [kaocha-cucumber](https://github.com/lambdaisland/kaocha-cucumber), which is a way of describing test scenarios in a human friendly format and backing up them with code to excersize and assert on the scenarios. This doubles as the integration test for the api, but the methodology is orthogonal to the granularity of the test.

To run,

```
make cucumber-test
```

### Contract test

Contract testing allows the provider and the consumer toverify compliance to the API independent of each other.
This is practiced by the [pact family of tools](https://docs.pact.io/).
For this project specifically, I created a pact.json file containing the happy path request/responses for the backend api which is the input to the following usecases

#### Check that the api is compliant to the pact

`make test-pact` takes the pact.json and makes http requests against the api after asking it to be put in a known state. This uses the pact verifier packaged in [the pact cli](https://hub.docker.com/r/pactfoundation/pact-cli)

#### Use the pact as stub service

`make pact-stub` spins up a stub server serving responses as specified in the pact
This uses the pact stub service packaged in [the pact cli](https://hub.docker.com/r/pactfoundation/pact-cli)

#### Check that the stub is compliant to the pact

Kinda silly, but nontheless it is cool that the pact can generate request and responses without any lines of code. Assuming the pact stub server running,
`make test-pact-without-setup` runs the verification from the same pact, except it skips the provider state set up steps.

#### Check that the UI is compliant to the pact

- `make pact-stub`
- `make ui-pact-test`
- open `http://localhost:8701`
