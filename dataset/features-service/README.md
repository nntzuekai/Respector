
[![Build Status](https://travis-ci.org/JavierMF/features-service.svg?branch=develop)](https://travis-ci.org/JavierMF/features-service)

# Features Model MicroService

REST MicroService for managing products Feature Models (https://en.wikipedia.org/wiki/Feature_model)

As defined by Wikipedia, "a feature model is a compact representation of all the products of the Software Product Line (SPL) in terms of features". The focus of SPL development is on the systematic and efficient creation of similar programs.

The goal of this project is to provide a REST based service for:
 - **defining products**, their available features and the activation constraints between its features.
 - **defining product configurations**, understood as a set of active features of those supported by the product which fulfil the features constraints.
 - **querying the active features for a configuration**, so an application in runtime can change its behaviour according to the active features of a configuration depending, for instance, on the logged user, the client, etc.


## Using the REST service

The project provides REST resources for defining and querying both products and product configurations. Let's use as running example an e-learning website which supply infrastructure to its clients so they can provide online courses. The company charges its clients depending on the enabled features, so the web application adapts its behaviour showing or hiding links and sections when a student is logged.
Some of the supported features are video lessons, online forums and chats for support, payments with credit card, PayPal or wires, redeem codes, etc.

The complete REST API specification can be found when running the application in the [/swagger.json](https://features-models-service.herokuapp.com/swagger.json) path.

### Working wth Products

To add a new product:
```
POST /products/ELEARNING_SITE
```
To add new features:
```
POST /products/ELEARNING_SITE/features/ONLINE_FORUM
```
To remove a feature:
```
DELETE /products/ELEARNING_SITE/features/REDEEM_CODES
```
To request a list with the names of all the available products:
```
GET /products
```
To request the features and constraints of a product:
```
GET /products/ELEARNING_SITE
```
To remove and existing product and all its configurations:
```
DELETE /product/ELEARNING_SITE
```
### Working wth Product Feature Constraints

Once added to a product, feature constraints can be removed:
```
DELETE /products/ELEARNING_SITE/constraints/4
``````
Currently we support the following constraints between features:

#### Required Constraint

When a feature requires another feature, it will be automatically added to the configuration when the source feature is added.

For instance, the PAYPAL_FEATURE requires that the COURSE_SELLING feature is active, so the COURSE_SELLING feature will be automatically added to the configuration when the PAYPAL_PAYMENT feature is added.
In fact, the COURSE_SELLING feature will not be able to be deactivated while the PAYPAL_FEATURE is active.

To add a required constraint:
```
POST /products/ELEARNING_SITE/constraints/required
```
sending as form parameters "sourceConstraint=PAYPAL_FEATURE" and "requiredConstraint=SELLING_COURSES".

#### Excluded Constraint

When a feature excludes another feature, it will turn the configuration invalid if both of them are active.

For instance, a ON_TRIAL_PERIOD feature excludes the COURSE_SELLING feature, so a client on trial period can not access to the functionality provided for selling courses.
If the COURSE_SELLING feature is actived while the ON_TRIAL_PERIOD is active, the service will save the activation but it will return an error and the configuration will be marked as invalid.

To add an excluded constraint:
```
POST /products/ELEARNING_SITE/constraints/excluded
```
sending as form parameters "sourceConstraint=ON_TRIAL_PERIOD" and "excludedConstraint=SELLING_COURSES".

### Working with Product Configurations

To add a new configuration for one product:
```
POST /products/ELEARNING_SITE/configurations/CLIENT1
```
To activate a feature in a configuration:
```
POST /products/ELEARNING_SITE/configurations/CLIENT1/features/COURSE_SELLING
```
To deactivate a feature in a configuration:
```
DELETE /products/ELEARNING_SITE/configurations/CLIENT1/features/REDEEM_CODES
```
To query all the active features names in a configuration:
```
GET /products/ELEARNING_SITE/configurations/CLIENT1/features
```
To remove a configuration:
```
DELETE /products/ELEARNING_SITE/configurations/CLIENT1
```
