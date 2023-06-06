.PHONY: pact-verify

pact-shell:
	docker run --network host -it -v ${PWD}:/user-home --entrypoint sh docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0

pact-verify:
	docker run --network host -it -v ${PWD}:/user-home docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0 verify /user-home/pact.json --provider-base-url http://localhost:8080 --provider-states-setup-url http://localhost:8080/pact-setup/
