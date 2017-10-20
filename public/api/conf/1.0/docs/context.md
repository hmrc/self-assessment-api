Making Tax Digital introduces digital record keeping for most businesses, self-employed people and landlords. HMRC customers (and their agents) will use digital record keeping software to interact directly with our systems via the MTD APIs.

The MTD Self Assessment API allows software packages to supply business and personal financial data to HMRC, so taxpayers can fulfil their obligations and view their tax calculation.

### How it works 

* the taxpayer (or their agent) enters details of the business’s income and expenses into the software 
* the software updates HMRC via the API
* the API submits this information to a HMRC systems database, where it’s used to work out the customers liability and establish whether the taxpayer's obligations have been met
* the software can then make a request to the API which returns an up-to-date tax calculation and confirms obligations

### Unauthorised agents 

Unauthorised agents (also known as filing only agents) can submit a taxpayer’s data to HMRC, but aren’t authorised to access existing data. This means an unauthorised agent can’t use the API to access the data required to submit an update, so:
* read (i.e. GET) requests are rejected
* write (i.e. POST and PUT) requests are processed normally (some errors may be generalised to protect user information)

To enable an unauthorised agent to submit an update correctly, pre-existing business identifiers or pre-existing periodic update identifiers must be recorded on creation, or obtained from a source with access to the data (e.g. the taxpayer). 