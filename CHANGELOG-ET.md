
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