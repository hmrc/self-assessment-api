From the end of July 2020, the following endpoints will no longer be available:

* trigger a tax calculation
* retrieve a tax calculation
* retrieve validation messages associated with a tax calculation

This is because they have been replaced with the [Individual Calculations API](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individual-calculations-api/1.0)

Use these resources to trigger a year-to-date tax calculation for a taxpayer. An update must be provided before a calculation is triggered. When a tax calculation is triggered, a check is made to determine if the taxpayer has met their obligations. To retrieve the status of the taxpayer’s updated obligations, use the obligation endpoints.

A tax calculation must be triggered to assess the update data against obligations. If a tax calculation is not triggered, relevant obligations are not determined even if the update data is valid and complete.
In the sandbox environment, this API returns pre-defined representative responses. These may include fields that are not yet possible to populate via the API.

Here, the developer can:

* trigger a tax calculation
* retrieve the result of a previously performed tax calculation
* retrieve the validation messages associated with a tax calculation

If the tax calculation is successfully triggered, the response _location_ header includes _calculationId_. Use the _calculationId_ to retrieve the tax calculation.

When retrieving a tax calculation returns the HTTP response code 204, the calculation is still in progress and needs to be retrieved again when the `etaSeconds` have elapsed.