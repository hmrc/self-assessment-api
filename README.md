# Self Assessment API Router
[![Build Status](https://travis-ci.org/hmrc/self-assessment-api-router.svg)](https://travis-ci.org/hmrc/self-assessment-api-router) [ ![Download](https://api.bintray.com/packages/hmrc/releases/self-assessment-api-router/images/download.svg) ](https://bintray.com/hmrc/releases/self-assessment-api-router/_latestVersion)

This API provides access to versioned resources of the [Self Assessment API](https://github.com/hmrc/self-assessment-api).

Access is granted by use of an `Accept` header defining the version number:

    Accept: application/vnd.hmrc.{version}+json    

## Running Locally

Install [Service Manager](https://github.com/hmrc/service-manager), if you want live endpoints, then start dependencies:

    sm --start MTDFB_SA

Start the app:

    sbt "run 9885" 
    

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")