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
            <td><p>Simulates a calculation with self-employment periodic updates and annual summaries</p></td>
        </tr>
        <tr>
            <td><p>SE_PERIODIC_ANNUALS</p></td>
            <td><p>Simulates a calculation with self-employment periodic updates and annual summaries</p></td>
        </tr>      
        <tr>
            <td><p>SE_PERIODIC</p></td>
            <td><p>Simulates a calculation with self-employment periodic updates</p></td>
        </tr>
        <tr>
            <td><p>PROP_FHL_PERIODIC_ANNUALS</p></td>
            <td><p>Simulates a calculation with UK property FHL periodic updates and annual summaries</p></td>
        </tr>
        <tr>
            <td><p>SE_PROPERTY</p></td>
            <td><p>Simulates a calculation with self-employment and UK property income sources</p></td>
        </tr>
        <tr>
            <td><p>SE_SCOTTISH_MIS</p></td>
            <td><p>Simulates a calculation with a Scottish regime and multiple income sources</p></td>
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