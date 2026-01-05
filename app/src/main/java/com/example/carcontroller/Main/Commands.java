package com.example.carcontroller.Main;

public enum Commands {
    // Other commands
    EMPTY("00"), // Nothing

    // Setup commands
    SETUPMODE("S0"), DRIVEMODE("S1"), LINEFOLLOWERMODE("S2"),

    // Drive commands
    STOP("D0"), FORWARD("D1"), REVERSE("D2"), LEFT("D3"), RIGHT("D4"), FORWARDLEFT("D5"), FORWARDRIGHT("D6"), REVERSLEFT("D7"), REVERSERIGHT("D8"),

    // Line Follower commands
    WAITFOR1("W1"), WAITFOR2("W2"), WAITFOR3("W3"), WAITFOR4("W4"), WAITFOR5("W5");
    private final String command;

    Commands (String command) {
        this.command = command;
    }

    public String getCommand () {
        return command;
    }
}
