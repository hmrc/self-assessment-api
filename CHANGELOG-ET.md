
## [0.140.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.140.0) 19-Oct-2017

* JSON schema rollback for breaking changes which were introduced in 0.138.0
  * List all self employment businesses
  * Add a self employment business
  * Get a self employment business
* Documentation improvements

## [0.138.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.138.0) 05-Oct-2017

* 3LA - Taxpayer can submit/amend consolidated expenses for Self employment
  * Impacted API's
    * Create a Self Employment Update Period
    * Update a Self Employment Update Period
    * Get a Self Employment Update Period
* Class4 NIC’s exemptions and Class2 NIC’s Voluntary payment. 
  * Taxpayer can update HMRC if they are exempt from Class4 NIC's payments
  * Taxpayer can update HMRC if they want to make voluntary Class2 NIC payments
  * Impacted API's
    * Update a Self employment Annual summary
    * Get a Self employment Annual summary
* Temporarily disabled the test-only endpoint _Update a self-employment business_. Awaiting clear steer from business. 


# UNRELEASED TAGS

## [0.141.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.141.0) UNRELEASED

* MTDSA-1149: Documentation improvements
  * Copy changes as per the User research
  * Fixed issue: Made id field in List all self-employment businesses mandatory. It was wrongly documented as optional
  * Updated the description of id field in List all self-employment businesses to An identifier for the self-employment business, unique to the customer.
  * The sample list periods response includes 2 elements for List all self-employment update periods, List all non-FHL UK property update periods, List all FHL UK property update periods
  * End-point name change: Update a non-FHL UK property business annual summary --> Amend a non-FHL UK property business annual summary
  * End-point name change: Update a FHL UK property business annual summary --> Amend a FHL UK property business annual summary
  * Fixed issue: Made id field as mandatory for List all self-employment update periods, List all non-FHL UK property update periods, List all FHL UK property update periods. It was wrongly documented as optional
  * All dates and taxYear fields are updated with format and example
  
## [0.141.1](https://github.com/hmrc/self-assessment-api/releases/tag/v0.141.1) UNRELEASED

* MTDSA-1227: Adding End-Of-Year Estimate to tax calculation response