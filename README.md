# wopr
Updating this 

This project demonstrates using:
* `Spring Boot` to create an app named `wopr` that uses an external `config file` or `env vars` to display a message in your browser. The app will run a webserver that is accessible on `localhost:8080` and that displays some text defined in an `application.properties` config file. The app currently consists of a single service defined as a `container` and deployed to `kubernetes` that displays a message.
* `Docker` to create a `Docker container image` using a `Dockerfile` to containerize the app.
* `Kubernetes` to deploy and run your `Docker container image` in a `Kubernetes` cluster.
* `Kubernetes ConfigMaps` to mount a `config file` in the container that is running the app.

## Getting started
### Prerequisites
* [Git](https://git-scm.com/) >= 2.17.1
* [Java SE JDK](https://www.oracle.com/technetwork/java/javase/overview/index.html) >= 10.0.2
* [Apache Maven](https://maven.apache.org/) >= 3.5.2
* [Docker CE](https://www.docker.com/) >= 18.06.1
* [kubectl](https://kubernetes.io/docs/reference/kubectl/kubectl/) >= 1.11.0
* [Minikube](https://github.com/kubernetes/minikube) >= 0.28.2

### Installation
Clone the repo and cd into it:


## Maven
### Run
Use mvn to run the app without building the jar.

Spring Boot will look for a config file in `config/` or the current directory or `env vars` to populate config values:
```
mvn spring-boot:run
```

### Access
Access the running app:

[`http://localhost:8080`](http://localhost:8080)

The pom.xml file adds `spring-boot-starter-actuator` which gives us a default health endpoint we can use to check the health of the running app: 

[`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health)

### Build
Build the jar file using maven:
```
mvn package
```

## Java
### Run
Use java to run the jar.

Spring Boot will look for a config file in `config/` or the current directory:
```
java -jar target/*.jar
```

You can specify a different configuration file at runtime:

```
java -jar target/*.jar --spring.config.location=some-other-application.properties
```
### Access
Access the running app:

[`http://localhost:8080`](http://localhost:8080)

The pom.xml file adds `spring-boot-starter-actuator` which gives us a default health endpoint we can use to check the health of the running app: 

[`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health)

## Docker
### Build
Build a `Docker container image` using the `Dockerfile` and `Docker CE`:
```
docker build . --tag wopr --file Dockerfile
```

List all your local `Docker container images`:
```
docker images
```
### Run
Run your `Docker container image` to create a `Docker container` that runs `wopr` and forwards host port 8080 to container port 8080:
```
docker run -p 8080:8080 wopr
```

### Access
Access `wopr` running on a `Docker container`. Only the default values will be shown because we did not bundle a config file `application.properties` in the container image:

[`http://localhost:8080`](http://localhost:8080)

The pom.xml file adds `spring-boot-starter-actuator` which gives us a default health endpoint we can use to check the health of the running app: 

[`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health)

## Kubernetes
### minikube
Start a single node `Kubernetes` cluster using `Minikube` on `virtualbox` (this can take 5 minutes):
```
minikube start --vm-driver virtualbox
```

Switch `kubectl` to the `minikube` context:
```
kubectl config use-context minikube
```

Check-out the details of your cluster to make sure it's working:
```
kubectl cluster-info
```

Use `minikube` to open the `Kubernetes` dashboard:
```
minikube dashboard
```

SSH in to your minikube cluster:
```
minikube ssh
```

Git clone the repo:


Build a `Docker container image` using the `Dockerfile` in the repo:
```
docker build . --tag wopr --file Dockerfile
```

Exit the minikube vm:
```
exit
```

Deploy your app to:
```
kubectl create -f wopr.yml
```

Use `minikube` to call the backend service and open `wopr` in your browser:
```
minikube service wopr
```
