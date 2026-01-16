package com.example.carcontroller.main_package;

/**
 * Enumerator class containing all the standard format command(instruction) set for the devices. <p>
 *
 * Standard format: command_type:value <p>
 * - command_type: Represents the type of command/instruction that will be processed <p>
 * S - setup commands <p>
 * D - drive commands <p>
 * L - line follow commands <p>
 * W - auxiliary commands <p>
 * C - checkpoint commands <p>
 * - value: Represents the value of the command/instruction (e.g. for S command_type): <p>
 * value = 0 - switch to SETUP MODE <p>
 * value = 1 - switch to DRIVE MODE <p>
 * value = 2 - switch to LINE FOLLOWER MODE <p>
 * A special instruction in the standard format exists: EMPTY = "00" - this instruction is a dummy instruction that does nothing
 * <p>
 * Every other instruction that is sent or received and does not respect the standard format it should be announced before using a standard format instruction
 * (e.g. When sending PID values, the value itself does not respect the standard format. For this reason, the auxiliary command WAITFOR1/2/3/4/5 is used notifying
 * the device that it should wait for 1/2/3/4/5 bytes before processing the value)
 * */
public enum Commands {

    // Setup commands
    SETUPMODE("S0"), DRIVEMODE("S1"), LINEFOLLOWERMODE("S2"),

    // Drive commands
    STOP("D0"), FORWARD("D1"), REVERSE("D2"), LEFT("D3"), RIGHT("D4"), FORWARDLEFT("D5"), FORWARDRIGHT("D6"), REVERSLEFT("D7"), REVERSERIGHT("D8"),


    // Line Follower commands
    STOPFOLLOWLINE("L0"), STARTFOLLOWLINE("L1"),

    // Auxiliary commands
    WAITFOR1("W1"), WAITFOR2("W2"), WAITFOR3("W3"), WAITFOR4("W4"), WAITFOR5("W5"),
    EMPTY("00"),    // NOTHING

    // Checkpoint commands
    DETECTED("C1");

    private final String command;

    Commands (String command) {
        this.command = command;
    }

    public String getCommand () {
        return command;
    }
}
