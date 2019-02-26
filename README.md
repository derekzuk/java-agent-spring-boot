# Java Agent for Spring Boot Application

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)

This application is a Java Agent that is designed to gather metrics on a Spring Boot application. The metrics gathered are on requests and responses served by the application, and it has been designed to be decoupled from the web application that it is gathering metrics for.

See the corresponding Spring Boot application if you'd like a quick way to test it out: URL here

# Metric Gathering
This application gathers the following metrics:
- Request Time
- Response Size
- Minimum, average, and Maximum Request Time and Response Size

While the agent is running, it will also add a unique identifier to each HTTP response.

In order to view the metrics, you can use the corresponsing Java Agent Web Application. This agent will occasionally send its metrics out to this web application if it is running: https://github.com/derekzuk/java-agent-web-app

This application has been integrated with Travis-CI, includes unit tests, and stores state in memory. It is also platform agnostic.

# Getting Started
To run this application, first obtain a jar file for both the Java Agent as well as the test application.
```
mvn clean install
```
Then start the test application with your Java Agent application using the javaagent option:
```
java -javaagent:directory/of/java-agent.jar -jar directory/of/test-application.jar
```
Then start the corresponding Java Agent Web Application (https://github.com/derekzuk/java-agent-web-app)
```
mvn clean install
mvn spring-boot:run
```
Navigate through the test application: http://localhost:8080/
Observe the metrics: http://localhost:8081/

### Execution of agent with Attach API

If you'd like to gather metrics on an application that is already running, it should be possible to execute this agent with the Attach API: https://docs.oracle.com/javase/7/docs/technotes/guides/attach/index.html
