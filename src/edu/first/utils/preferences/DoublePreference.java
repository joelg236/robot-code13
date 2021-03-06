package edu.first.utils.preferences;

import edu.first.identifiers.ReturnableNumber;
import edu.first.identifiers.SetteableNumber;
import edu.first.utils.Logger;

/**
 * Preference that holds a double value.
 *
 * @author Joel Gallant
 */
public final class DoublePreference extends Preference implements SetteableNumber, ReturnableNumber {

    private final double defaultValue;

    /**
     * Constructs the preference with its key used to access it in
     * {@link Preferences}.
     *
     * @param key string to access preference
     * @param defaultValue value to set if none exist
     */
    public DoublePreference(String key, double defaultValue) {
        super(key);
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the value of the preference. Will not save over reboot.
     *
     * @param value new value of preference
     */
    public void set(double value) {
        Logger.log(Logger.Urgency.LOG, "Setting " + getKey() + " to " + value);
        PREFERENCES.putDouble(getKey(), value);
    }

    /**
     * Returns the current value of the preference.
     *
     * @return value of preference
     */
    public double get() {
        return PREFERENCES.getDouble(getKey(), defaultValue);
    }

    /**
     * If the preference does not exist ({@link Preference#exists()}), creates
     * it with the default value.
     *
     */
    public void create() {
        if (!exists()) {
            set(defaultValue);
        }
    }
}
