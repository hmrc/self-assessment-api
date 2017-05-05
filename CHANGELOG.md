
### Please refer to [CHANGELOG-ET.md] (https://github.com/hmrc/self-assessment-api/blob/master/CHANGELOG-ET.md) file for any delta changes deployed to External Test Environment.

## [0.100.1] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.100.1) 06-Mar-2017

* Use UTF8 for reading SICs.txt file


## [0.99.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.99.0) 02-Mar-2017

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
    

## [0.85.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.85.0) 24-Jan-2017

* Ability to provide UK interest received

## [0.82.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.82.0) 19-Jan-2017

* API re-design
* Ability to provide HMRC periodic and annual information
     * Self-employment income/expense
     * Income/expense from UK property including Furnished Holiday Lettings 
* Ability to provide HMRC with annual information on customers income from
     * Income from dividends
* Removed Liability end-point (only temporarily)
* Removed Employment, UnEarned Income Source end-points (Not part of MVP)

## [0.61.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.61.0) 10-Oct-2016

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

## [0.38.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.38.0) 29-June-2016

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

## [0.26.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.26.0) 02-June-2016

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

## [0.23.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.23.0) 25-May-2016

Sandbox implementation to Collect Self Employment (includes all CRUD operations)

* Adjustments and Allowances (all CRUD operations)
* Balancing Charges (all CRUD operations)
* Goods and Services for own use (all CRUD operations)

## [0.21.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.21.0) 19-May-2016

Simplified API Documentation

## [0.18.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.18.0) 17-May-2016

Sandbox and Live implementation for end-points

* [Resolve Customer] (https://www.tax.service.gov.uk/api-documentation/docs/api/self-assessment-api/1.0/Resolve%20Customer)
* [Discover Tax Year] (https://www.tax.service.gov.uk/api-documentation/docs/api/self-assessment-api/1.0/Discover%20Tax%20Year)
* [Discover Tax Years] (https://www.tax.service.gov.uk/api-documentation/docs/api/self-assessment-api/1.0/Discover%20Tax%20Years)

Sandbox implementation to Collect Self Employment

* incomes (all CRUD operations)
* expenses (all CRUD operations)


