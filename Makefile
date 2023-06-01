.PHONY: build-api
TAG:=0.0.1

health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar:
	cd health-record-app && lein uberjar

build-images: health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar
	docker build health-record-app -t ghcr.io/iku000888/health-record-api:${TAG}

image-load:
	minikube image load ghcr.io/iku000888/health-record-api:${TAG}
