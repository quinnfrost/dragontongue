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
        FIRE,
        NONE
        ;
        private static final GroundAttackType[] vals = values();
        public GroundAttackType prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}

        public GroundAttackType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum AirAttackType {
        ANY,
        SCORCH_STREAM,
        HOVER_BLAST,
        TACKLE,
        NONE
        ;
        private static final AirAttackType[] vals = values();
        public AirAttackType prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}

        public AirAttackType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum AttackDecisionType {
        GUARD,
        ALWAYS_HELP,
        DONT_HELP,
        NONE
        ;
        private static final AttackDecisionType[] vals = values();
        public AttackDecisionType prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}

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
        public MovementType prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}

        public MovementType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum DestroyType {
        ANY,
        CAREFUL_AROUND_ROOST,
        NONE,
        DELIBERATE
        ;
        private static final DestroyType[] vals = values();
        public DestroyType prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}

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
        public BreathType prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}
        public BreathType next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum CommandStatus {
        NONE, // The vanilla behavior: wander, sit, escort
        REACH,
        STAY,
        HOVER,
        ATTACK
        ;
        private static final CommandStatus[] vals = values();
        public CommandStatus prev() {return vals[(this.ordinal() + vals.length - 1) % vals.length];}
        public CommandStatus next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }
}
