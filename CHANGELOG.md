
## [0.65.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.65.0) Release Candidate. Not Yet Released to Production.

* Removed 'Employments' source and its summaries

* Removed 'Unearned Income' source
   * 'savings' summary is now 'Banks' Income source. Change made based on feedback from TPV's.
   * 'dividends' summary now 'Dividends' Income source. Change made based on feedback from TPV's.
   * 'benefits' summary now 'Benefits' source

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


