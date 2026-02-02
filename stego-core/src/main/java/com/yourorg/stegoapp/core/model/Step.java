package com.yourorg.stegoapp.core.model;

/**
 * Legacy model representing a single stego/encryption step.
 * <p>
 * Newer code should prefer {@link StepConfig} with typed {@link StepOptions} to represent options
 * safely (e.g., {@link CryptoOptions}, {@link ZeroWidthOptions}).
 * </p>
 */
public class Step {
    private StepType type;
    private String password;

    /**
     * Default constructor.
     */
    public Step() {}

    /**
     * Constructs a Step with type and password.
     *
     * @param type     The step type
     * @param password The password (if required)
     */
    public Step(StepType type, String password) {
        this.type = type;
        this.password = password;
    }

    /**
     * Constructs a Step with type only.
     *
     * @param type The step type
     */
    public Step(StepType type) {
        this(type, null);
    }

    /**
     * Gets the step type.
     *
     * @return StepType
     */
    public StepType getType() {
        return type;
    }

    /**
     * Gets the password (if any).
     *
     * @return Password string or null
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the step type.
     *
     * @param type StepType
     */
    public void setType(StepType type) {
        this.type = type;
    }

    /**
     * Sets the password.
     *
     * @param password Password string
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
