# self-assessment-api

[![Build Status](https://travis-ci.org/hmrc/self-assessment-api.svg?branch=master)](https://travis-ci.org/hmrc/self-assessment-api) [ ![Download](https://api.bintray.com/packages/hmrc/releases/self-assessment-api/images/download.svg) ](https://bintray.com/hmrc/releases/self-assessment-api/_latestVersion)

This is the API project for self assessment

## Running Locally

Install Service Manager, if you want live endpoints, then start dependencies:

    sm --start MONGO
    sm --start AUTH -f
    sm --start DATASTREAM -f

Start the app:

    run -Drun.mode=Dev

Now you can test sandbox:

    UTR=2234567890
    curl -v http://localhost:9000/sandbox/$UTR -H 'Accept: application/vnd.hmrc.1.0+json'

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

### Coverage

[Scala Coverage Plugin](https://github.com/scoverage/sbt-scoverage)