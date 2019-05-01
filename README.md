# Self Assessment API Router
[![Build Status](https://travis-ci.org/hmrc/self-assessment-api-router.svg)](https://travis-ci.org/hmrc/self-assessment-api-router) [ ![Download](https://api.bintray.com/packages/hmrc/releases/self-assessment-api-router/images/download.svg) ](https://bintray.com/hmrc/releases/self-assessment-api-router/_latestVersion)

This API provides access to versioned resources of the [Self Assessment API](https://github.com/hmrc/self-assessment-api).

Access is granted by use of an `Accept` header defining the version number:

    Accept: application/vnd.hmrc.{version}+json    

## Running Locally

Install [Service Manager](https://github.com/hmrc/service-manager), if you want live endpoints, then start dependencies:

    sm --start MTDFB_SA

Start the app:

    sbt "~run 9665" 

## Checking your project

    The sbt plugin dependencyUpdates, will traverse your dependency versions and report on updates to the 
    version if they exist.

    sbt dependencyUpdates

    will produce output in the console, that might look something like ...
    
    [info] Found 14 dependency updates for self-assessment-api-router
    [info]   com.typesafe.play:play-logback                       : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:play-netty-server                  : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:play-omnidoc:docs                  : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:play-server                        : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:play-test:test                     : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:play-test:test                     : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:play-ws                            : 2.5.12         -> 2.5.19         -> 2.6.20         
    [info]   com.typesafe.play:twirl-api                          : 1.1.1                            -> 1.3.15         
    [info]   org.scala-lang:scala-library                         : 2.11.12                          -> 2.12.7         
    [info]   org.scalatestplus.play:scalatestplus-play:test       : 2.0.1                            -> 3.1.2
    [info]   org.scoverage:scalac-scoverage-plugin_2.11:provided  : 1.1.1                            -> 1.3.1          
    [info]   org.scoverage:scalac-scoverage-runtime:test          : 1.2.0                            -> 1.3.1          
    [info]   org.scoverage:scalac-scoverage-runtime_2.11:provided : 1.1.1                            -> 1.3.1          
    [info]   uk.gov.hmrc:auth-client                              : 2.17.0-play-25 -> 2.17.0-play-26                   
    [success] Total time: 10 s, completed 29-Nov-2018 13:50:53
    
    Using the command 
    
    sbt dependencyUpdatesReport 
    
    will write the same output to the file dependency-updates.txt in the <project>/target folder. 
    
    The sbt plugin dependencyCheck, will traverse your dependencies checking them against several 
    vulnerability databases.
    
    sbt dependencyCheck
    
    will generate the html report in <project>/target/scala-2.??/dependency-check-report.html

    Note: the first time you try this, it may take seeral minutes to download all the databases.
    
    Prior to creating a pull request, it is useful to run several commands against the project, and check 
    the results, making any changes that might be necessary. E.g.

    sbt clean test it:test coverage dependencyUpdatesReport dependencyCheck

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
