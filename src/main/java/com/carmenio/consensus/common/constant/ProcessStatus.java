package com.carmenio.consensus.common.constant;

/**
 * Represents the possible states of an electoral process.
 * <p>
 * States are <strong>calculated in real-time</strong> based on the process dates,
 * not persisted in the database.
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
