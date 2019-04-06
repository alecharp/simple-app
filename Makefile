IMAGE_NAME=alecharp/simple-app

.PHONY: all clean
all: clean build
clean:
	@mvn clean

.PHONY: build
build: target/simple-app.jar
target/simple-app.jar:
	@mvn clean verify

.PHONY: docker
docker: target/simple-app.jar src/main/docker/Dockerfile
	@docker image build -t $(IMAGE_NAME) -f src/main/docker/Dockerfile .
