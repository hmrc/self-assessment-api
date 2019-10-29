<hr>

## All changes after 22<sup>nd</sup> Feb 2019 can be found within the Wiki page located [here](https://github.com/hmrc/self-assessment-api/wiki/Changelog).

<hr>

## 6-Nov-2018
<b>Self Assessment - v0.207.0<br>
Router - v1.45.0<br>
Self Employment - v0.21.0<br>
Proeprty - v0.38.0</b>
<br>Version 1.0 endpoints released into Production
  * Get Self-Employment Business Income Source Summary (BISS)
    * /self-assessment/ni/{nino}/self-employments/{selfEmploymentId}/{taxYear}/income-summary
  * Get UK property Business Income Source Summary (BISS)
    * /self-assessment/ni/{nino}/uk-properties/{taxYear}/income-summary
    
Version 2.0 endpoints released into Production
  * Submit Self-Employment End-of-Period Statement
    * /self-assessment/ni/{nino}/self-employments/{selfEmploymentId}/end-of-period-statements/from/{start}/to/{end}
  * Retrieve End of Period Statement Obligations for a UK Property Business
    * /self-assessment/ni/{nino}/uk-properties/end-of-period-statements/obligations
  * Submit UK Property End-of-Period Statement
    * /self-assessment/ni/{nino}/uk-properties/end-of-period-statements/from/{start}/to/{end}
    
Please note v1.0 'Submit Self-employment End-of-Period Statement' and 'Submit UK Property End-of-Period Statement' will not be released into Production.
        
## 22-Oct-2018
<b>Self Assessment - v0.198.0<br>
Router - v1.37.0<br>
Tax Calc - v1.9.0</b>
<br>Version 1.0 endpoints released into Production
  * Retrieve End of Period Statement Obligations for a Self-Employment Business
    * /self-assessment/ni/{nino}/self-employments/{selfEmploymentId}/end-of-period-statements/obligations
  * Intent to crystallise
    * /self-assessment/ni/{nino}/{taxYear}/intent-to-crystallise
  * Crystallisation
    * /self-assessment/ni/{nino}/{taxYear}/crystallisation
  
## 17-Jul-2018
<b>Self Assessment - v0.141.13<br>
Router - v1.8.0<br>
Tax Calc - v0.11.0</b>
* Moved API documentation to self-assessment-api-router
* Includes versioning for self-assessment-api
* NEW endpoint Retrieve Tax Calculation (2.0)

## 0.141.4 - 21-Dec-2017
* Documentation updated to say, In production it can take up to an hour for the obligation to be updated

## 0.141.3 - 12-Dec-2017
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
* Temporarily disabled the test-only endpoint _Update a self-employment business_.
* Tax calculation response now includes EndOfYear estimates for each income source calculated based on the information provided
* Documentation improvements and fixes
  * Copy changes as per the User research
  * Fixed issue: Made id field in List all self-employment businesses mandatory. It was wrongly documented as optional
  * Updated the description of id field in List all self-employment businesses to An identifier for the self-employment business, unique to the customer.
  * The sample list periods response includes 2 elements for List all self-employment update periods, List all non-FHL UK property update periods, List all FHL UK property update periods
  * End-point name change: Update a non-FHL UK property business annual summary --> Amend a non-FHL UK property business annual summary
  * End-point name change: Update a FHL UK property business annual summary --> Amend a FHL UK property business annual summary
  * Fixed issue: Made id field as mandatory for List all self-employment update periods, List all non-FHL UK property update periods, List all FHL UK property update periods. It was wrongly documented as optional
  * All dates and taxYear fields are updated with format and example

## 0.136.2 - 21-Sep-2017

Fix application.raml to include the correct error scenarios for update period BVR's 

## 0.136.0 - 20-Sep-2017

* New errors responses are being returned after implementation of BVR's
  * MISALIGNED_PERIOD - period being submitted/created is not within the accounting period
  * NOT_CONTIGUOUS_PERIOD - period being submitted/created is not contiguous with the previous period
  * OVERLAPPING_PERIOD - period being submitted/created overlaps with any of the previously submitted periods
* Impacted API's
  * Create a self-employment update period
  * Create a non-FHL UK property update period
  * Create a FHL UK property update period
     
## 0.132.0 - 27-Jul-2017

* Enable the following APIs in production:
  * Self-employment business
     * Update a self-employment update period
  * UK property business
     * Update a non-FHL UK property update period
     * Update a FHL UK property update period

## 0.129.0 - 13-Jul-2017

* Enable the following APIs in production:
  * Self-employment business
     * Get a self-employment update period
  * UK property business
     * Get a non-FHL UK property update period
     * Get a FHL UK property update period
* Allow lower-case NINO's for all end-points
* Improve logging
* Fix issue with parsing list periods Json received from backend

## 0.126.0 - 03-Jul-2017

* Enable the following APIs in production:
  * Self-employment business
     * List all self-employment update periods
     * Get a self-employment annual summary
  * UK property business
     * List all non-FHL UK property update periods
     * List all FHL UK property update periods
     * Get a FHL UK property business annual summary
     * Update a FHL UK property business annual summary
     * Get a non-FHL UK property business annual summary
     * Update a non-FHL UK property business annual summary
* Tax Calculation: Changes to tax calculation result response to include missing fields (like NI and some proportional values)

## 0.121.0 - 14-Jun-2017

* No functional changes
* Enable UK-Property create 'update period' end-points for LIVE and update documentation accordingly
* Auditing improvements - identify the type of caller (Individual/Organisation or Agent)

## 0.117.3 - 01-Jun-2017

* No functional changes
* Enable Obligations end-points for LIVE and update documentation accordingly

## 0.117.2 - 31-May-2017

* Sandbox section added to documentation.
* Documentation updated for LIVE release. 
* Validation of monetary values changed to max=99999999999999.98

## 0.112.0 - 05-May-2017 (Only on Sandbox)

* Documentation updates:
  * Added a Technical Issues section explaining how to raise technical issues. Also includes link to GitHub issues and ChangeLog.
  * The documentation now clearly states that Tax Calculation has to be triggered in order for update data to be assessed for meeting obligations. 
* Added validation to fail null periodic submissions. i.e the following endpoints will fail if no incomes and expenses are supplied.
  * Create a self-employment update period
  * Update a self-employment update period
  * Create a non FHL UK property update period
  * Update a non FHL UK property update period
  * Create a FHL UK property update period
  * Update a FHL UK property update period
* Retrieve self-employment business obligations and Retrieve all UK property business obligations now return field 'due' which refers to date by which this obligation is due.
* Implement Access Controls
  * MTD subscription check
  * Fully Authorised Agent
  * Filing-Only Agent or UnAuthorised can only submit

## 0.100.1 - 06-Mar-2017

* Use UTF8 for reading SICs.txt file


## 0.99.0 - 02-Mar-2017

* Rename Liability end-points as Tax calculation
* Remove field 'accountingType' from UK Properties API's
* Documentation
  * Title and Overview updated to state this API is for Making Tax Digital (MTD)
  * New terminology section to explain obligations, periods and annual summary
  * Mark non-production end-points as test-only
* Ability to simulate responses for following scenarios when API calls are made by
  * a taxpayer who is not subscribed to MTD
  * an Agent who is not subscribed to Agent Services
  * an Agent who is subscribed to Agent Services but has not been authorised by the client on act on their behalf
    

## 0.85.0 - 24-Jan-2017

* Ability to provide UK interest received

## 0.82.0 - 19-Jan-2017

* API re-design
* Ability to provide HMRC periodic and annual information
     * Self-employment income/expense
     * Income/expense from UK property including Furnished Holiday Lettings 
* Ability to provide HMRC with annual information on customers income from
     * Income from dividends
* Removed Liability end-point (only temporarily)
* Removed Employment, UnEarned Income Source end-points (Not part of MVP)

## 0.61.0 - 10-Oct-2016

* Liability endpoints have been updated to support a single liability per tax year.
* Data Collection: Live implementation for sources
    * Self-Employment, 
    * Employment (Not enabled in Production)
    * UK Savings Interest (Part of UnEarned Income Source),
    * UK Dividends (Part of UnEarned Income Source),
    * Furnished Holiday Lettings and
    * UK Property
* Estimated Liability: Considers following sources
    * Self-Employment, 
    * Employment, (Not enabled in Production)
    * UK Savings Interest (Part of UnEarned Income Source),
    * UK Dividends (Part of UnEarned Income Source),
    * Furnished Holiday Lettings and
    * UK Property
* Documentation updated to include sub groups

## 0.38.0 - 29-June-2016

Sandbox implementation (all CRUD operations unless specified)

### Source
* UnEarned Income

### Summary
* Collect following summaries for source **UnEarned Income**
    * savings income
    * dividends
    * Pension, Annuities and State Benefits
    
### Tax Year (only GET and PUT)
* Pension Contributions
* Charitable Givings
* Blind Persons Allowance
* Tax Refunded Or Set Off
* Student Loan details
* Child Benefit details


### Other
* Remove name field from all sources to avoid identifiable information being provided
* Return full object representations in list responses

## 0.26.0 - 02-June-2016

Sandbox implementation (all CRUD operations unless specified)

### Source
* Furnished Holiday Lettings
* UK Property
* Employments

### Summary
* Collect following summaries for source **Furnished Holiday Lettings**
    * incomes
    * expenses
    * private-use-adjustments
    * balancing-charges
* Collect following summaries for source **UK Property**
    * incomes
    * expenses
    * taxes-paid
    * balancing-charges
    * private-use-adjustments
* Collect following summaries for source **Employments**
    * incomes
    * benefits
    * expenses
    * uk-taxes-paid

## 0.23.0 - 25-May-2016

Sandbox implementation to Collect Self Employment (includes all CRUD operations)

* Adjustments and Allowances (all CRUD operations)
* Balancing Charges (all CRUD operations)
* Goods and Services for own use (all CRUD operations)

## 0.21.0 - 19-May-2016

Simplified API Documentation

## 0.18.0 - 17-May-2016

Sandbox and Live implementation for end-points

* [Resolve Customer] (https://www.tax.service.gov.uk/api-documentation/docs/api/self-assessment-api/1.0/Resolve%20Customer)
* [Discover Tax Year] (https://www.tax.service.gov.uk/api-documentation/docs/api/self-assessment-api/1.0/Discover%20Tax%20Year)
* [Discover Tax Years] (https://www.tax.service.gov.uk/api-documentation/docs/api/self-assessment-api/1.0/Discover%20Tax%20Years)

Sandbox implementation to Collect Self Employment

* incomes (all CRUD operations)
* expenses (all CRUD operations)


