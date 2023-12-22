# senzing-rest-api-specification

## Overview

The Senzing REST API specification is an OpenAPI 3.0 Specification of the RESTful API supported by the
[Senzing API server](https://github.com/Senzing/senzing-api-server).

## View (Latest version)

Viewing the Senzing REST API OpenAPI specification:

1. In [Swagger UI](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/main/senzing-rest-api.yaml)
1. In [Swagger validator](http://validator.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/main/senzing-rest-api.yaml)
1. In [Swagger editor](http://editor.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/main/senzing-rest-api.yaml)
1. Using Docker.
  Example:

    ```console
    sudo docker run \
      --env URL=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/main/senzing-rest-api.yaml \
      --name senzing-swagger-ui \
      --publish 9180:8080 \
      --rm \
      swaggerapi/swagger-ui
    ```

   Open browser to [localhost:9180](http://localhost:9180)
1. In [GitHub](senzing-rest-api.yaml)

## View (Previous Versions)

View previous versions of the Senzing REST API OpenAPI specification:

- [Version 3.1.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/3.1.0/senzing-rest-api.yaml)
- [Version 3.0.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/3.0.0/senzing-rest-api.yaml)
- [Version 2.7.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.7.0/senzing-rest-api.yaml)
- [Version 2.6.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.6.0/senzing-rest-api.yaml)
- [Version 2.5.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.5.0/senzing-rest-api.yaml)
- [Version 2.4.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.4.0/senzing-rest-api.yaml)
- [Version 2.3.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.3.0/senzing-rest-api.yaml)
- [Version 2.2.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.2.0/senzing-rest-api.yaml)
- [Version 2.1.1](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.1.1/senzing-rest-api.yaml)
- [Version 2.1.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.1.0/senzing-rest-api.yaml)
- [Version 2.0.0](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/2.0.0/senzing-rest-api.yaml)
- [Version 1.8.2](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/1.8.2/senzing-rest-api.yaml)
- [Version 1.8.1](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/1.8.1/senzing-rest-api.yaml)
- [Version 1.7.5](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Senzing/senzing-rest-api-specification/1.7.5/senzing-rest-api.yaml)

## Submitting comments

Please create an [issue](https://github.com/Senzing/senzing-rest-api-specification/issues) for any requests for clarification or change.
