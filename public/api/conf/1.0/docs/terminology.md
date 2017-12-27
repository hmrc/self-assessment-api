### Obligations ###

The _obligations_ are defined as a set of date periods for which a taxpayer must provide summary income and expense 
data. Each obligation has a start date and an end date which together define the _obligation period_. For MTD, each 
business has multiple obligations which are based on its accounting period. 

Please note – when calling the _retrieve obligations_ (Self Employment or Property) following an update to check whether a
customer's obligation has been met. It can take up to an hour for the _obligation_ to be updated within HMRC systems,
so it is best to wait for an hour before calling the endpoint for an up to date picture.

### Update period ###
A period of time within an obligation, for which the taxpayer can submit summarised income and expense data. This can cover anything from a day to the duration of the whole obligation period - so data can be provided as a single update covering the whole period, or as multiple smaller updates.

### Annual summary ###
An _annual summary_ is defined as a set of summary data for a tax year, containing allowances and adjustments broken down by category. 