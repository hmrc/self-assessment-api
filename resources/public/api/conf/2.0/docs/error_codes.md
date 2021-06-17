Below are some example errors, for specific errors refer to the individual resources below 

Example error response returned if invalid values are present in the body of POST/PUT.
<pre class="snippet--block code_text">
{
  "code": "INVALID_REQUEST",
  "message": "Invalid request",
    "errors": [
    {
      "code": "INVALID_MONETARY_AMOUNT",
      "message": "amounts should be non-negative numbers with up to 2 decimal places",
      "path": "/allowances/annualInvestmentAllowance"
    },
    {
      "code": "INVALID_MONETARY_AMOUNT",
      "message": "amounts should be non-negative numbers with up to 2 decimal places",
      "path": "/adjustments/averagingAdjustment"
    }
  ]
}
</pre>


Example error response returned if multiple business rule errors are returned.
<pre class="snippet--block code_text">
{
  "code": "BUSINESS_ERROR",
  "message": "Business validation error",
  "errors": [
    {
      "code": "RULE_CLASS4_OVER_16",
      "message": "Class 4 exemption is not allowed because the individual's age is greater than or equal to 16 years old on the 6th April of the current tax year."
    },
    {
      "code": "RULE_CLASS4_PENSION_AGE",
      "message": "Class 4 exemption is not allowed because the individual's age is less than their State Pension age on the 6th April of the current tax year."
    }
  ]
}
</pre>

Example error response returned if a single business rule error is returned.
<pre class="snippet--block code_text">
{
  "code": "RULE_CLASS4_OVER_16",
  "message": "Class 4 exemption is not allowed because the individual's age is greater than or equal to 16 years old on the 6th April of the current tax year."
}
</pre>
