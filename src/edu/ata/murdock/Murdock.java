package edu.ata.murdock;

import edu.ata.autonomous.GordianAuto;
import edu.ata.binds.SetWinchSpeed;
import edu.ata.binds.SetWiperSpeed;
import edu.ata.commands.AdjustRPM;
import edu.ata.commands.AutoShoot;
import edu.ata.commands.SetAlignment;
import edu.ata.commands.SetGear;
import edu.ata.commands.SetLoader;
import edu.ata.commands.SetShooter;
import edu.ata.commands.SetWinch;
import edu.ata.modules.XboxController;
import edu.ata.subsystems.AlignmentSystem;
import edu.ata.subsystems.BitchBar;
import edu.ata.subsystems.Compressor;
import edu.ata.subsystems.Drivetrain;
import edu.ata.subsystems.Driving;
import edu.ata.subsystems.GearShifters;
import edu.ata.subsystems.Loader;
import edu.ata.subsystems.MovementSystem;
import edu.ata.subsystems.ShooterWheel;
import edu.ata.subsystems.SmartDashboardSender;
import edu.ata.subsystems.Winch;
import edu.ata.subsystems.WindshieldWiper;
import edu.first.commands.SetNumberCommand;
import edu.first.module.actuator.SolenoidModule;
import edu.first.module.driving.RobotDriveModule;
import edu.first.module.sensor.DigitalLimitSwitchModule;
import edu.first.module.sensor.EncoderModule;
import edu.first.module.sensor.GyroModule;
import edu.first.module.sensor.HallEffectModule;
import edu.first.module.sensor.PotentiometerModule;
import edu.first.identifiers.Function;
import edu.first.module.actuator.DualActionSolenoidModule;
import edu.first.module.joystick.BindableJoystick;
import edu.first.module.speedcontroller.SpeedControllerModule;
import edu.first.module.speedcontroller.SpikeRelayModule;
import edu.first.module.target.BangBangModule;
import edu.first.robot.Robot;
import edu.first.robot.RobotAdapter;
import edu.first.utils.DriverstationInfo;
import edu.first.utils.Logger;
import edu.first.utils.TransferRateCalculator;
import edu.first.utils.preferences.DoublePreference;
import edu.first.utils.preferences.StringPreference;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;

/**
 * Our 2013 robot, Murdock. Our beginning and end.
 *
 * @author Joel Gallant
 */
public final class Murdock {

    // Preferences in code //
    private static final double defaultArm = 5;
    private static final double defaultRPM = 4000;
    private static final String defaultAuto = "auto";
    private static final double triggerShotThreashold = 0.7;
    private static final double wiperSpeed = 0.4;
    private static final double rpmAdjustment = 25;
    private static final boolean reverseSpeed = false;
    private static final boolean reverseTurn = true;
    private static final boolean reverseShooter = false;
    public static final int competitionPort = 1;
    public static final int smartDashboardPort = 2;
    // Important things //
    private static Murdock MURDOCK;
    private final Robot murdock = new MurdockRobot();
    private final RobotMode normalMode = new NormalMode();
    private long lastSave = System.currentTimeMillis();
    private DoublePreference Zero = new DoublePreference("Zero", 0);
    private StringPreference AUTOMODE = new StringPreference("AutonomousMode", defaultAuto);
    private DoublePreference ShooterRPM = new DoublePreference("ShooterRPM", defaultRPM);
    private DoublePreference ASetpoint = new DoublePreference("ASetpoint", defaultArm);
    private DoublePreference ARPM = new DoublePreference("ARPM", defaultRPM);
    private DoublePreference BSetpoint = new DoublePreference("BSetpoint", defaultArm);
    private DoublePreference BRPM = new DoublePreference("BRPM", defaultRPM);
    private DoublePreference XSetpoint = new DoublePreference("XSetpoint", defaultArm);
    private DoublePreference XRPM = new DoublePreference("XRPM", defaultRPM);
    private DoublePreference YSetpoint = new DoublePreference("YSetpoint", defaultArm);
    private DoublePreference YRPM = new DoublePreference("YRPM", defaultRPM);
    // WPILIBJ //
    private final PortMapFile mapFile = PortMapFile.getInstance();
    private final DigitalInput _psi120 = new DigitalInput(mapFile.getPort("psi120", 5));
    private final DigitalInput _psi60 = new DigitalInput(mapFile.getPort("psi60", 6));
    private final AnalogChannel _potentiometer = new AnalogChannel(mapFile.getPort("Pot", 1));
    private final DigitalInput _hallEffect = new DigitalInput(mapFile.getPort("HallEffect", 1));
    private final Encoder _encoder = new Encoder(mapFile.getPort("EncoderA", 2), mapFile.getPort("EncoderB", 3));
    private final Gyro _gyro = new Gyro(mapFile.getPort("Gyro", 2));
    private final Relay _compressorRelay = new Relay(mapFile.getPort("Compressor", 1));
    private final Joystick _joystick1 = new Joystick(mapFile.getPort("Joystick1", 1));
    private final Joystick _joystick2 = new Joystick(mapFile.getPort("Joystick2", 2));
    private final Talon _shooter = new Talon(mapFile.getPort("Shooter", 1));
    private final Victor _winchMotor = new Victor(mapFile.getPort("Winch", 2));
    private final Victor _leftBack = new Victor(mapFile.getPort("LeftBack", 5));
    private final Victor _leftFront = new Victor(mapFile.getPort("LeftFront", 6));
    private final Victor _rightBack = new Victor(mapFile.getPort("RightBack", 3));
    private final Victor _rightFront = new Victor(mapFile.getPort("RightFront", 4));
    private final SpeedController _windshieldWiperMotor = new Victor(mapFile.getPort("WindshiedWiper", 7));
    private final RobotDrive _drive = new RobotDrive(_leftFront, _leftBack, _rightFront, _rightBack);
    private final Solenoid _loadIn = new Solenoid(mapFile.getPort("LoadIn", 8));
    private final Solenoid _loadOut = new Solenoid(mapFile.getPort("LoadOut", 7));
    private final Solenoid _bitchBarIn = new Solenoid(mapFile.getPort("BitchBarIn", 5));
    private final Solenoid _bitchBarOut = new Solenoid(mapFile.getPort("BitchBarOut", 6));
    private final Solenoid _gearUp = new Solenoid(mapFile.getPort("GearUp", 4));
    private final Solenoid _gearDown = new Solenoid(mapFile.getPort("GearDown", 3));
    private final Solenoid _backLeft = new Solenoid(mapFile.getPort("BackLeft", 2));
    private final Solenoid _backRight = new Solenoid(mapFile.getPort("BackRight", 1));
    // Robot //
    private final TransferRateCalculator transferRate = new TransferRateCalculator();
    private final DigitalLimitSwitchModule psi120 = new DigitalLimitSwitchModule(_psi120);
    private final DigitalLimitSwitchModule psi60 = new DigitalLimitSwitchModule(_psi60);
    private final PotentiometerModule potentiometer = new PotentiometerModule(_potentiometer);
    private final HallEffectModule hallEffect = new HallEffectModule(_hallEffect);
    private final EncoderModule encoder = new EncoderModule(_encoder, Encoder.PIDSourceParameter.kDistance);
    private final GyroModule gyro = new GyroModule(_gyro);
    private final SpikeRelayModule compressorRelay = new SpikeRelayModule(_compressorRelay);
    private final XboxController joystick1 = new XboxController(_joystick1);
    private final XboxController joystick2 = new XboxController(_joystick2);
    private final SpeedControllerModule shooter = new SpeedControllerModule(_shooter);
    private final SpeedControllerModule winchMotor = new SpeedControllerModule(_winchMotor);
    private final RobotDriveModule drive = new RobotDriveModule(_drive, reverseSpeed, reverseTurn);
    private final SpeedControllerModule windshieldWiperMotor = new SpeedControllerModule(_windshieldWiperMotor);
    private final DualActionSolenoidModule _loader = new DualActionSolenoidModule(_loadIn, _loadOut);
    private final DualActionSolenoidModule _bitchBar = new DualActionSolenoidModule(_bitchBarIn, _bitchBarOut);
    private final DualActionSolenoidModule _gearShifters = new DualActionSolenoidModule(_gearDown, _gearUp);
    private final SolenoidModule backLeft = new SolenoidModule(_backLeft);
    private final SolenoidModule backRight = new SolenoidModule(_backRight);
    private final BangBangModule shooterBangBang = new BangBangModule(hallEffect, shooter, 0, reverseShooter);
    // Subsystems //
    private final AlignmentSystem alignmentSystem = new AlignmentSystem(backLeft, backRight);
    private final BitchBar bitchBar = new BitchBar(_bitchBar);
    private final Compressor compressor = new Compressor(psi120, compressorRelay);
    private final Drivetrain drivetrain = new Drivetrain(drive);
    private final Driving driving = new Driving(drivetrain, joystick1, joystick2);
    private final GearShifters gearShifters = new GearShifters(_gearShifters);
    private final Loader loader = new Loader(_loader, shooterBangBang, potentiometer);
    private final MovementSystem movementSystem = new MovementSystem(drive, encoder);
    private final ShooterWheel shooterWheel = new ShooterWheel(shooterBangBang);
    private final Winch winch = new Winch(winchMotor, potentiometer);
    private final WindshieldWiper windshieldWiper = new WindshieldWiper(windshieldWiperMotor);
    private final SmartDashboardSender smartDashboardSender =
            new SmartDashboardSender(shooterWheel, psi60, psi120, bitchBar, alignmentSystem, winch, gearShifters,
            windshieldWiper, encoder, gyro, transferRate);
    private final BindableJoystick BINDS = new BindableJoystick(new Joystick(8));

    public static Murdock getInstance() {
        synchronized (Murdock.class) {
            if (MURDOCK == null) {
                MURDOCK = new Murdock();
            }
        }
        return MURDOCK;
    }

    private Murdock() {
    }

    public Robot getRobot() {
        return murdock;
    }

    private RobotMode getSelectedRobot() {
        return normalMode;
    }

    // Static to appear before final objects made
    static {
        Logger.log(Logger.Urgency.USERMESSAGE, "Initializing...");
    }

    private void init() {
        Logger.log(Logger.Urgency.USERMESSAGE, "IO " + competitionPort + " = Competition");
        Logger.log(Logger.Urgency.USERMESSAGE, "IO " + smartDashboardPort + " = SmartDashboard");
        if (DriverstationInfo.FMSattached()) {
            Logger.log(Logger.Urgency.USERMESSAGE, "FMS attached - reverting to competition mode");
            DriverstationInfo.getDS().setDigitalOut(1, true);
        }
        Logger.log(Logger.Urgency.USERMESSAGE, DriverstationInfo.getAllianceName() + " "
                + DriverstationInfo.getAllianceLocation());

        int ix = (int) (DriverstationInfo.getBatteryVoltage() * 100.0); // scale it 
        double rounded = ((double) ix) / 100.0;
        Logger.log(Logger.Urgency.USERMESSAGE, "Battery: " + rounded);
        Logger.log(Logger.Urgency.USERMESSAGE, "Good luck Team " + DriverstationInfo.getTeamNumber() + "!");

        Zero.create();
        // Zero moved
        if (Math.abs(Zero.get() - _potentiometer.getVoltage()) < 0.2) {
            Zero.set(_potentiometer.getVoltage());
            Logger.log(Logger.Urgency.USERMESSAGE, "Winch is using default zero!");
        }
        winch.setZero(Zero.get());

        AUTOMODE.create();
        ShooterRPM.create();
        ASetpoint.create();
        ARPM.create();
        BSetpoint.create();
        BRPM.create();
        XSetpoint.create();
        XRPM.create();
        YSetpoint.create();
        YRPM.create();
    }

    private void disabled() {
        // can only save every 30 seconds - prevents breakage
        if (System.currentTimeMillis() - lastSave > 30000) {
            lastSave = System.currentTimeMillis();
            Logger.log(Logger.Urgency.USERMESSAGE, "Saving Preferences");
            Preferences.getInstance().save();
        }

        Logger.log(Logger.Urgency.LOG, "Disabling...");

        joystick1.disable();
        joystick2.disable();
        alignmentSystem.disable();
        bitchBar.disable();
        compressor.disable();
        drivetrain.disable();
        driving.disable();
        gearShifters.disable();
        loader.disable();
        shooterWheel.disable();
        smartDashboardSender.disable();
        winch.disable();
        windshieldWiper.disable();

        Logger.log(Logger.Urgency.USERMESSAGE, "Robot is disabled.");
    }

    private void doScriptAutonomous() {

        // End any teleop
        driving.disable();

        alignmentSystem.enable();
        bitchBar.enable();
        compressor.enable();
        drivetrain.enable();
        gearShifters.enable();
        loader.enable();
        movementSystem.enable();
        shooterWheel.enable();
        smartDashboardSender.enable();
        winch.enable();
        windshieldWiper.enable();

        drive.setSafetyEnabled(false);
        encoder.reset();
        gyro.reset();

        GordianAuto.ensureInit(alignmentSystem, bitchBar, compressor, drivetrain,
                gearShifters, loader, movementSystem, shooterWheel, smartDashboardSender,
                winch, windshieldWiper);
        try {
            String current = AUTOMODE.get();
            Logger.log(Logger.Urgency.USERMESSAGE, "Running auto/" + current + ".txt");
            GordianAuto.run("auto/" + current + ".txt");
            Logger.log(Logger.Urgency.USERMESSAGE, "Autonomous complete");
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.log(Logger.Urgency.USERMESSAGE, "AUTO DID NOT RUN");
        }
    }

    private void doTeleopBinds() {
        BINDS.removeAllBinds();

        BINDS.addWhenPressed(joystick1.getLeftBumper(),
                new SetGear(gearShifters, SetGear.FIRST, false));
        BINDS.addWhenPressed(joystick1.getRightBumper(),
                new SetGear(gearShifters, SetGear.SECOND, false));

        BINDS.addWhenPressed(joystick1.getAxisAsButton(XboxController.TRIGGERS, triggerShotThreashold),
                new AutoShoot(shooterWheel, loader, true));
        BINDS.addWhenPressed(joystick1.getAxisAsButton(XboxController.TRIGGERS, -triggerShotThreashold),
                new SetLoader(loader, SetLoader.FIRE, true));

        BINDS.addWhenPressed(joystick1.getAButton(),
                new SetAlignment(alignmentSystem, SetAlignment.SWITCH, false));

        BINDS.addWhenPressed(joystick1.getBackButton(),
                new SetWinch(winch, SetWinch.POSITION, 0, false));

        BINDS.addWhenPressed(joystick1.getStartButton(),
                new SetWinch(winch, SetWinch.ZERO, potentiometer, false));
        BINDS.addWhenPressed(joystick1.getStartButton(), 
                new SetNumberCommand(Zero, potentiometer));

        BINDS.addAxis(joystick1.getDirectionalPad(),
                new SetWiperSpeed(windshieldWiper), new Function.ProductFunction(wiperSpeed));

        // JOYSTICK 2 //

        BINDS.addWhenPressed(joystick2.getLeftBumper(),
                new AdjustRPM(shooterWheel, -rpmAdjustment, false));
        BINDS.addWhenPressed(joystick2.getLeftBumper(),
                new SetNumberCommand(ShooterRPM, shooterWheel));

        BINDS.addWhenPressed(joystick2.getRightBumper(),
                new AdjustRPM(shooterWheel, +rpmAdjustment, false));
        BINDS.addWhenPressed(joystick2.getRightBumper(),
                new SetNumberCommand(ShooterRPM, shooterWheel));

        BINDS.addAxis(joystick2.getTriggers(),
                new SetWinchSpeed(winch), new Function() {
            public double apply(double start) {
                return start > 0 ? (start * start) : -(start * start);
            }
        });

        BINDS.addWhenPressed(joystick2.getAButton(),
                new SetWinch(winch, SetWinch.POSITION, ASetpoint, false));
        BINDS.addWhenPressed(joystick2.getAButton(),
                new SetShooter(shooterWheel, ARPM, false));
        BINDS.addWhenPressed(joystick2.getAButton(),
                new SetNumberCommand(ShooterRPM, shooterWheel));

        BINDS.addWhenPressed(joystick2.getBButton(),
                new SetWinch(winch, SetWinch.POSITION, BSetpoint, false));
        BINDS.addWhenPressed(joystick2.getBButton(),
                new SetShooter(shooterWheel, BRPM, false));
        BINDS.addWhenPressed(joystick2.getBButton(),
                new SetNumberCommand(ShooterRPM, shooterWheel));

        BINDS.addWhenPressed(joystick2.getXButton(),
                new SetWinch(winch, SetWinch.POSITION, XSetpoint, false));
        BINDS.addWhenPressed(joystick2.getXButton(),
                new SetShooter(shooterWheel, XRPM, false));
        BINDS.addWhenPressed(joystick2.getXButton(),
                new SetNumberCommand(ShooterRPM, shooterWheel));

        BINDS.addWhenPressed(joystick2.getYButton(),
                new SetWinch(winch, SetWinch.POSITION, YSetpoint, false));
        BINDS.addWhenPressed(joystick2.getYButton(),
                new SetShooter(shooterWheel, YRPM, false));
        BINDS.addWhenPressed(joystick2.getYButton(),
                new SetNumberCommand(ShooterRPM, shooterWheel));

        BINDS.addWhenPressed(joystick2.getStartButton(),
                new SetShooter(shooterWheel, ShooterRPM, false));
        BINDS.addWhenPressed(joystick2.getStartButton(),
                new SetLoader(loader, SetLoader.OUT, false));

        BINDS.addWhenPressed(joystick2.getBackButton(),
                new SetShooter(shooterWheel, 0, false));
        BINDS.addWhenPressed(joystick2.getBackButton(),
                new SetLoader(loader, SetLoader.IN, false));

        Logger.log(Logger.Urgency.USERMESSAGE, "Teleop Binds Ready");
    }

    public final class NormalMode extends RobotMode {

        public void autonomousInit() {
            doScriptAutonomous();
        }

        public void teleopInit() {

            // End any autonomous
            movementSystem.disable();

            joystick1.enable();
            joystick2.enable();
            alignmentSystem.enable();
            bitchBar.enable();
            compressor.enable();
            driving.enable();
            gearShifters.enable();
            loader.enable();
            shooterWheel.enable();
            smartDashboardSender.enable();
            winch.enable();
            windshieldWiper.enable();

            shooterWheel.setRPM(0);
            drive.setSafetyEnabled(true);

            doTeleopBinds();
        }

        public void teleopPeriodic() {
            BINDS.doBinds();
        }
    }

    // Meant to restrict access to robotInit() and disabledInit()
    private static class RobotMode extends RobotAdapter {

        public final void robotInit() {
        }

        public final void disabledInit() {
        }
    }

    private final class MurdockRobot extends RobotAdapter {

        public void robotInit() {
            init();
        }

        public void disabledInit() {
            disabled();
        }
        private Robot robot = getSelectedRobot();

        public void autonomousInit() {
            (robot = getSelectedRobot()).autonomousInit();
        }

        public void autonomousPeriodic() {
            robot.autonomousPeriodic();
        }

        public void teleopInit() {
            (robot = getSelectedRobot()).teleopInit();
        }

        public void teleopPeriodic() {
            robot.teleopPeriodic();
        }

        public void testInit() {
            (robot = getSelectedRobot()).testInit();
        }

        public void testPeriodic() {
            robot.testPeriodic();
        }
    }
}
