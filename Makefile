.PHONY: locally-build-images locally-load-images postgres-port-forward minikube-up
TAG:=0.0.1

health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar:
	cd health-record-app && lein uberjar

locally-build-images: health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar
	docker build health-record-app -t ghcr.io/iku000888/health-record-api:${TAG}

locally-load-images:
	minikube image load ghcr.io/iku000888/health-record-api:${TAG}

postgres-port-forward:
	kubectl port-forward service/postgres-service 5432:5432

minikube-up:
	minikube start
