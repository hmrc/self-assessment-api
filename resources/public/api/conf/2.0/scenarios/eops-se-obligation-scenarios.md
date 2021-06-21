<p>Scenario simulations using Gov-Test-Scenario headers is only available in sandbox environment.</p>
<p>You will need to add a self-employment business before making a request. This endpoint returns static responses.</p>
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
            <td><p>Returns a EOPs obligation for standard tax year dates with Open status</p></td>
        </tr>
        <tr>
            <td><p>EOPS_OPEN</p></td>
            <td><p>Returns a EOPs obligation for standard tax year dates with Open status</p></td>
        </tr>
        <tr>
            <td><p>EOPS_FULFILLED</p></td>
            <td><p>Returns a EOPs obligation for standard tax year dates with Fulfilled status</p></td>
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
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulate a not found response</p></td>
        </tr>
    </tbody>
</table>