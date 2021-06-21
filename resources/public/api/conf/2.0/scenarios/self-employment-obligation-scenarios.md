<p>You can simulate scenarios using Gov-Test-Scenario headers in the sandbox environment.</p>
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
            <td><p>N/A - Default</p></td>
            <td><p>Self employment business accounting period greater than 6 months, obligation periods are calculated based on the accounting period dates. Only 1st obligation is met</p></td>
        </tr>
        <tr>
            <td><p>NONE_MET</p></td>
            <td><p>Self employment business 4 standard tax year obligations. None of the obligations are met</p></td>
        </tr>
        <tr>
        <tr>
            <td><p>ALL_MET</p></td>
            <td><p>Self employment business 4 standard tax year obligations. All the obligations are met</p></td>
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