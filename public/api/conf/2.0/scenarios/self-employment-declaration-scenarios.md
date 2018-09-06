<p>You can simulate scenarios using Gov-Test-Scenario headers in the sandbox environment.</p>
<p>You can test this endpoint independently, in the sandbox environment, by using any selfEmploymentId that matches the correct regular expression. (You are not required to generate the selfEmploymentId using a previous endpoint.)<p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>Default (No header value)</p></td>
            <td><p>Simulates a successful submission.</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates a not found response.</p></td>
        </tr>
        <tr>
            <td><p>RULE_ALREADY_SUBMITTED</p></td>
            <td><p>Simulates an error where the declaration has already been submitted.</p></td>
        </tr>
        <tr>
            <td><p>RULE_EARLY_SUBMISSION</p></td>
            <td><p>Simulates an error where a submission is made before the end of your accounting period.</p></td>
        </tr>
        <tr>
            <td><p>RULE_LATE_SUBMISSION</p></td>
            <td><p>Simulates an error where the period to finalise has passed.</p></td>
        </tr>
        <tr>
            <td><p>RULE_MISMATCH_START_DATE</p></td>
            <td><p>Simulates an error where the start date does not align to accounting period start date.</p></td>
        </tr>
        <tr>
            <td><p>RULE_MISMATCH_END_DATE</p></td>
            <td><p>Simulates an error where the end date does not align to accounting period end date.</p></td>
        </tr>
        <tr>
            <td><p>RULE_CLASS4_OVER_16</p></td>
            <td><p>Simulates an error with a Class 4 exemption.</p></td>
        </tr>
        <tr>
            <td><p>RULE_CLASS4_PENSION_AGE</p></td>
            <td><p>Simulates an error with a Class 4 exemption.</p></td>
        </tr>
        <tr>
            <td><p>RULE_CONSOLIDATED_EXPENSES</p></td>
            <td><p>Simulates an error with consolidated expenses.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_ERRORS</p></td>
            <td><p>Simulates an error with multiple errors.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_BUSINESS_ERRORS</p></td>
            <td><p>Simulates an error with multiple business errors.</p></td>
        </tr>  
    </tbody>
</table>
