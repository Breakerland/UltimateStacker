package com.songoda.ultimatestacker.entity;

import java.util.HashMap;
import java.util.Map;

public enum Check {

    SPAWN_REASON(false),
    NERFED(true),
    AGE(true),
    TICK_AGE(false),
    IS_TAMED(true),
    ANIMAL_OWNER(true),
    SKELETON_TYPE(true),
    ZOMBIE_BABY(true),
    HAS_EQUIPMENT(true),
    SLIME_SIZE(true),
    PIG_SADDLE(true),
    SHEEP_SHEARED(true),
    SHEEP_COLOR(true),
    SNOWMAN_DERPED(true),
    WOLF_COLLAR_COLOR(true),
    OCELOT_TYPE(true),
    HORSE_COLOR(true),
    HORSE_STYLE(true),
    HORSE_CARRYING_CHEST(true),
    HORSE_HAS_ARMOR(true),
    HORSE_HAS_SADDLE(true),
    HORSE_JUMP(true),
    RABBIT_TYPE(true),
    VILLAGER_PROFESSION(true),
    LLAMA_COLOR(true),
    LLAMA_STRENGTH(true),
    PARROT_TYPE(true),
    PUFFERFISH_STATE(true),
    TROPICALFISH_PATTERN(true),
    TROPICALFISH_BODY_COLOR(true),
    TROPICALFISH_PATTERN_COLOR(true),
    PHANTOM_SIZE(true),
    CAT_TYPE(true);

    private final boolean isEnabledByDefault;
    private final static Map<String, Check> checks = new HashMap();

    static {
        for (Check c : values()) {
            checks.put(c.name(), c);
        }
    }

    Check(boolean isEnabledByDefault) {
        this.isEnabledByDefault = isEnabledByDefault;
    }

    public boolean isEnabledByDefault() {
        return isEnabledByDefault;
    }

    public static Check getCheck(String name) {
        return name != null ? checks.get(name.toUpperCase()) : null;
    }
}
