package com.example.carcontroller;

public enum Commands {
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

    private final String command;

    Commands (String command) {
        this.command = command;
    }

    public String getCommand () {
        return command;
    }
}
