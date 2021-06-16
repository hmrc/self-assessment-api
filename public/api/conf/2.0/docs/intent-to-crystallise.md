The 'Intent to Crystallise' endpoint allows you to trigger a final calculation - to be used when you Crystallise.

A successful response returns a HTTP status code of 303 along with a `Location` header value redirecting you to the 'Retrieve a tax calculation' endpoint. Note that 'Retrieve a tax calculation v2.0' is no longer available.

Do not follow the redirect, instead use the `calculationId` from the `Location` header value, and use the new [Individual Tax Calculations](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individual-calculations-api/1.0) APIs.

From 30 November 2021 this resource will be replaced by the Intent to crystallise endpoint in the <a href="/api-documentation/docs/api/service/individual-calculations-api/2.0">individual-calculations-api</a>.