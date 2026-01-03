package com.example.carcontroller.Main;

public enum Commands {
    // Other commands
    EMPTY("00"), // Nothing

    // Setup commands
    SETUPMODE("S0"),
    DRIVEMODE("S1"),
    LINEFOLLOWERMODE("S2"),

    // Drive commands
    STOP("D0"),
    FORWARD("D1"),
    REVERSE("D2"),
    LEFT("D3"),
    RIGHT("D4"),
    FORWARDLEFT("D5"),
    FORWARDRIGHT("D6"),
    REVERSLEFT("D7"),
    REVERSERIGHT("D8");

    // Line Follower commands

    private final String command;

    Commands (String command) {
        this.command = command;
    }

    public String getCommand () {
        return command;
    }
}
