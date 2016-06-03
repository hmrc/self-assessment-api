# Self-Assessment API

[![Build Status](https://travis-ci.org/hmrc/self-assessment-api.svg?branch=master)](https://travis-ci.org/hmrc/self-assessment-api) [ ![Download](https://api.bintray.com/packages/hmrc/releases/self-assessment-api/images/download.svg) ](https://bintray.com/hmrc/releases/self-assessment-api/_latestVersion)

This REST API allows clients to post information related to a taxpayer, and then get an estimated tax liability calculation for a tax period.

A typical workflow would be:

1. Authenticate.
1. Access a self-assessment resource (this is implicitly created).
1. Send details of various income **sources** (e.g. employment or property).
1. Send details of various types of **summary** (e.g. income or expenses) for each source.
1. Request a liability calculation.
1. Wait for calculation to complete (only a short time).
1. Request calculation and display it to your user.

All end points are User Restricted (see [authorisation](https://developer.service.hmrc.gov.uk/api-documentation/docs/authorisation)). Versioning, data formats etc follow the API Platform standards (see [the reference guide](https://developer.service.hmrc.gov.uk/api-documentation/docs/reference-guide)).

The API makes use of HATEOAS/HAL resource links. Your application does not need to store a catalogue of all URLs.

Sources and summaries will be given IDs. Do not expect these to be GUIDs. They will only be unique within context. I.e. source IDs will be unique for that type of source and within that self-assessment. Similarly, summary IDs will be unique for that type of summary within its source.

You will need to store these IDs within your application for disambiguation and reconciliation.

Consider:

* We do not expect or want you to submit transaction level information (if fact, we may limit the amount of data you can send). How will you aggregate this information within your software?
* How does your application deal with lost HTTP requests or responses?
* How do you reconcile your data with our data?
* The API doesn't expose meta-data or audit information (e.g. created or update times, who made the change). Do you need to record this audit information yourself?
* Your application may not be the only application access and modifying a taxpayer's data. How would you differentiate different data from your application and others?

You can dive deeper into the documentation in the [API Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api#self-assessment-api).

## Running Locally

Install [Service Manager](https://github.com/hmrc/service-manager), if you want live endpoints, then start dependencies:

    sm --start MONGO
    sm --start AUTH -f
    sm --start DATASTREAM -f

Start the app:

    run -Drun.mode=Dev

Now you can test sandbox:

    UTR=2234567890 ;# or UTR=1234567895
    curl -v http://localhost:9000/sandbox/$UTR -H 'Accept: application/vnd.hmrc.1.0+json'

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
