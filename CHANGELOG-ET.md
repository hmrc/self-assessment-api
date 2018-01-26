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
