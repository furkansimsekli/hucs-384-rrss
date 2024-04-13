[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/8l0a6UeB)

# RRSS by Fosketeers

## Requirements
- OpenJDK >= 21
- Docker
- Docker Compose
- [Spring Boot Extension Pack](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack) for VSCode (optional)

## Build and Run (for local builds)
Create a directory named `data`. This step is only required once.

```sh
mkdir data
```

Then run the application with:

```sh
./gradlew bootRun
```

The website should be accessible from `localhost:8080`

## Build and Run (for production builds)
Build a JAR file containing all the dependencies:

```sh
./gradlew build
```

And then build and run the docker container with:

```sh
docker compose up --build
```

The website should be accessible from `localhost:8520`. The port number can be changed by editing the `compose.yaml` file.

## License
This project is released under the terms of the GNU Affero General Public License version 3.
Check out the [LICENSE](/LICENSE) file in this repository for details.