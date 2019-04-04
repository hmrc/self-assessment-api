<p>You can simulate scenarios using Gov-Test-Scenario headers in the sandbox environment.<p>

<p>You can test this endpoint independently, in the sandbox environment, by using any calculationId that matches the correct regular expression. 
(You are not required to generate the calculationId using a previous endpoint.)</p>

<p>The values returned in the pre-defined responses may not follow business validation rules.</p>

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
            <td><p>Simulates a calculation with self-employment income</p></td>
        </tr>
        <tr>
            <td><p>SELF_EMPLOYMENT</p></td>
            <td><p>Simulates a calculation with self-employment income</p></td>
        </tr>
        <tr>
            <td><p>ALL_FIELDS_TEST_ONLY</p></td>
            <td><p>Simulates a calculation with all available data fields<br><br>Please note the values do not represent a realistic tax calculation and is intended to show all fields being populated</p></td>
        </tr>     
        <tr>
            <td><p>ERRORS_AND_WARNINGS</p></td>
            <td><p>Simulates a calculation with four BVR errors and one BVR warning</p></td>
        </tr>
        <tr>
            <td><p>RESPONSE_BEING_PREPARED</p></td>
            <td><p>Simulates the scenario where the calculation is in progress</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data can be found</p></td>
        </tr>
    </tbody>
</table>