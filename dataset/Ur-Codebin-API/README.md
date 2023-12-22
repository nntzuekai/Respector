<p align= "center">
  <a href="https://github.com/Mathew-Estafanous/Ur-Codebin-API"></a>
  
  <h1 align="center">Ur-Codebin [REST API]</h1>

  <p align="center">
    An easy to use REST API backend for the Ur-Codebin website
    <br />
    <a href="https://ur-codebin.herokuapp.com/"><strong>View The Website »</strong></a>
    <br />
    <br />
    <a href="https://mathew.stoplight.io/docs/ur-codebin-api">Explore the docs</a>
    ·
    <a href="https://github.com/Mathew-Estafanous/Ur-Codebin-API/issues">Report Bug</a>
    ·
    <a href="https://github.com/Mathew-Estafanous/Ur-Codebin-API/issues">Request Feature</a>
  </p>
</p>

<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
  * [Technologies Used](#technologies-used)
* [Usage](#usage)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#cloning)
  * [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)


<!-- ABOUT THE PROJECT -->
# About The Project

Ur-Codebin is a website aimed at creating an easy to use code sharing platform, that allows users to quickly 
and efficiently share their code with others. This project is inspired by many other similar websites.

This repository is specifically the backend api for the website. It can be used to handle most requests relating to
the website functionality. This API is secured by JWT token authentication except for all public endpoints. To get 
authenticated, a user must authenticate themselves using the /account/login endpoint. More information about
authentication and other endpoints can be found in the [Api Documentation](https://mathew.stoplight.io/docs/ur-codebin-api).

## Technologies Used

* [Spring Boot](https://spring.io/) - Used as the main framework and foundation for the entire web application.
* [Maven](https://maven.apache.org/) - A very important dependency and library management system.
* [MySQL](https://www.mysql.com/) - This was the database used for the development and production environments.
* [Docker](https://www.docker.com/) - Docker was used to containerize the project to easily deploy on the server.
* [AWS EC2](https://aws.amazon.com/ec2/) - The dockerized application is deployed on an EC2 instance.
* [Nginx](https://www.nginx.com/) - Used as a reverse proxy to route "/api" calls to this REST API service.
* [CloudFlare](https://www.cloudflare.com/) - Mainly used for free SSL certification and caching.

<!-- USAGE EXAMPLES -->
# Usage

You can use this REST API to make calls to add/delete/get any information regarding the Ur-Codebin application, and the 
pastes that have been uploaded. Authentication is mandatory for any non-public endpoints. Authentication is done through
a login endpoint and returns a **Bearer JWT Token** as an authentication header. More information regarding the endpoints
can be found in the [Api Documentation](https://mathew.stoplight.io/docs/ur-codebin-api).

To learn more about the database schema and how the application stores related information. Head to the following
[Database Schema](https://dbdiagram.io/d/5fb41d6a3a78976d7b7c4f73) link and look through the relationships among 
all the tables.

<!-- GETTING STARTED -->
# Getting Started

The following are steps for users who wish to get a local copy of the web application running on their machine,
and contribute to the project.

### Prerequisites

The following needs to be installed on your machine for the application to properly work.
* [Maven](https://maven.apache.org/download.cgi)
    * [Download Tutorial](https://howtodoinjava.com/maven/how-to-install-maven-on-windows/)
* [MySQL On Local Machine](https://dev.mysql.com/doc/mysql-getting-started/en/)

### Cloning

1. Go to your preferred directory
```sh
cd FakeFolder/NotRealDirectory/
```
2. Clone the repo
```sh
git clone https://github.com/Mathew-Estafanous/Ur-Codebin-API.git
```
3. Run app using maven
```sh
mvn spring-boot:run
```

### Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. 
Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<!-- LICENSE -->
# License

This project is protected under the MIT License! See [LICENSE](LICENSE) or <https://en.wikipedia.org/wiki/MIT_License> 
for more detailed information regarding permissions and protections regarding this open-source project.

<!-- CONTACT -->
# Contact

If you have any questions or comments regarding this project. Feel free to reach out through my email.

**Mathew Estafanous -** mathewestafanous13@gmail.com

**Project Link -** https://github.com/Mathew-Estafanous/Ur-Codebin
