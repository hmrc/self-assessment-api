These resources allow software packages to trigger a year-to-date tax calculation for a taxpayer. A calculation may only be triggered when an update has been provided.
When a tax calculation is triggered, a check will also be performed to see whether or not the taxpayer has met their obligations. If you wish to retrieve the status of the taxpayers updated obligations, you can use the provided obligation APIs.

A tax calculation **must** be triggered in order for update data to be assessed for meeting obligations. If a tax calculation is not triggered, then relevant obligations will **not** be determined even if update data is valid and complete.

In test, this API will return pre-defined representative responses. These may include fields that are not yet possible
to populate via the API.

Here, the developer can:

* Trigger a tax calculation
* Retrieve the result of a previously triggered tax calculation

If trigger tax calculation succeeds, response _location_ header includes _calculationId_ which should be used to retrieve the tax calculation.

When retrieving a tax calculation returns the HTTP response code 204, the calculation is still in progress and needs to be retrieved again when 
the `etaSeconds` have elapsed.