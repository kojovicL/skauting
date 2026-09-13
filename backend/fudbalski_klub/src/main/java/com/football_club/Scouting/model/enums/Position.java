package com.football_club.Scouting.model.enums;

import lombok.Getter;

@Getter
public enum Position {

    // --- GOALKEEPERS ---
    GK("Goalkeeper", Category.GOALKEEPER),

    // --- DEFENDERS ---
    CB("Center-Back", Category.DEFENDER),
    LB("Left-Back", Category.DEFENDER),
    RB("Right-Back", Category.DEFENDER),
    LWB("Left Wing-Back", Category.DEFENDER),
    RWB("Right Wing-Back", Category.DEFENDER),

    // --- MIDFIELDERS ---
    CDM("Defensive Midfielder", Category.MIDFIELDER),
    CM("Central Midfielder", Category.MIDFIELDER),
    CAM("Attacking Midfielder", Category.MIDFIELDER),
    LM("Left Midfielder", Category.MIDFIELDER),
    RM("Right Midfielder", Category.MIDFIELDER),

    // --- ATTACKERS ---
    LW("Left Winger", Category.ATTACKER),
    RW("Right Winger", Category.ATTACKER),
    ST("Striker", Category.ATTACKER),
    CF("Center Forward", Category.ATTACKER);

    private final String displayName;
    private final Category category;

    Position(String displayName, Category category) {
        this.displayName = displayName;
        this.category = category;
    }

    public enum Category {
        GOALKEEPER,
        DEFENDER,
        MIDFIELDER,
        ATTACKER
    }

    /**
     * Safely converts generic string inputs from API-Football into the Enum.
     */
    public static Position fromApiString(String apiPosition) {
        if (apiPosition == null || apiPosition.isBlank()) return CM; // Fallback

        return switch (apiPosition.trim().toLowerCase()) {
            case "goalkeeper", "g" -> GK;
            case "defender", "d" -> CB;
            case "midfielder", "m" -> CM;
            case "attacker", "a", "f" -> ST;
            case "cb" -> CB;
            case "lb" -> LB;
            case "rb" -> RB;
            case "cdm" -> CDM;
            case "cam" -> CAM;
            case "lw" -> LW;
            case "rw" -> RW;
            case "st", "cf" -> ST;
            default -> CM;
        };
    }
}