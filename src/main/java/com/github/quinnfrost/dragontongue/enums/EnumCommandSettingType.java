package com.github.quinnfrost.dragontongue.enums;

public enum EnumCommandSettingType {
        NONE,
        COMMAND_STATUS,
        GROUND_ATTACK_TYPE,
        AIR_ATTACK_TYPE,
        MOVEMENT_TYPE,
        DESTROY_TYPE,
        BREATH_TYPE,
        SHOULD_RETURN_ROOST;

    public enum GroundAttackType {
        NONE,
        BITE,
        SHAKE_PREY,
        TAIL_WHIP,
        WING_BLAST,
        FIRE;

    }

    public enum AirAttackType {
        NONE,
        SCORCH_STREAM,
        HOVER_BLAST,
        TACKLE;

    }

    public enum MovementType {
        ANY,
        LAND,
        AIR
    }

    public enum DestroyType {
        ANY,
        AROUND_ROOST,
        NONE
    }

    public enum BreathType {
        ANY,
        WITHOUT_BLAST,
        NONE
    }

}
