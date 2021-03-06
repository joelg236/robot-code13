package edu.first.module.driving;

import edu.first.identifiers.Function;
import edu.first.module.Module;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.RobotDrive;

/**
 * Module designed to drive robots. Has all of the benefits of
 * {@link edu.wpi.first.wpilibj.RobotDrive}, as well as {@link RobotDrive}. When
 * it is not enabled, this class will not move the robot. (the max speed is set
 * to 0)
 *
 * @author Joel Gallant
 */
public class RobotDriveModule extends ForwardingRobotDrive implements Module.DisableableModule {

    private boolean enabled;
    private double maxSpeed = 1;

    /**
     * Constructs the object by using composition, using the given robot drive
     * object to control methods in this class.
     *
     * @param drive actual underlying class used
     */
    public RobotDriveModule(RobotDrive drive) {
        this(drive, false, false);
    }

    /**
     * Constructs the object by using composition, using the given robot drive
     * object to control methods in this class.
     *
     * @param drive actual underlying class used
     * @param reverseSpeed if class should be reversed speed
     * @param reverseTurn if class should be reversed turn
     */
    public RobotDriveModule(RobotDrive drive, boolean reverseSpeed, boolean reverseTurn) {
        super(drive, reverseSpeed, reverseTurn);
    }

    /**
     * Enables the robot drive to drive normally. Sets the max speed back to
     * what it was before disabling. (default is 1 - this should usually not be
     * changed)
     *
     * @return returns whether it successfully enabled and is ready to work
     */
    public final boolean enable() {
        setMaxOutput(maxSpeed);
        setSafetyEnabled(true);
        return (enabled = true);
    }

    /**
     * Returns whether or not the object can move motors successfully.
     *
     * @return if module is enabled
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Disables the module. This effectively stops all of the motors from
     * running.
     *
     * @return whether the module successfully disabled
     */
    public final boolean disable() {
        super.setMaxOutput(0);
        stopMotors();
        setSafetyEnabled(false);
        return !(enabled = false);
    }

    /**
     * {@inheritDoc}
     */
    public final void setMaxOutput(double maxOutput) {
        super.setMaxOutput(maxSpeed = maxOutput);
    }
}

/**
 * Forwarding class, as described in Effective Java: Second Edition, Item 16.
 * Forwards {@link edu.wpi.first.wpilibj.RobotDrive}.
 *
 * @author Joel Gallant
 */
class ForwardingRobotDrive implements edu.first.module.driving.RobotDrive {

    private final edu.wpi.first.wpilibj.RobotDrive drive;
    private Function function = new Function.DefaultFunction();
    private final boolean reverseSpeed, reverseTurn;

    /**
     * Constructs the object by using composition, using the given robot drive
     * object to control methods in this class.
     *
     * @param drive actual underlying object used
     */
    ForwardingRobotDrive(edu.wpi.first.wpilibj.RobotDrive drive, boolean reverseSpeed, boolean reverseTurn) {
        if (drive == null) {
            throw new NullPointerException();
        }
        this.drive = drive;
        this.reverseSpeed = reverseSpeed;
        this.reverseTurn = reverseTurn;
    }

    ForwardingRobotDrive(edu.wpi.first.wpilibj.RobotDrive drive, boolean reverseSpeed, boolean reverseTurn, Function function) {
        if (drive == null) {
            throw new NullPointerException();
        }
        this.drive = drive;
        this.reverseSpeed = reverseSpeed;
        this.reverseTurn = reverseTurn;
        this.function = function;
    }

    /**
     * Returns the instance of the underlying
     * {@link edu.wpi.first.wpilibj.RobotDrive}.
     *
     * @return composition object under this one
     */
    protected final edu.wpi.first.wpilibj.RobotDrive getDrive() {
        return drive;
    }

    /**
     * Drive the motors at "speed" and "curve".
     *
     * <p> The speed and curve are -1.0 to +1.0 values where 0.0 represents
     * stopped and not turning. The algorithm for adding in the direction
     * attempts to provide a constant turn radius for differing speeds.
     *
     * <p> This function will most likely be used in an autonomous routine.
     *
     * @param outputMagnitude the forward component of the output magnitude to
     * send to the motors.
     * @param curve the rate of turn, constant for different forward speeds.
     */
    public final void drive(double outputMagnitude, double curve) {
        if (reverseSpeed) {
            outputMagnitude = -outputMagnitude;
        }
        if (reverseTurn) {
            curve = -curve;
        }
        drive.drive(function.apply(outputMagnitude), curve);
    }

    /**
     * Provide tank steering using the stored robot configuration. This function
     * lets you directly provide joystick values from any source.
     *
     * @param leftValue the value of the left stick.
     * @param rightValue the value of the right stick.
     * @param squaredInputs setting this parameter to true decreases the
     * sensitivity at lower speeds
     */
    public final void tankDrive(double leftValue, double rightValue, boolean squaredInputs) {
        if (reverseSpeed) {
            leftValue = -leftValue;
            rightValue = -rightValue;
        }
        drive.tankDrive(function.apply(leftValue), function.apply(rightValue), squaredInputs);
    }

    /**
     * Provide tank steering using the stored robot configuration. This function
     * lets you directly provide joystick values from any source.
     *
     * @param leftValue the value of the left stick.
     * @param rightValue the value of the right stick.
     */
    public final void tankDrive(double leftValue, double rightValue) {
        tankDrive(leftValue, rightValue, false);
    }

    /**
     * Arcade drive implements single stick driving. This function lets you
     * directly provide joystick values from any source.
     *
     * @param moveValue the value to use for forwards/backwards
     * @param rotateValue the value to use for the rotate right/left
     * @param squaredInputs if set, decreases the sensitivity at low speeds
     */
    public final void arcadeDrive(double moveValue, double rotateValue, boolean squaredInputs) {
        if (reverseSpeed) {
            moveValue = -moveValue;
        }
        if (reverseTurn) {
            rotateValue = -rotateValue;
        }

        moveValue = function.apply(moveValue);
        drive.arcadeDrive(moveValue, rotateValue, squaredInputs);
    }

    /**
     * Arcade drive implements single stick driving. This function lets you
     * directly provide joystick values from any source.
     *
     * @param moveValue the value to use for forwards/backwards
     * @param rotateValue the value to use for the rotate right/left
     */
    public final void arcadeDrive(double moveValue, double rotateValue) {
        arcadeDrive(moveValue, rotateValue, false);
    }

    /**
     * A method for driving with Mecanum wheeled robots. There are 4 wheels on
     * the robot, arranged so that the front and back wheels are toed in 45
     * degrees. When looking at the wheels from the top, the roller axles should
     * form an X across the robot.
     *
     * <p> This is designed to be directly driven by joystick axes.
     *
     * @param x the speed that the robot should drive in the X direction.
     * [-1.0..1.0]
     * @param y the speed that the robot should drive in the Y direction. This
     * input is inverted to match the forward == -1.0 that joysticks produce.
     * [-1.0..1.0]
     * @param rotation the rate of rotation for the robot that is completely
     * independent of the translation. [-1.0..1.0]
     * @param gyroAngle the current angle reading from the gyro. Use this to
     * implement field-oriented controls.
     */
    public final void mecanumDrive_Cartesian(double x, double y, double rotation, double gyroAngle) {
        drive.mecanumDrive_Cartesian(x, function.apply(y), rotation, gyroAngle);
    }

    /**
     * A method for driving with Mecanum wheeled robots. There are 4 wheels on
     * the robot, arranged so that the front and back wheels are toed in 45
     * degrees. When looking at the wheels from the top, the roller axles should
     * form an X across the robot.
     *
     * @param magnitude the speed that the robot should drive in a given
     * direction.
     * @param direction the direction the robot should drive in degrees. The
     * direction and magnitude are independent of the rotation rate.
     * @param rotation the rate of rotation for the robot that is completely
     * independent of the magnitude or direction. [-1.0..1.0]
     */
    public final void mecanumDrive_Polar(double magnitude, double direction, double rotation) {
        drive.mecanumDrive_Polar(function.apply(magnitude), direction, rotation);
    }

    /**
     * Set the speed of the right and left motors.
     *
     * @param leftOutput the speed to send to the left side of the robot.
     * @param rightOutput the speed to send to the right side of the robot.
     */
    public final void setLeftRightMotorOutputs(double leftOutput, double rightOutput) {
        if (reverseSpeed) {
            leftOutput = -leftOutput;
            rightOutput = -rightOutput;
        }
        leftOutput = function.apply(leftOutput);
        rightOutput = function.apply(rightOutput);
        drive.setLeftRightMotorOutputs(leftOutput, rightOutput);
    }

    /**
     * Configure the scaling factor for using RobotDrive with motor controllers
     * in a mode other than PercentVbus.
     *
     * @param maxOutput multiplied with the output percentage computed by the
     * drive functions.
     */
    public void setMaxOutput(double maxOutput) {
        drive.setMaxOutput(maxOutput);
    }

    /**
     * Set the expiration time for the corresponding motor safety object.
     *
     * @param timeout the timeout value in seconds.
     */
    public final void setExpiration(double timeout) {
        drive.setExpiration(timeout);
    }

    /**
     * Retrieve the timeout value for the corresponding motor safety object.
     *
     * @return the timeout value in seconds.
     */
    public final double getExpiration() {
        return drive.getExpiration();
    }

    /**
     * Return the state of the motor safety enabled flag. Return if the motor
     * safety is currently enabled for this device.
     *
     * @return true if motor safety is enforced for this device
     */
    public final boolean isSafetyEnabled() {
        return drive.isSafetyEnabled();
    }

    /**
     * Enable/disable motor safety for this device. Turn on and off the motor
     * safety option for this PWM object.
     *
     * @param enabled true if motor safety is enforced for this object
     */
    public final void setSafetyEnabled(boolean enabled) {
        drive.setSafetyEnabled(enabled);
    }

    /**
     * Stops all motors that are part of the robot drive.
     */
    public final void stopMotors() {
        drive.stopMotor();
    }

    public final PIDOutput getForwards() {
        return new PIDOutput() {
            public void pidWrite(double output) {
                arcadeDrive(output, 0);
            }
        };
    }

    public final PIDOutput getTurning() {
        return new PIDOutput() {
            public void pidWrite(double output) {
                arcadeDrive(0, output);
            }
        };
    }

    public void set(double value) {
        arcadeDrive(value, 0);
    }
}
