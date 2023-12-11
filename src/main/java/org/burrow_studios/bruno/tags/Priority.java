package org.burrow_studios.bruno.tags;

public enum Priority {
    LOWEST("\uD83D\uDFE3", "Lowest"),
    LOW("\uD83D\uDD35", "Low"),
    NORMAL("\uD83D\uDFE1", "Normal"),
    HIGH("\uD83D\uDFE0", "High"),
    HIGHEST("\uD83D\uDD34", "Highest");

    private final String emote;
    private final String name;

    Priority(String emote, String name) {
        this.emote = emote;
        this.name = name;
    }

    public String getEmote() {
        return emote;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return "Priority: " + getName();
    }
}
