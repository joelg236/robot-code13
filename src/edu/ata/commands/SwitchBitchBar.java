package edu.ata.commands;

import edu.ata.subsystems.ReversingSolenoids;

/**
 * Command to switch the bitch bar's position.
 *
 * @author Joel Gallant <joelgallant236@gmail.com>
 */
public final class SwitchBitchBar extends ThreadableCommand {

    public static final SwitchType IN = new SwitchType(SwitchType.IN),
            OUT = new SwitchType(SwitchType.OUT),
            SWITCH = new SwitchType(SwitchType.SWITCH);
    private final ReversingSolenoids bitchBar;
    private final SwitchType type;

    public SwitchBitchBar(ReversingSolenoids bitchBar, SwitchType type, boolean newThread) {
        super(newThread);
        this.bitchBar = bitchBar;
        this.type = type;
    }

    public SwitchBitchBar(ReversingSolenoids bitchBar, boolean newThread) {
        this(bitchBar, SWITCH, false);
    }

    public Runnable getRunnable() {
        return new Runnable() {
            public void run() {
                if (type.type == IN.type) {
                    bitchBar.setIn();
                } else if (type.type == OUT.type) {
                    bitchBar.setOut();
                } else if (type.type == SWITCH.type) {
                    bitchBar.switchPosition();
                }
            }
        };
    }

    public final static class SwitchType {

        private static final int IN = 1, OUT = 2, SWITCH = 3;
        private final int type;

        public SwitchType(int type) {
            this.type = type;
        }
    }
}
