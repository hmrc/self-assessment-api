The Making Tax Digital (MTD) Self Assessment API allows software packages to provide a taxpayer's financial data for 
their self-employment and UK property businesses, and other personal income, and to request and view their tax 
calculation. This data includes income, expenses, and allowances etc. for each business, as well as personal savings 
interest and dividends. 

To meet their obligations, taxpayers are required to submit regular financial updates for 
each of their businesses. Typically these updates cover four 3-month _obligation periods_, with one set of 
_obligation periods_ per business per accounting period. To meet the taxpayer's obligation for a particular _obligation period_
for a business:
* Supply summarised transactions for income and expenditure for the the whole of the _obligation period_ for that specific business
* Request a tax calculation

The data for each _obligation period_ can be provided either as a single _update period_, 
or as multiple smaller _update periods_, covering the whole period so long as there are no gaps, and no overlaps 
amongst the _update periods_.

More accurate tax calculations can be provided when businesses' allowances and adjustments, and personal savings 
and dividend income are also provided, though these details are not required to meet obligations.

### Unauthorised Agents (Filing Only Agents) ###

Where an agent is not authorised by the taxpayer to fully represent the taxpayer, then the agent is only allowed to 
send data to HMRC. These agents are sometimes referred to as Filing Only Agents, meaning that they can submit (file) taxpayer 
data to HMRC, but cannot retrieve existing data.

* Read (i.e. GET) requests are rejected
* Write (i.e. POST and PUT) requests are processed normally; however, certain errors may be generalised to protect user information.

The above restrictions mean it is not possible for an unauthorised agent to use the APIs to obtain certain data 
necessary to submit an update. For example, pre-existing business identifiers or pre-existing periodic update identifiers 
must be recorded on creation, or obtained from a source that does have access, e.g. the taxpayer.

### Technical Issues ###

Technical issues with the API may be raised via the Software Development Support Team (SDST). In addition to this,
the dev team also provides technical support via the API [Github](https://github.com/hmrc/self-assessment-api/issues) page. 
Please note that Github should be used for technical queries only.

### Changelog ###

The changelog can be found in API [Github](https://github.com/hmrc/self-assessment-api/blob/master/CHANGELOG.md) 
repository.

### Notes ###
* This API is usable only for taxpayers subscribed to Making Tax Digital (MTD) and only for tax years 2017/18 onwards.
* Self-employment and UK property businesses will be known to HMRC  as part of the MTD subscription process.
* Some APIs may be marked "test only"; this means that they are not available for use in production.

