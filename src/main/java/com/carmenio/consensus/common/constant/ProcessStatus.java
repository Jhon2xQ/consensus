package com.carmenio.consensus.common.constant;

/**
 * Represents the possible states of an electoral process.
 * <p>
 * NONE, COMMITMENT, VOTING, and CLOSED are <strong>calculated in real-time</strong>
 * from the process dates and managed by {@code ProcessStateCalculator.transitionState()}
 * on write operations. PAUSED and CANCELLED are manual lock states that block
 * auto-transition and must be explicitly set via the update endpoint.
 *
 * <pre>
 * NONE → COMMITMENT → NONE → VOTING → NONE → CLOSED
 * </pre>
 *
 * <table>
 *   <caption>State transitions</caption>
 *   <tr><th>State</th><th>Condition</th></tr>
 *   <tr><td>NONE</td><td>{@code now < commitmentStart} OR
 *       {@code commitmentEnd < now < votingStart} OR
 *       {@code votingEnd < now < results}</td></tr>
 *   <tr><td>COMMITMENT</td><td>{@code commitmentStart ≤ now ≤ commitmentEnd}</td></tr>
 *   <tr><td>VOTING</td><td>{@code votingStart ≤ now ≤ votingEnd}</td></tr>
 *   <tr><td>CLOSED</td><td>{@code results ≤ now}</td></tr>
 *   <tr><td>PAUSED</td><td>Explicit lock — temporary halt, blocks auto-transition, enrollment and results</td></tr>
 *   <tr><td>CANCELLED</td><td>Explicit lock — irreversible cancellation, blocks all operations</td></tr>
 * </table>
 */
public enum ProcessStatus {
    NONE,
    COMMITMENT,
    VOTING,
    CLOSED,
    PAUSED,
    CANCELLED
}
