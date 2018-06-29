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
            <td><p>N/A - Default</p></td>
            <td><p>Simulates a successful calculation</p></td>
        </tr>
        <tr>
            <td><p>TAX_CALCULATION</p></td>
            <td><p>Simulates a successful calculation</p></td>
        </tr>
        <tr>
            <td><p>TAX_CALCULATION_WITH_WARNINGS</p></td>
            <td><p>Simulates a successful calculation, with warnings</p></td>
        </tr>
        <tr>
            <td><p>BVR_ONLY_CALCULATION</p></td>
            <td><p>Simulates an unsuccessful calculation, due to business validation failure(s)</p></td>
        </tr> 
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data can be found</p></td>
        </tr>
        <tr>
            <td><p>AGENT_NOT_SUBSCRIBED</p></td>
            <td><p>Simulates the scenario where the agent is not subscribed to Agent Services</p></td>
        </tr>
        <tr>
            <td><p>AGENT_NOT_AUTHORIZED</p></td>
            <td><p>Simulates the scenario where the agent is not authorized by the client to act on their behalf</p></td>
        </tr>
        <tr>
            <td><p>CLIENT_NOT_SUBSCRIBED</p></td>
            <td><p>Simulates the scenario where the client is not subscribed to Making Tax Digital</p></td>
        </tr>
    </tbody>
</table>