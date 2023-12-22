# PROGRAM_NAME is the name of the GIT repository.
# It should match <artifactId> in pom.xml
PROGRAM_NAME := $(shell basename `git rev-parse --show-toplevel`)

# Git variables

GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD)
GIT_REPOSITORY_NAME := $(shell basename `git rev-parse --show-toplevel`)
GIT_SHA := $(shell git log --pretty=format:'%H' -n 1)
GIT_VERSION := $(shell git describe --always --tags --long --dirty | sed -e 's/\-0//' -e 's/\-g.......//')
GIT_VERSION_LONG := $(shell git describe --always --tags --long --dirty)

# Docker variables

DOCKER_IMAGE_NAME := senzing/senzing-api-server

# Misc.

TARGET ?= target

# -----------------------------------------------------------------------------
# The first "make" target runs as default.
# -----------------------------------------------------------------------------

.PHONY: default
default: help

# -----------------------------------------------------------------------------
# Local development
# -----------------------------------------------------------------------------

.PHONY: install
install:
	mvn install \
		-Dgit.branch=$(GIT_BRANCH) \
		-Dgit.repository.name=$(GIT_REPOSITORY_NAME) \
		-Dgit.sha=$(GIT_SHA) \
		-Dgit.version.long=$(GIT_VERSION_LONG) \
		-Dproject.version=$(GIT_VERSION) \
		-DskipTests=True

.PHONY: package
package:
	mvn package \
		-Dgit.branch=$(GIT_BRANCH) \
		-Dgit.repository.name=$(GIT_REPOSITORY_NAME) \
		-Dgit.sha=$(GIT_SHA) \
		-Dgit.version.long=$(GIT_VERSION_LONG) \
		-Dproject.version=$(GIT_VERSION) \
		-DskipTests=True

# -----------------------------------------------------------------------------
# Docker-based package
# -----------------------------------------------------------------------------

.PHONY: docker-package
docker-package: docker-build

	# Build package in a docker container.
	# Copy the maven output from the container to the local workstation.
	# Finally, remove the docker container.

	mkdir $(TARGET) || true
	PID=$$(docker create $(DOCKER_IMAGE_NAME) /bin/bash); \
	docker cp $$PID:/app/ $(TARGET)/; \
	docker rm -v $$PID

# -----------------------------------------------------------------------------
# Docker-based builds
# -----------------------------------------------------------------------------

.PHONY: docker-build
docker-build:
	docker build \
		--no-cache \
		--tag $(DOCKER_IMAGE_NAME) \
		--tag $(DOCKER_IMAGE_NAME):$(GIT_VERSION) \
		.

# -----------------------------------------------------------------------------
# Clean up targets
# -----------------------------------------------------------------------------

.PHONY: docker-rmi-for-build
docker-rmi-for-build:
	-docker rmi --force \
		$(DOCKER_IMAGE_NAME):$(GIT_VERSION) \
		$(DOCKER_IMAGE_NAME)

.PHONY: rm-target
rm-target:
	-rm -rf $(TARGET)

.PHONY: clean
clean: docker-rmi-for-build rm-target

# -----------------------------------------------------------------------------
# Help
# -----------------------------------------------------------------------------

.PHONY: help
help:
	@echo "List of make targets:"
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | awk -v RS= -F: '/^# File/,/^# Finished Make data base/ {if ($$1 !~ "^[#.]") {print $$1}}' | sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$' | xargs
