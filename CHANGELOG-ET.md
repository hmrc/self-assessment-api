## [0.112.0] (https://github.com/hmrc/self-assessment-api/releases/tag/v0.112.0) 05-May-2017


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