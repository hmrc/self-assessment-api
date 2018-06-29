Use these resources to trigger a year-to-date tax calculation for a taxpayer. An update must be provided before a calculation is triggered. When a tax calculation is triggered, a check is made to determine if the taxpayer has met their obligations. To retrieve the status of the taxpayer’s updated obligations, use the obligation endpoints.

A tax calculation must be triggered to assess the update data against obligations. If a tax calculation is not triggered, relevant obligations are not determined even if the update data is valid and complete.
In the sandbox environment, this API returns pre-defined representative responses. These may include fields that are not yet possible to populate via the API.

Here, the developer can:

* trigger a tax calculation
* retrieve the result of a previously performed tax calculation

If the tax calculation is successfully triggered, the response location header includes calculationId. Use the calculationId to retrieve the tax calculation.