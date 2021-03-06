package edu.first.utils;

import com.sun.squawk.microedition.io.FileConnection;
import edu.wpi.first.wpilibj.DriverStationLCD;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;

/**
 * Static utility class used to access the various forms of logging that are
 * used. Is capable of logging messages to the log file (available in
 * {@link Logger#PATH} through ftp), sending messages to the DriverStation
 * console (box on the right side of the program) and printing .
 *
 * <p> Logs are sent with different urgencies, which influence how they are sent
 * to the user. Urgency constants are available in {@link Urgency}, in an
 * enum-like format.
 *
 * <p> To retrieve the log file contents, use FTP or {@link Logger#getLog()}.
 *
 * @author Joel Gallant
 */
public final class Logger {

    private static final String PATH = "file:///log.txt";
    private static boolean fileLoggingOn = true;
    private static int lineNum = 1;
    private static FileConnection logFile;

    // cannot be subclassed or instantiated
    private Logger() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Class representing the urgency of the log. The reason for this
     * implementation is to ensure that logging does not encounter an error when
     * looking at its urgency. (Ex. int could be out of range, etc.) This keeps
     * the only possible values to be pre-approved by this class. (No errors
     * when logging)
     *
     * <p> <b> Null urgency objects are possible, but should throw an error
     * anyways. </b>
     */
    public final static class Urgency {

        private Urgency() {
        }
        /**
         * User message. Displayed on DriverStation. (Sent to log and console)
         */
        public final static Urgency USERMESSAGE = new Urgency();
        /**
         * Log message. Is not displayed to user. (Sent to console)
         */
        public final static Urgency LOG = new Urgency();
    }

    /**
     * Logs the message according to the urgency. There are three different ways
     * to log messages:
     *
     * <pre>
     * 1. Send to the console (netbeans console that code was deployed on
     *
     * 2. Send to the DriverStation console (box on the right side)
     * </pre>
     *
     * The urgency and logging methods are as follows:
     *
     * <pre>
     * {@link Urgency#USERMESSAGE} - 1, 2
     *
     * {@link Urgency#LOG} - 1
     * </pre>
     *
     * <p> Use a null urgency to just send the message to log file and console.
     *
     * @param urgency representation of how the message should be delivered
     * @param msg message to log
     */
    public static void log(Urgency urgency, String msg) {
        String usrMsg = msg;
        if (urgency == Urgency.USERMESSAGE) {
            displayLCDMessage(usrMsg, true);
        }
        System.out.println(msg);
    }

    private static FileConnection logFileInput() throws IOException {
        if (logFile == null) {
            logFile = (FileConnection) Connector.open(PATH, Connector.READ);
            logFile.create();
        }
        return logFile;
    }

    /**
     * Returns the full text from the log file.
     *
     * @see Logger#logFile(java.lang.String)
     * @throws IOException thrown when connection cannot be created or the file
     * cannot be accessed / read from
     * @return text in the log file
     */
    public static String getLog() throws IOException {
        return getTextFromFile(logFileInput());
    }

    /**
     * Returns the full text from a text file based on its
     * {@link DataInputStream}.
     *
     * <i> Note: </i> If preferences are currently being read, this method can
     * escape into an infinite loop. Be sure that they are not (only done at the
     * start of the robot code)
     *
     * @param connection the file connection to read from
     * @return text from the file
     * @throws IOException thrown when error occurs
     */
    public static synchronized String getTextFromFile(FileConnection connection) throws IOException {
        if (connection == null) {
            throw new NullPointerException();
        }
        InputStream stream = connection.openInputStream();
        StringBuffer buffer = new StringBuffer();
        char buf;
        while ((buf = (char) stream.read()) != -1 && buf != 65535) {
            buffer.append(buf);
        }
        return buffer.toString();
    }

    /**
     * Displays a string on the {@link DriverStationLCD}. This is the box on the
     * DriverStation on the right side. <b> Messages cannot be over
     * {@link DriverStationLCD#kLineLength}.</b> Messages are displayed on the
     * next line, meaning that if the previous message was displayed on line
     * <i>x</i>, the next message by convention is displayed on line <i>x+1</i>,
     * etc.
     *
     * @param msg message to display on next line
     * @param blank if a blank line should be added after the line
     */
    public static void displayLCDMessage(String msg, boolean blank) {
        if (msg == null) {
            throw new NullPointerException();
        }
        if (msg.length() > DriverStationLCD.kLineLength) {
            displayLCDMessage(msg.substring(0, DriverStationLCD.kLineLength), false);
            displayLCDMessage(msg.substring(DriverStationLCD.kLineLength), true);
            return;
        }
        DriverStationLCD.Line line;
        DriverStationLCD.Line blankLine;
        switch (lineNum) {
            case (1):
                line = DriverStationLCD.Line.kUser1;
                blankLine = DriverStationLCD.Line.kUser2;
                break;
            case (2):
                line = DriverStationLCD.Line.kUser2;
                blankLine = DriverStationLCD.Line.kUser3;
                break;
            case (3):
                line = DriverStationLCD.Line.kUser3;
                blankLine = DriverStationLCD.Line.kUser4;
                break;
            case (4):
                line = DriverStationLCD.Line.kUser4;
                blankLine = DriverStationLCD.Line.kUser5;
                break;
            case (5):
                line = DriverStationLCD.Line.kUser5;
                blankLine = DriverStationLCD.Line.kUser6;
                break;
            case (6):
                line = DriverStationLCD.Line.kUser6;
                blankLine = DriverStationLCD.Line.kUser1;
                break;
            default:
                line = DriverStationLCD.Line.kUser1;
                blankLine = DriverStationLCD.Line.kUser2;
        }
        DriverStationLCD.getInstance().println(line, 1, msg + "                      ");
        if (blank) {
            DriverStationLCD.getInstance().println(blankLine, 1, "^---^                ");
        }
        DriverStationLCD.getInstance().updateLCD();
        if (++lineNum > 6) {
            lineNum = 1;
        }
    }

    /**
     * Sets whether or not logging through
     * {@link Logger#log(edu.ATA.main.Logger.Urgency, java.lang.String) log(Urgency, String)}
     * will log to the file at {@link Logger#PATH}. This is enabled by default.
     *
     * @param fileLoggingOn whether messages should be logged to the log file
     */
    public static void setFileLoggingOn(boolean fileLoggingOn) {
        Logger.fileLoggingOn = fileLoggingOn;
    }

    /**
     * Returns whether or not
     * {@link Logger#log(edu.ATA.main.Logger.Urgency, java.lang.String) log(Urgency, String)}
     * will log to the file at {@link Logger#PATH}. This is enabled by default.
     *
     * @return whether messages are being logged to the log file
     */
    public static boolean isFileLoggingOn() {
        return fileLoggingOn;
    }
}
