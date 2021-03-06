<hr>

## All changes after 22<sup>nd</sup> Feb 2019 can be found within the Wiki page located [here](https://github.com/hmrc/self-assessment-api/wiki/Changelog).

<hr>

## 22-Feb-2019
| Change    | Version  | Status                               | Endpoint                            |    Detail
| :------:  | :------: | :-----------------------------------:| :---------------------------------: | :---------
| Addition  | v2.0     | Testable in Sandbox                  | List all UK savings accounts        |  Added new version 2.0 'List all UK savings accounts' with the following: <ul> <li> Updated endpoint and field descriptions </li> <li> A new response body containing the savings accounts</li> <li> Updated error scenarios </li> <li> Updated Gov-Test-Scenario headers</ul>

## 20-Feb-2019
| Change    | Version  | Status                               | Endpoint                    |    Detail
| :------:  | :------: | :-----------------------------------:| :-------------------------: | :---------
| Addition  | v2.0     | Testable in Sandbox                  | Add a UK savings account    |  Added new version 2.0 'Add a UK savings account' with the following: <ul> <li> Updated endpoint and field descriptions </li> <li> A new response body containing the `id`</li> <li> Updated error scenarios </li> <li> Updated Gov-Test-Scenario headers</ul>

**Please Note**: Other savings accounts endpoints (retrieve, list all, annual summaries) will not work in version 2.0 until they have been versioned. They are currently using version 1.0 code, but will soon be updated. Update a savings account will not be available in version 2.0.

## 11-Feb-2019
| Change    | Version  | Status                               | Endpoint                    |    Detail
| :------:  | :------: | :-----------------------------------:| :-------------------------: | :---------
| Addition  | v2.0     | Testable in Sandbox                  | Retrieve Dividends Income   |  Added new version 2.0 Retrieve Dividends Income with the following: <ul> <li> `ukDividends` data field </li> <li> `otherUkDividends` data field </li> <li>   Error scenarios </li> <li> Gov-Test-Scenario headers</ul>


## 07-Feb-2019
| Change    | Version  | Status                               | Endpoint                |  Detail  
| :------:  | :------: | :-----------------------------------:| :----------------------:|:---------------
| Addition  | v2.0     | Testable in Sandbox                  | Amend Dividends Income  | Added new version 2.0 Retrieve Dividends Income with the following: <ul> <li> `ukDividends` data field </li> <li> `otherUkDividends` data field </li> <li> Additional example request </li> <li>   Error scenarios </li> <li> Gov-Test-Scenario headers</ul>


## 01-Feb-2019
| Change    | Version  | Status                               | Endpoint                         | Detail
| :------:  | :------: | :-----------------------------------:| :-------------------------------:|:---------------------
| Addition  | v2.0     | Documentation / Testable in Sandbox  | Amend/Retrieve Charitable Giving |  Added 2 decimal place detail
| Addition  | v2.0     | Documentation Only                   | Retrieve Tax Calculation         |  Added 2 decimal place detail

## 29-Jan-2019

| Change    | Version  | Status              |  Endpoint                   |  Detail
| :------:  | :------: | :-----------------: | :-------------------------: | :-----
| Addition  | v2.0     | Testable in Sandbox | Retrieve Charitable Giving  | New response body with multiple fields.  See API Documentation
| Addition  | v2.0     | Testable in Sandbox | Amend Charitable Giving     | New request body with multiple fields.  See API Documentation
| Addition  | v2.0     | Documentation Only  | Amend UK Dividends          | Documentation only - see API Documentation
| Addition  | v2.0     | Documentation Only  | Retrieve UK Dividends       | Documentation only - see API Documentation


## 24-Jan-2019
<b>Router - v1.122.0</b> <br/>
* Update to documentation - Amend charitable giving tax relief
    * Invalid Request examples
    * Errors Scenarios
  
<b>Charitable Giving - v0.32.0</b>
*  Unhappy path (error scenario) functionality added to version 2.0 Amend Charitable Giving

## 15-Jan-2019
<b>Router - v1.115.0</b> <br/>
* New documentation - ‘Retrieve charitable giving tax relief’
    * Additional data sets
        * Non UK Charity Names
        * Investments Non UK Charity Name

* New documentation - ‘Amend charitable giving tax relief’
    * Additional data sets
        * Non UK Charity Names
        * Investments Non UK Charity Names
        * Additional error scenarios
        * Added examples of how to remove previously submitted values within the JSON request.
  
<b>Charitable Giving - v0.19.0</b>
* NEW version 2.0 endpoint for Amend Charitable Giving in sandbox, ONLY happy path scenario is functional

## 21-Dec-2018
<b>Router - v1.95.0</b> <br/>
<b>Self Assessment - v0.236.0</b> <br/>
<b>Tax Calculation - v1.17.0</b>
* Remove unused fields and add Trading Allowance to Self-employment annual endpoint
* Added new field to Allowances model:
   * tradingAllowance
* Removed unused fields to Adjustments model:
   * overlapProfitCarriedForward
   * overlapProfitBroughtForward
   * lossCarriedForwardTotal
   * cisDeductionsTotal
   * taxDeductionsFromTradingIncome
   * class4NicProfitAdjustment
* Removal of "Retrieve a tax calculation" endpoint for version 1.0 of Self Assessment API

## 06-Dec-2018
<b>Router - 1.72.0</b>
<b>Self Assessment - v0.227.0</b>
* Documentation change to include Welsh rate of income tax for Retrieve Tax Calculation.
* Added new fields to FHL and non-FHL Property Annual Summary (Update, Amend)
   * businessPremisesRenovationAllowance
   * bpraBalancingCharges
   * propertyIncomeAllowance
   * nonResidentLandlord
   * rarjointLet
   
## 04-Dec-2018
<b>Self Assessment - v0.216.0<br>
<b>Router - 1.71.0</b>
* New optional fields added to UK Property FHL Periodic endpoints (Create, Amend, Retrieve) in Sandbox:
    * rarRentReceived
    * travelCosts
    * rarReliefClaimed
    
## 03-Dec-2018
<b>Self Assessment - v0.214.0<br>
<b>Router - 1.64.0</b>
* New optional fields added to UK Property Non-FHL Periodic endpoints (Create, Amend, Retrieve) in Sandbox:
    * rarRentReceived
    * travelCosts
    * broughtFwdResidentialFinancialCost
    * rarReliefClaimed

## 25-Oct-2018
<b>Self Employment - v0.14.0<br>
<b>Router - 1.40.0</b>
* NEW endpoint available in Sandbox: Submit Self-Employment EOPS Declaration (v2.0)

## 23-Oct-2018
<b>Property - v0.32.0<br>
<b>Router - 1.37.0</b>
* NEW endpoint available in Sandbox: Submit UK Property EOPS Declaration (v2.0)

## 11-Sep-2018
<b>Self Assessment - v0.167.0<br>
<b>Router - 1.20.0</b>
* Updated the Self-employment EOPS obligations by adding the validation for self-employment-id with new regex
* Updated the error scenarios, with additional SELF_EMPLOYMENT_ID_INVALID error

## 30-Aug-2018
<b>Tax Calc - v​1.2.0<br>
<b>Router - 1.19.0</b>
* Updated Retrieve Tax Calculation endpoint (2.0) to align with new backend specification
    * Min/max values previously (+/-) 999999999999.98 changed to (+/-) 99999999999.99
    * Test data for Gov-test-scenarios, including examples with errors/warnings
* NEW endpoint Retrieve validation messages associated with a tax calculation (2.0)

## 14-Aug-2018
<b>Self Assessment - v0.166.0<br>
<b>Router - 1.17.0</b>
* The following changes apply to endpoints:
    * Create a Self-employment Periodic update
    * Amend a Self-employment Periodic update
    * Get a Self-employment Periodic update
* Removed DEPRECIATION_DISALLOWABLE_AMOUNT error from Self-employment Period endpoints.
* Removed INVALID_DISALLOWABLE_AMOUNT error from Self-employment Period endpoints.
* Correct validation to allow value range -99999999999.99 to 99999999999.99, for following fields on Self-employment Period endpoints:
    * costOfGoods.amount
    * costOfGoods.disallowableAmount
    * premisesRunningCosts.amount
    * premisesRunningCosts.disallowableAmount
    * maintenanceCosts.amount
    * maintenanceCosts.disallowableAmount
    * interest.amount
    * interest.disallowableAmount
    * financialCharges.amount
    * financialCharges.disallowableAmount
    * badDebt.amount
    * badDebt.disallowableAmount
    * professionalFees.amount (Note: but not the disallowableAmount)
    * depreciation.amount
    * depreciation.disallowableAmount

## 01-Jul-2018
<b>Property - v0.6.0</b>
* Added GET UK Property EOPS obligations endpoint.

## 24-Jul-2018
<b>Self Assessment - v0.165.0<br>
<b>Router - 1.10.0</b>
* Removed INVALID_TAX_DEDUCTION_AMOUNT rule from all UK Property Period Create/Update endpoints.
* Documentation for the error marked as (Production only) for time being.

## Tax Calculation - v0.5.0 - 4-Jul-2018
* Includes versioning for self-assessment-api
* NEW endpoint Retrieve Tax Calculation (2.0)

## Self Assessment - v0.163.0 - 25-Jun-2018
* Include Other Property Income in non-FHL Periodic endpoints

## 0.162.0 -  14-Jun-2018
* NEW API to retrieve Self-Employment Business Income Sources summary (BISS)

## 0.160.0 - 22-May-2018
* Removed 'rentARoomExempt' field from Non-FHL UK Property Annual Summary requests and responses.

## 0.158.0 - 21-May-2018
### New Endpoints
#### UK Property BISS
* Get UK Property Business Income Sources summary
### Other changes
* Updated error handling

## 0.153.0 - 28-Mar-2018
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

## Self Assessment - 0.147.0 - 25-Jan-2018
* New API to retrieve End of Period Statement Obligations for their Self-Employment Business
* New API to submit the Intent to Crystallise for a tax year
* New API to submit the final Crystallisation for a tax year
* Enhancement for existing API UK property business to display the accounting period for property business
* Enhancement for existing API Submit End-Of-Period Statement (EOPS) to include two additional validations

## Self Assessment - 0.143.2 - 21-Dec-2017

* Documentation updated to say, In production it can take up to an hour for the obligation to be updated

## Self Assessment - 0.143.1 - 14-Dec-2017

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
