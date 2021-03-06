package edu.first.module.sensor;

import edu.first.module.Module;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.PIDSource;

/**
 * Module representing hall effect sensors that are triggered once-per-rev. To
 * get the current rate use {@link HallEffectModule#getRate()} When enabled, can
 * receive input from the sensor, but otherwise will always return 0.
 *
 * @author Team 4334
 */
public class HallEffectModule extends ForwardingHallEffectModule implements Module.DisableableModule {

    private boolean enabled;

    /**
     * Constructs the object by using composition, using the given digital input
     * object to control methods in this class.
     *
     * @param hallEffect actual underlying object used
     */
    public HallEffectModule(DigitalInput hallEffect) {
        super(hallEffect);
    }

    /**
     * Constructs the object by using composition, using the given digital input
     * object and a custom counter to control methods in this class.
     *
     * @param hallEffect actual underlying object used
     * @param counter the counter object it uses
     */
    public HallEffectModule(DigitalInput hallEffect, Counter counter) {
        super(hallEffect, counter);
    }

    /**
     * Disables the module. This prevents the class from returning values.
     * Additionally, it stops and resets the counter.
     *
     * @return if module was successfully disabled
     */
    public final boolean disable() {
        stop();
        return !(enabled = false);
    }

    /**
     * Enables the module and starts counter. Allows the class to function
     * properly.
     *
     * @return if module was successfully enabled
     */
    public final boolean enable() {
        start();
        return (enabled = true);
    }

    /**
     * Returns whether or not the module has been enabled yet. If it is not
     * enabled, the methods of this class will not function. (will always return
     * false or 0)
     *
     * @return whether module is enabled
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * If the module is enabled, returns the current polarity of the sensor.
     *
     * @return the current polarization of the sensor
     */
    public final boolean isPolarized() {
        return isEnabled() ? super.isPolarized() : false;
    }

    /**
     * If the module is enabled, returns the current count from {@link Counter}.
     *
     * @return the current count from the Counter
     */
    public final int getCount() {
        return isEnabled() ? super.getCount() : 0;
    }

    /**
     * If the module is enabled, returns the current rate of the counter in
     * rotations per minute.
     *
     * @return the rate of sensor
     */
    public final double getRate() {
        return isEnabled() ? super.getRate() : 0;
    }
}

/**
 * Forwarding class, as described in Effective Java: Second Edition, Item 16.
 * Forwards
 * {@link edu.wpi.first.wpilibj.DigitalInput} & {@link edu.wpi.first.wpilibj.Counter}.
 *
 * @author Denis Trailin
 */
class ForwardingHallEffectModule implements HallEffect, PIDSource {

    public static final double defaultMaxPossible = 50000;
    private final DigitalInput hallEffect;
    private final Counter counter;
    private final double maxPossible;
    private double prev;

    /**
     * Constructs the object by using composition, using the given digital input
     * object to control methods in this class.
     *
     * @param hallEffect underlying hallEffect object used
     */
    ForwardingHallEffectModule(DigitalInput hallEffect) {
        this(hallEffect, defaultMaxPossible);
    }

    /**
     * Constructs the object by using composition, using the given digital input
     * object to control methods in this class.
     *
     * @param hallEffect actual underlying object used
     * @param maxPossible maximum acceptable value to allow getRate() to return
     */
    ForwardingHallEffectModule(DigitalInput hallEffect, double maxPossible) {
        this(hallEffect, new Counter(hallEffect), maxPossible);
    }

    /**
     * Constructs the object by using composition, using the given digital input
     * object and a custom counter to control methods in this class.
     *
     * @param hallEffect actual underlying object used
     * @param counter the counter object it uses
     */
    ForwardingHallEffectModule(DigitalInput hallEffect, Counter counter) {
        this(hallEffect, counter, defaultMaxPossible);
    }

    /**
     * Constructs the object by using composition, using the given digital input
     * object and a custom counter to control methods in this class.
     *
     * @param hallEffect actual underlying object used
     * @param counter the counter object it uses
     * @param maxPossible maximum acceptable value to allow getRate() to return
     */
    ForwardingHallEffectModule(DigitalInput hallEffect, Counter counter, double maxPossible) {
        this.hallEffect = hallEffect;
        this.counter = counter;
        this.maxPossible = maxPossible;
    }

    /**
     * Starts the the counter.
     */
    protected final void start() {
        counter.start();
    }

    /**
     * Stops and resets the counter.
     */
    protected final void stop() {
        counter.stop();
        counter.reset();
    }

    /**
     * Returns the current count of pulses.
     *
     * @return current count of pulses
     */
    public int getCount() {
        return counter.get();
    }

    /**
     * Returns the counter rate in rotations per minute.
     *
     * @return current counter rate
     */
    public double getRate() {
        double rate = 60 / counter.getPeriod();
        return (rate > maxPossible) ? prev : (prev = rate);
    }

    /**
     * Returns boolean polarity of the hall effect sensor.
     *
     * @return the hall effect sensor polarity
     */
    public boolean isPolarized() {
        return hallEffect.get();
    }

    /**
     * {@inheritDoc}
     */
    public double pidGet() {
        return getRate();
    }

    public double get() {
        return getRate();
    }
}