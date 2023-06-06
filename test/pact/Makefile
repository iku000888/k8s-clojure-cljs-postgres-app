.PHONY: pact-verify pact-shell pact-mock

pact-shell:
	docker run --network host -it -v ${PWD}:/user-home --entrypoint sh docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0

pact-verify:
	docker run --network host -it -v ${PWD}:/user-home docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0 verify /user-home/pact.json --provider-base-url http://localhost:8080 --provider-states-setup-url http://localhost:8080/pact-setup/

pact-verify-without-setup:
	docker run --network host -it -v ${PWD}:/user-home docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0 verify /user-home/pact.json --provider-base-url http://localhost:8080

pact-mockk:
	docker run -it --rm --name pact-mock-service -p 8080:8080 -v ${PWD}:/tmp/pacts docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0 stub-service -p 8080 --host 0.0.0.0 --pact-dir /tmp/pacts --provider=Provider --consumer=Consumer

pact-mock:
	docker run -it --rm --name pact-mock-service -p 8080:8080 -v ${PWD}:/tmp/pacts docker.io/pactfoundation/pact-cli:0.53.0-pactcli0.52.0 stub-service -p 8080 --host 0.0.0.0 /tmp/pacts/pact.json
