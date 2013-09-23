package com.eyeofender.dodgeball.game;

public enum Team {

    NONE((short) -1),

    WHITE((short) 0),
    ORANGE((short) 1),
    MAGENTA((short) 2),
    LIGHT_BLUE((short) 3),
    YELLOW((short) 4),
    GREEN((short) 5),
    PINK((short) 6),
    DARK_GRAY((short) 7),
    GRAY((short) 8),
    AQUA((short) 9),
    PURPLE((short) 10),
    BLUE((short) 11),
    BROWN((short) 12),
    DARK_GREEN((short) 13),
    RED((short) 14),
    BLACK((short) 15);

    final short woolData;

    Team(short woolData) {
        this.woolData = woolData;
    }

    public static Team fromString(String team) {
        for (Team t : values())
            if (t.toString().equalsIgnoreCase(team))
                return t;
        return NONE;
    }
}
