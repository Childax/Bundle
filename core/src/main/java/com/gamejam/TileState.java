package com.gamejam;

public enum TileState {
    EMPTY,      // No letter yet
    FILLED,     // Letter typed but not submitted
    CORRECT,    // Green
    PRESENT,    // Yellow
    ABSENT      // Gray
}
