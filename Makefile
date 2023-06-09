.PHONY: locally-build-images locally-load-images postgres-port-forward minikube-up pull-images api-port-forward frontend-port-forward test-e2e test-pact pact-stub ui-patct-test k6-test cucumber-test api-unit-test
TAG:=0.0.1

health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar:
	cd health-record-app && lein uberjar

health-record-frontend/public/js/compiled/main.js:
	cd health-record-frontend && lein shadow release app

locally-build-images: health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar health-record-frontend/public/js/compiled/main.js
	docker build health-record-app -t ghcr.io/iku000888/health-record-api:${TAG}
	docker build health-record-frontend -t ghcr.io/iku000888/health-record-frontend:${TAG}

minikube-rm-images:
	minikube image rm ghcr.io/iku000888/health-record-api:${TAG}

locally-load-images:
	minikube image load ghcr.io/iku000888/health-record-frontend:${TAG}
	minikube image load ghcr.io/iku000888/health-record-api:${TAG}

pull-images:
	docker pull ghcr.io/iku000888/health-record-api:${TAG}
	docker pull ghcr.io/iku000888/health-record-frontend:${TAG}

postgres-port-forward:
	kubectl port-forward service/postgres-service 5432:5432

api-port-forward:
	kubectl port-forward service/api-service 8080:8080

frontend-port-forward:
	kubectl port-forward service/frontend-service 8700:3000

minikube-up:
	minikube start

test-e2e:
	cd test/e2e/ && make run-headless

test-pact:
	cd test/pact/ && make pact-verify

test-pact-without-setup:
	cd test/pact/ && make pact-verify-without-setup

pact-stub:
	cd test/pact/ && make pact-stub

cypress-open:
	cd test/e2e/ && make cypress-open

ui-patct-test:
	cd health-record-frontend && lein shadow watch component-test

k6-test:
	cd test/k6/ && k6 run load-test.js

cucumber-test:
	cd health-record-app && lein kaocha features

api-unit-test:
	cd health-record-app && lein test
