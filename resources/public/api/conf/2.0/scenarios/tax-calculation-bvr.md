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
            <td><p>Simulates a calculation that does not have any associated errors or warnings</p></td>
        </tr>
        <tr>
            <td><p>ERRORS_AND_WARNINGS</p></td>
            <td><p>Simulates an unsuccessful calculation due to errors and warnings</p></td>
        </tr>
        <tr>
            <td><p>WARNINGS_ONLY</p></td>
            <td><p>Simulates a calculation where only warnings are returned</p></td>
        </tr>      
        <tr>
            <td><p>ERRORS_ONLY</p></td>
            <td><p>Simulates an unsuccessful calculation due to errors</p></td>
        </tr>
        <tr>
            <td><p>NO_MESSAGES</p></td>
            <td><p>Simulates a calculation that does not have any associated errors or warnings</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data can be found</p></td>
        </tr>
    </tbody>
</table>