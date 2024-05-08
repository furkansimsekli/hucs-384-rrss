[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/8l0a6UeB)

# RRSS by FOSSkeeters

## Requirements
- OpenJDK >= 21
- Docker (production only)
- Docker Compose (production only)
- [Spring Boot Extension Pack](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack) for VSCode (optional)

## Build and Run (for local builds)

### IntelliJ
IntelliJ automatically detects, indexes, and builds the project by itself. All you have to do just click the Run icon at
the top right.

The website should be accessible from `localhost:8080`

### CLI
You can immediately run the application with the command below:

```sh
./gradlew bootRun
```

If you would like to make the program hot-reload i.e. restart everytime there is a change in the codebase, then you can
use this command:

```sh
./gradlew compileJava
```

The website should be accessible from `localhost:8080`

## Build and Run (for production builds)

Build and run the docker container with:

```sh
docker compose up --build
```

The website should be accessible from `localhost:8520`. The port number can be changed by editing the `compose.yaml` file.

## Database Path
The default path for the database file is `./rrss.sqlite`. 
This can be easily overriden by setting the environment variable `DB_PATH` to desired path.

## License
This project is released under the terms of the GNU Affero General Public License version 3.
Check out the [LICENSE](/LICENSE) file in this repository for details.
