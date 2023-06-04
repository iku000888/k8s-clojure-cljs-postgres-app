.PHONY: locally-build-images locally-load-images postgres-port-forward minikube-up pull-images
TAG:=0.0.1

health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar:
	cd health-record-app && lein uberjar

health-record-frontend/public/js/compiled/main.js:
	cd health-record-frontend && lein shadow release app

locally-build-images: health-record-app/target/health-record-app-0.1.0-SNAPSHOT-standalone.jar health-record-frontend/public/js/compiled/main.js
	docker build health-record-app -t ghcr.io/iku000888/health-record-api:${TAG}
	docker build health-record-frontend -t ghcr.io/iku000888/health-record-frontend:${TAG}

locally-load-images:
	minikube image load ghcr.io/iku000888/health-record-frontend:${TAG}
	minikube image load ghcr.io/iku000888/health-record-api:${TAG}

pull-images:
	docker pull ghcr.io/iku000888/health-record-api:${TAG}
	docker pull ghcr.io/iku000888/health-record-frontend:${TAG}

postgres-port-forward:
	kubectl port-forward service/postgres-service 5432:5432

minikube-up:
	minikube start
