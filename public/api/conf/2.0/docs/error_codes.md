Below are some example errors, for specific errors refer to the individual resources below 


HTTP status : 400

Example error responses returned if invalid values are present in the url. 
<pre class="snippet--block code_text">
{
  "code": "NINO_INVALID",
  "message": "The provided Nino is invalid"
}
</pre>

<pre class="snippet--block code_text">
{
  "code": "TAX_YEAR_INVALID",
  "message": "Tax year invalid"
}
</pre>

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



HTTP status : 403
<pre class="snippet--block code_text">
{
  "code": "BUSINESS_ERROR",
  "message": "Business validation error",
  "errors": [
    {
      "code": "TOO_MANY_SOURCES",
      "message": "The maximum number of Self-Employment incomes sources is 1",
      "path": ""
    }
  ]
}
</pre>


