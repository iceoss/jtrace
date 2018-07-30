# JTrace Java Profiler

JTrace is a lightweight, low-overhead CPU profiling agent for Java applications. Currently it only profiles method executions and tracks # of invocations, method own time and method total time of the methods included for profiling.

The agent has been designed for use in local/staging/build environments to provide the least amount of overhead and performance impact possible while providing valuable execution statistics for tracking down potential performance issues. Running in production environments is currently discouraged until the agent has been proven stable.

## JTrace Goals

- Free.
- Easy to use in local development environments
- Easy to integrate with CI/build processes. (ex: Profiling unit tests / integration tests)
- Easy to run on QA/staging servers with **minimal performance degradation**
- Designed for use with large web applications.

## What JTrace Can Do

- Profile method executions tracking number of *method invocations*, *method own time* and *method total time* while providing the least amount of performance impact to running applications.

## What JTrace Cannot Do (currently)

- Provide method execution stack traces. Keeping track of stack traces adds too much overhead. Instead JTrace uses "contexts" which will only track executions for stack traces which originate from specified contexts (see below)
- Profile memory usage
- Profile object allocations
- Profile threading
- Profile JDBC/SQL statements
- Any other type of profiling

## Planned Future Features

- Ability to separate method execution metrics using method parameters or instance field values
- SQL/JDBC Profiling
- Method execution stack trace recording
- GUI Application

## Downloads

- [jtrace-dist-0.0.1.jar](....)

## Profiling Java Applications

    java -javaagent:jtrace-dist-0.0.1.jar=file=profile.bin,includes=com\.mypackage\..* com.mypackage.Main
    
## Profiling Unit Tests (Maven Surfire)
    
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.18.1</version>
                <configuration>
                    <redirectTestOutputToFile>false</redirectTestOutputToFile>
                    <testSourceDirectory>src/test/java</testSourceDirectory>
                    <includes>
                        <include>**/*.java</include>
                    </includes>
                    <forkCount>1</forkCount>
                    <reuseForks>false</reuseForks>
                    <argLine>-javaagent:${basedir}/jtrace-dist-0.0.1.jar=file=target/profile.bin,includes=com\\.mypackage\\..*</argLine>
                </configuration>
            </plugin>
        </plugin>
    </build>
    ...

## Agent Arguments

- **disableMethodProfiling**: Disables method profiling. (For future usage when there is more than method profiling)
- **resolution=[nanos|millis]**: Defines the time resolution used when profiling (default nanos)
- **includes=regex**: Regex pattern of classes to profile. May be defined multiple times.
- **excludes=regex**: Regex pattern of classes to exclude from profiling. May be defined multiple times.
- **context=regex**: Regex pattern of classes to use for contextual profiling.
- **disableSL4J**: Optionally disable SL4J detection and usage (If you have custom appenders in the package being profiled using SL4J might cause the profiler to not startup correctly)
- **interval=1000**: Defines the time in milliseconds in which the profile data will be published (default 5000)
- **publisher=[file|s3]**: Defines how the metrics will be published (default file)
- **file=profile.bin**: Defines the file which profiling data will be written to
- **s3region=region**: The region the S3 bucket exists in (required is publisher=s3)
- **s3bucket=bucket**: The bucket to publish to (required is publisher=s3)
- **s3key=key**: The S3 key to publish to (required is publisher=s3)

## Agent Argument Notes

- When using publisher=s3, the aws-java-sdk-s3 dependency must exist on the classpath as it is not bundled with JTrace. JTrace will use the attached IAM role or other default authentication mechanisms. JTrace does not provide a means to specify access/secret keys.

## Performance

We've tested this agent against our automated integration tests which include roughly 4.6 billion profiled method executions. When running the profiler on application servers there is almost no noticeable difference in performance.

- Integration Tests Without JTrace: **00:47:33**
- Integration Tests With JTrace: **01:19:31**
- Total Profiled Method Invocations: **4,687,578,950**
- Unique # Profiled Methods: **23,187**
- Average Profiler Overhead Per Method Invocation: **0.00041ms**
- JTrace Profile Filesize: **2.68MB**


## Contextual Profiling

JTrace has the ability to only record method execution metrics for executions which are part of a stack trace originating from one or more contexts specified when attaching the agent. When using contexts, the agent will profile and record metrics for all the included classes only when called by methods matching one of the specified context patterns. This allows for answering questions such as "What is causing all the slowdown in the methods of ClassXYZ?" This helps narrowing down profiling metrics in large applications when you have an idea of where you need to look.

## Logging

JTrace will detect and use an SL4J Logger if SL4J is on the classpath. Otherwise all logging data will be printed to System.out

## Viewing Results

Currently JTrace only provides a CLI interface for viewing profile results. (GUI to come!)

    java -jar jtrace-dist.jar load profile.bin [format [order [limit [filter[convertNanos]]]]]
    
- format: csv | screen
- order: Invocations | OwnTime | AverageOwnTime | TotalTime | AverageTotalTime | MethodSignature
- limit: Limits the number of results
- filter: ${FIELD}${OP}${VALUE} (ex: AverageOwnTime>100)
  -  Field Types: Invocations | OwnTime | AverageOwnTime | TotalTime | AverageTotalTime | MethodSignature
  -  Op Types: < (less than), > (greater than), = (equals), ~ (contains), ! (does not contain)
- convertNanos: true | false - Should nanoseconds be converted to milliseconds (easier to read)
    
Example (Top 5 results by # of Invocations, no filtering, nanoseconds converted to milliseconds)

    java -jar jtrace-dist-0.0.1.jar load profile.bin screen Invocations 5 '' true
    
    Invocations |     AverageOwnTime |            OwnTime |   AverageTotalTime |          TotalTime | MethodSignature
       40893367 |             0.0001 |          3671.7322 |             0.0001 |          3671.7322 | c.i.m.RefCode.getRefCodeCategory()
       38035129 |             0.0001 |          3440.2066 |             0.0001 |          3440.2066 | c.i.m.RefCode.getRefCodeDefinitions()
       37087297 |             0.0001 |          3898.7522 |             0.0001 |          3898.7522 | c.i.m.RefCode.getCode()
       37073567 |             0.0001 |          3342.6170 |             0.0001 |          3342.6170 | c.i.m.RefCode.getMappedTo()
       37071973 |             0.0001 |          3566.2844 |             0.0001 |          3566.2844 | c.i.m.RefCode.getCodeEndDate()
            
Exmaple (Top 10 by own time, with more than 1000 invocations and a MethodSignature which contains "DataValidator")

    java -jar jtrace-dist-0.0.1.jar load profile.bin screen OwnTime 10 'Invocations>1000,MethodSignature~DataValidator' true

    Invocations |     AverageOwnTime |            OwnTime |   AverageTotalTime |          TotalTime | MethodSignature
       15427379 |             0.0013 |         20573.9054 |             0.0093 |        143328.2064 | c.i.c.DataValidator.validateField(Field, Object, Object, boolean)
       24195290 |             0.0007 |         16464.3135 |             0.0077 |        187024.6030 | c.i.c.DataValidator.validateAnnotations(Object, boolean)
       15433181 |             0.0008 |         12948.9199 |             0.0008 |         12948.9199 | c.i.c.DataValidator.hasGetter(Field, Object)
       15429948 |             0.0008 |         12782.0728 |             0.0009 |         14397.1471 | c.i.c.DataValidator.getFieldValue(Field, Object)
       15427382 |             0.0001 |          2186.5695 |             0.0001 |          2186.5695 | c.i.c.DataValidator.validateObject(Field, Object, Object)
        9449439 |             0.0002 |          1769.0030 |             0.0092 |         87341.5478 | c.i.c.DataValidator.validateAnnotations(Object)
       15427382 |             0.0001 |          1692.7862 |             0.0001 |          1775.5290 | c.i.c.DataValidator.validateString(Field, Object, Object)
       15427379 |             0.0001 |          1689.3547 |             0.0001 |          1689.3547 | c.i.c.DataValidator.validateTimeString(Field, Object, Object)
       15427382 |             0.0001 |          1684.1494 |             0.0001 |          1684.1494 | c.i.c.DataValidator.validateInt(Field, Object, Object)
       15427382 |             0.0001 |          1673.7124 |             0.0001 |          1696.2530 | c.i.c.DataValidator.validateList(Field, Object, Object)

## Contributing / Development Environment

If you find a bug you can either open an issue, or fork the repository, fix it and open a new pull request. Please make sure 'mvn clean verify' builds successfully before opening a pull request.

- Eclipse Configuration
  - Project is eclipse-ready and can be imported into eclipse
  - Contains PMD rule sets and is configured to use the [Eclipse PMD Plugin](https://github.com/pmd/pmd-eclipse-plugin)
  - Contains Java code-style formatting rules
  - Enables save actions for source formatting and import organization
- Maven Configuration
  - Project contains both unit and integration tests
  - Jacoco is used for code coverage (minimum 100% coverage) (configured to merge unit and integration test coverage)
  - maven-pmd-plugin is used for static analysis (uses same rulset as Eclipse) - no violations allowed
  - Builds to a single jar file (jar-with-dependencies)

## Release Notes
#### 0.0.1 - Initial Release
- Initial beta release of JTrace