
This is a Java API for parking lot.
Technologies used:

 * Java 11
 * Spring Boot
 * Docker

# About the application

 Parking lot contains different types of slots:
 
  * Slots for regular cars (with gasoline)
  * Slots for electric cars with 20 KW power outlet
  * Slots for electric cars with 50 KW power outlet
 
 As the car enters the parking, it receives a ticket with parking slot location.
 As the car leaves, the driver should be charged with amount according to the time spent at the slot.
 
 Pricing policy is flexible and can be defined by parking owner.
 Default model is SIMPLE - charge according to hourly rate and hours spent in parking. The first hour is free.
 
 # Building and running the application.

 
## Prerequisites
 
  * Java 11,
  * Maven 3+
 
 ### Build the application
 ```
 mvn clean install -DskipTests=true
 ```
 ### Run the server at localhost:
 
raw java command:
```
 java -jar target/parking-lot-0.0.1-SNAPSHOT.jar
```
handy script - takes profile as optional argument (dev/stg/prod,  default - dev)
```
 chmod +x run-local.sh && ./run-local.sh stg
```
 ### Run the server in docker container:
 
 raw command
```
 docker run  -ti snripa/parking-lot:master -p 8080:8080
```
 
 handy script - takes profile as optional argument (dev/stg/prod,  default - dev)
```
 chmod +x run-local.sh && ./run-docker.sh stg
```

After the start, application should be available:


 * Documentation: http://localhost:8080/specs
 * API: http://localhost:8080
