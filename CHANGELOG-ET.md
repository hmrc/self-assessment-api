## [mtd-tax-calculation v0.5.0](https://github.com/hmrc/mtd-tax-calculation/releases/tag/v0.5.0) 4-Jul-2018
* Includes versioning for self-assessment-api
* NEW endpoint Retrieve Tax Calculation (2.0)

## [self-assessment-api v0.163.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.163.0) 25-Jun-2018
* Include Other Property Income in non-FHL Periodic endpoints

## [0.162.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.162.0) 14-Jun-2018
* NEW API to retrieve Self-Employment Business Income Sources summary (BISS)

## [0.160.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.160.0) 22-May-2018
* Removed 'rentARoomExempt' field from Non-FHL UK Property Annual Summary requests and responses.

## [0.158.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.158.0) 21-May-2018
### New Endpoints
#### UK Property BISS
* Get UK Property Business Income Sources summary
### Other changes
* Updated error handling

## [0.153.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.153.0) 28-Mar-2018
### New Endpoints
#### Charitable giving
* Get charitable giving tax relief
* Amend charitable giving tax relief
#### UK property business
* Submit UK Property End-of-Period Statement
### Other Differences include
* UK property endpoints have consolidated Expenses
* Add periodOfGraceAdjustment field to FHL Property Adjustments
* Amend accounting period start date cut off date from 05/04/2017 to 06/04/2017 for Self Employment EOPS
* Update documentation description for lossBroughtForward for Self-Employment, Properties (FHL, Other) both
* Add rentARoomExempt attribute to the Other FHL Annual Summary Allowances
* Add costOfServices field to Create/Amend a FHL UK property update period
* Remove BPRA from 'Get/Submit a non-FHL UK Property Business Annual Summary' API
* Separate adjustments schemas for FHL/other property annual summaries
* Add residential financial cost to Non-FHL UK properties
* Remove SIC Code validation

## [0.147.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.147.0) 25-Jan-2018
* New API to retrieve End of Period Statement Obligations for their Self-Employment Business
* New API to submit the Intent to Crystallise for a tax year
* New API to submit the final Crystallisation for a tax year
* Enhancement for existing API UK property business to display the accounting period for property business
* Enhancement for existing API Submit End-Of-Period Statement (EOPS) to include two additional validations

## [0.143.2](https://github.com/hmrc/self-assessment-api/releases/tag/v0.143.2) 21-Dec-2017

* Documentation updated to say, In production it can take up to an hour for the obligation to be updated

## [0.143.1](https://github.com/hmrc/self-assessment-api/releases/tag/v0.143.1) 14-Dec-2017

* New API to facilitate taxpayer can submit End-Of-Period Statement (EOPS) for their Self Employment business 
* Added new fields for allowances/adjustment types in Self Employment Annual Summary
 * Impacted API's
    * Update a Self employment Annual summary
    * Get a Self employment Annual summary
 * Added a new field in furnished-holiday-lettings business 
    * Impacted API's
        * Create a FHL UK property update period
        * Amend a FHL UK property periodic update
        * Get a FHL UK property update period
