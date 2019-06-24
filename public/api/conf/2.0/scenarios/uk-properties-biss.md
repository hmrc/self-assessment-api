<p>You can simulate scenarios using Gov-Test-Scenario headers in the sandbox environment.</p>
<p>You will need to add a UK property business before making a request. This endpoint returns static responses.</p>
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
            <td><p>Simulates the scenario where the client has a net profit.</p></td>
        </tr>
        <tr>
            <td><p>NET_PROFIT</p></td>
            <td><p>Simulates the scenario where the client has a net profit.</p></td>
        </tr>
        <tr>
            <td><p>NET_LOSS</p></td>
            <td><p>Simulates the scenario where the client has a net loss.</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data is found.</p></td>
        </tr>
        <tr>
            <td><p>AGENT_NOT_SUBSCRIBED</p></td>
            <td><p>Simulate agent subscription check failure.</p></td>
        </tr>
        <tr>
            <td><p>AGENT_NOT_AUTHORIZED</p></td>
            <td><p>Simulate agent is not authorized by the client to act on their behalf.</p></td>
        </tr>
        <tr>
            <td><p>CLIENT_NOT_SUBSCRIBED</p></td>
            <td><p>Simulate client subscription to MTD check failure.</p></td>
        </tr>
    </tbody>
</table>