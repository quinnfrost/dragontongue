package com.github.quinnfrost.dragontongue.enums;

public enum EnumCommandSettingType {
        NONE,
        COMMAND_STATUS,
        GROUND_ATTACK_TYPE,
        AIR_ATTACK_TYPE,
        ATTACK_DECISION_TYPE,
        MOVEMENT_TYPE,
        DESTROY_TYPE,
        BREATH_TYPE,
        SHOULD_RETURN_ROOST
    ;
    public enum GroundAttackType {
        ANY,
        BITE,
        SHAKE_PREY,
        TAIL_WHIP,
        WING_BLAST,
        FIRE
        ;
        private static final GroundAttackType[] vals = values();

        public GroundAttackType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum AirAttackType {
        ANY,
        SCORCH_STREAM,
        HOVER_BLAST,
        TACKLE
        ;
        private static final AirAttackType[] vals = values();

        public AirAttackType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum AttackDecisionType {
        DEFAULT,
        OPEN_FIRE,
        ALWAYS_HELP,
        NONE
        ;
        private static final AttackDecisionType[] vals = values();

        public AttackDecisionType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum MovementType {
        ANY,
        LAND,
        AIR
        ;
        private static final MovementType[] vals = values();

        public MovementType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum DestroyType {
        ANY,
        CAREFUL_AROUND_ROOST,
        NONE
        ;
        private static final DestroyType[] vals = values();

        public DestroyType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum BreathType {
        ANY,
        WITHOUT_BLAST,
        NONE
        ;
        private static final BreathType[] vals = values();

        public BreathType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

}
