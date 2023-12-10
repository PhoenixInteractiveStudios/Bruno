package org.burrow_studios.bruno.tags;

public enum Priority {
    LOWEST(":purple_circle:", "Lowest"),
    LOW(":blue_circle:", "Low"),
    NORMAL(":yellow_circle:", "Normal"),
    HIGH(":orange_circle:", "High"),
    HIGHEST(":red_circle:", "Highest");

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
