# wopr
This is an example application that I wrote to teach my teammates how to run a Java Spring Boot application on Kubernetes.

## Prerequisites
* [Git](https://git-scm.com/) >= 2.25.1
* [Java SE JDK](https://www.oracle.com/technetwork/java/javase/overview/index.html) >= 11.0.10 2021-01-19
* [Gradle](https://gradle.org/) >= 6.6.1
* [Docker](https://www.docker.com/) >= 19.03.13
* [Docker Compose](https://docs.docker.com/compose/) >= 1.28.2
* [kubectl](https://kubernetes.io/docs/reference/kubectl/kubectl/) >= 1.11.0
* [Minikube](https://minikube.sigs.k8s.io/) >= 1.17.1

## Usage
### Build and run the .jar file
```
git clone
gradle build
java -jar build/libs/wopr.jar
http://localhost:8080/
http://localhost:8080/actuator/health
```

#### Take a look at how externalized config works
```
java -jar build/libs/wopr.jar --spring.config.location=/path/to/some/other/application.properties

java -jar -Dmessage.text="This is one way to do it." build/libs/wopr.jar

java -jar -Dspring.application.json='{"message.text":"This is a different way to do it."}' build/libs/wopr.jar
```

### Build and run the Docker container image
```
docker build . --tag wopr:latest
docker run -p 8080:8080 wopr:latest
```

#### Take a look at how externalized config works
```
docker run -p 8080:8080 -e message.text="More ways." wopr:latest

docker run -p 8080:8080 -e spring.application.json='{"message.text":"This is a valid way to do it."}'  wopr:latest
```

### Start the cluster
```
minkube start
kubectl config use-context minikube
kubectl cluster-info
minikube dashboard
```

### Deploy the service, deployment, and configmap manifests to the cluster
```
kubectl create namespace example1
kubectl create -f wopr-example1-envvars.yaml  --namespace=example1
minikube service wopr --namespace example1

kubectl create namespace example2
kubectl create -f wopr-example2-applicationproperties.yaml --namespace=example2
minikube service wopr --namespace example2

kubectl delete namespaces example1 example2
```

