# Spring, Quarkus, NodeJS Lambda benchmark

In this article:
- [Goal](#Goal)
- [Methodology](#Methodology)
  - [Lambda setup](#Lambda-setup)
  - [Load testing](#Load-testing)
  - [Cloudwatch metrics](#Cloudwatch-metrics)
  - [Reference framework documentation](#Reference-framework-documentation)
- [Results](#Results)
- [NodeJS Plot twist](#NodeJS-Plot-twist)
- [Conclusion](#Conclusion)
# Goal
I currently maintain a Java Spring Boot application that serves a website. 
I realized that moving to serverless would simplify the architecture and be more cost efficient.

I want to compare the performance of different Java frameworks so that I can make
a data driven decision of what to use on my scenario. I decided to compare **Quarkus**
to **Spring Cloud Functions** for creating a Lambda function.

# Methodology
I have built simple functions that take an integer ID in order to make a synchronous external
API call, and return its result as JSON to the caller of the Lambda function. This represents
the business scenario I am testing. You should benchmark functions using code representative of your own
business scenarios.

I have used the [camara-api-java-client](https://mvnrepository.com/artifact/io.github.gpr-indevelopment/camara-api-java-client/1.1.1)
library on version 1.1.1 for doing the external API call. This library uses Java 11's HttpClient
for performing synchronous API calls.

## Lambda setup
The source code for the Quarkus and Spring functions are provided in the [functions-quarkus](./functions-quarkus) and [functions-spring](./functions-spring)
maven modules of this project. Both used the Java 11 (Corretto) Lambda runtime.

The handler and deployment packages of both modules were configured as follows:

* Spring: `org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest` and `functions-spring-1.0-SNAPSHOT-aws.jar`
* Quarkus: `io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest` and `function.zip`

## Load testing
The functions were load tested with the [JMeter](https://jmeter.apache.org/) tool. The [jmeter](./jmeter) folder
contains the `.jmx` files for the load test script, and some sample `.csv` results.

JMeter was the tool of choice here because the open source version of Gatling was difficult to use, requiring a script written in Scala and Serverless
Artillery currently has some compatibility issues while not being actively maintained.

All Lambda functions need to have a **public** URL for being called from JMeter. Each Lambda URL
needs to be configured on its corresponding `HTTP Request` component in the JMeter project.

## Cloudwatch metrics
Metrics were collected from the Lambda functions and displayed on a [Cloudwatch](https://aws.amazon.com/cloudwatch/?nc1=h_ls) dashboard. The collected metrics show:

1. Number of invocations of each function.
2. Average, maximum and minimum duration of function executions.
3. Maximum [init](https://docs.aws.amazon.com/lambda/latest/dg/lambda-runtime-environment.html) phase duration.
4. Memory usage information.

The **number** widget for metrics 1 and 2 was configured for each function like the image:
![](C:\Users\Gabriel\Documents\GitHub\cec-lambda-spring-quarkus-node-benchmark\assets\Cloudwatch-setup.JPG)

The **logs** widget for metrics 3 and 4 was configured using a log insights filter. Go to the log
insights page on Cloudwatch, select the function log group, add the filter string and click `Add to dashboard`:

```
filter @message like /(?i)(Init Duration)/
| stats max(@initDuration) as MaxInitDuration, max(@duration) as MaxDuration, avg(@maxMemoryUsed)/1000 as MaximumMemoryUsedKb, avg(@memorySize)/1000 as ProvisionedMemoryKb, (avg(@memorySize) - avg(@maxMemoryUsed))/1000 as OverProvisionedMemoryKb
```

All experiments ran with 10 concurrent threads doing 20 requests each with a ramp-up of 10 seconds.
With these parameters the JMeter script sends 200 requests to each function in total.

## Reference framework documentation

* [Spring Cloud Functions - AWS Lambda docs](https://docs.spring.io/spring-cloud-function/docs/current/reference/html/spring-cloud-function.html#_serverless_platform_adapters)
* [Quarkus - AWS Lambda guide](https://quarkus.io/guides/amazon-lambda)

# Results

Cold starts took 6.66 s on Spring and 7.29 s on Quarkus. Spring was also faster on
average and on minimum durations. On the other hand, Quarkus was more efficient on 
memory consumption with a maximum of 157.4 MB used against 190.5 MB on Spring. The
figure below presents the metrics for the Quarkus and Spring functions:

(Spring has 199 invocations because 1 failed since I exceeded the allowed API request rate...)
![](C:\Users\Gabriel\Documents\GitHub\cec-lambda-spring-quarkus-node-benchmark\assets\Results-spring-quarkus.JPG)

I want to use the Lambda functions as the backend of a web application. For this reason, 
cold starts that take over 1 second are not ideal. Even though this scenario still
uses the JVM, I was expecting better results from Quarkus. Since AWS does not currently support
a container for native Quarkus out of the box, I am not considering doing this customization
(even though there is enough documentation for it).

# NodeJS Plot twist
Since Spring and Quarkus did not deliver the performance I wanted, I decided to
try something more adequate to serverless with the same scenario I applied to the Java frameworks.

I have created a simple NodeJS function using [this code](./functions-node.js). I must
say: It was faster and easier to code and deploy the NodeJS function since everything can be
done with very little effort from the AWS console.

NodeJS was better than the Java frameworks in every metric. It runs with less memory and
cold starts in less than half a second.
![](C:\Users\Gabriel\Documents\GitHub\cec-lambda-spring-quarkus-node-benchmark\assets\Results-nodejs.JPG)

# Conclusion

Even though I am a Java developer, with little knowledge of NodeJS, the data
shows that Java is not the best tool for the serverless task. Even though coding in 
Java is more comfortable to me, I am strongly considering giving NodeJS a go for the sake of performance.