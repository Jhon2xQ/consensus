package com.carmenio.consensus.common.constant;

/**
 * Represents the possible states of an electoral process.
 * <p>
 * All states are <strong>calculated in real-time</strong> from the process
 * dates by {@code ProcessStateCalculator.transitionState()}. No manual
 * overrides exist — the state machine is purely date-driven.
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
 * </table>
 */
public enum ProcessStatus {
    NONE,
    COMMITMENT,
    VOTING,
    CLOSED
}
