# Java Agent Spring Boot

This is a java agent meant to provide metrics for a spring boot web application.

### Execution of agent from command line

```
java -javaagent:java-agent-instrumentation-extension-1.0-full.jar -jar spring-boot-angular-0.0.1-SNAPSHOT.jar > log.txt
```

### Execution of agent with Attach API

Check project [Attach API Agent Loader](https://github.com/jakubhalun/tt2016_attach_api_agent_loader) to load agent with Attach API.