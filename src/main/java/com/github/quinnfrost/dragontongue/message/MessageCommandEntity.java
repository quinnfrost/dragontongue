package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MessageCommandEntity {
    public static final BlockPos INVALID_POS = new BlockPos(0, 0.5, 0);
    public static final UUID INVALID_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private EnumCommandType action;
    private UUID commanderUUID;
    private UUID targetUUID;
    private BlockPos blockPos;

    // Add a command entity
    public MessageCommandEntity(EnumCommandType action, UUID commanderUUID, UUID addTarget) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = addTarget != null ? addTarget : INVALID_UUID;
        this.blockPos = INVALID_POS;
    }

    // Command attack target
    public MessageCommandEntity(EnumCommandType action, UUID commanderUUID, @Nullable EntityHitResult targetUUID) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = targetUUID != null ? targetUUID.getEntity().getUUID() : INVALID_UUID;
        this.blockPos = INVALID_POS;
    }

    public MessageCommandEntity(EnumCommandType action, UUID commanderUUID, @Nullable EntityHitResult targetUUID, BlockHitResult blockRayTraceResult) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = targetUUID != null ? targetUUID.getEntity().getUUID() : INVALID_UUID;
        this.blockPos = new BlockPos(blockRayTraceResult.getLocation());
    }

    public MessageCommandEntity(EnumCommandType action, UUID commanderUUID, BlockHitResult position) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = INVALID_UUID;
        this.blockPos = new BlockPos(position.getLocation());

    }


    public MessageCommandEntity(FriendlyByteBuf buf) {
        this.action = EnumCommandType.valueOf(buf.readUtf());
        this.commanderUUID = buf.readUUID();
        this.targetUUID = buf.readUUID();
        this.blockPos = buf.readBlockPos();
        if (!buf.readBoolean() && this.blockPos.equals(INVALID_POS)) {
            blockPos = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(action.name());
        buf.writeUUID(commanderUUID);
        buf.writeUUID(targetUUID);
        if (blockPos != null) {
            buf.writeBlockPos(blockPos);
            buf.writeBoolean(true);
        } else {
            buf.writeBlockPos(INVALID_POS);
            buf.writeBoolean(false);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            ServerLevel serverWorld = contextSupplier.get().getSender().getLevel();
            EnumCommandType action = this.action;
            LivingEntity commander = (LivingEntity) serverWorld.getEntity(this.commanderUUID);
            LivingEntity targetEntity = (LivingEntity) serverWorld.getEntity(this.targetUUID);
            BlockPos targetPos = this.blockPos;

            if (commander == null || action == null) {
                return;
            }
            if (targetEntity != null) {
//                targetEntity.addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
            }

            commandEntity(serverWorld, commander, action, targetEntity, targetPos, null);
        });
        return true;
    }

    /**
     * Main logic upon command entity package arrived
     *
     * @param serverWorld
     * @param commander
     * @param action
     * @param target
     * @param pos
     * @param excludeEntity
     */
    public static void commandEntity(ServerLevel serverWorld, LivingEntity commander, EnumCommandType action,
                                     @Nullable LivingEntity target,
                                     BlockPos pos, @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) entity -> true;
        }
        ICapabilityInfoHolder capTargetHolder = commander.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(commander));

        switch (action) {
            case ADD:
                if (util.isOwner(target, commander)) {
                    capTargetHolder.addCommandEntity(target.getUUID());
                }
                MessageSyncCapability.syncCapabilityToClients(commander);
                break;
            case SET:
                // Allow set a single entity and remove all
                if (target == null) {
                    capTargetHolder.setCommandEntities(new ArrayList<>());
                } else if (util.isOwner(target, commander)) {
                    capTargetHolder.setCommandEntities(new ArrayList<>(Arrays.asList(target.getUUID())));
                }
                MessageSyncCapability.syncCapabilityToClients(commander);
                break;
            case REMOVE:
                if (util.isOwner(target, commander)) {
                    capTargetHolder.removeCommandEntity(target.getUUID());
                    target.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
                        iCapTargetHolder.setDestination(null);
                    });
                }
                MessageSyncCapability.syncCapabilityToClients(commander);
                break;
            case ATTACK:
                List<UUID> attackerUUIDs = commander.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(commander)).getCommandEntities();
                if (!attackerUUIDs.isEmpty()) {
                    for (UUID entityUUID :
                            attackerUUIDs) {
                        commandAttack(commander,
                                (Mob) serverWorld.getEntity(entityUUID), target);
                    }
                } else {
                    commandNearby(EnumCommandType.ATTACK, commander, target, pos, excludeEntity, (float) capTargetHolder.getSelectDistance());
                }
                break;
            case BREATH:
                List<UUID> breathUUIDs = commander.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(commander)).getCommandEntities();
                if (!breathUUIDs.isEmpty()) {
                    for (UUID entityUUID :
                            breathUUIDs) {
                        commandBreath(commander,
                                (LivingEntity) serverWorld.getEntity(entityUUID), pos);
                    }
                } else {
                    commandNearby(EnumCommandType.BREATH, commander, null, pos, excludeEntity, (float) capTargetHolder.getSelectDistance());
                }
                break;
            case NEARBY_ATTACK:
                commandNearby(EnumCommandType.ATTACK, commander, target, pos, excludeEntity, (float) capTargetHolder.getSelectDistance());
                break;
            case LOOP_STATUS:
                loopSitting(commander, false, target);
                break;
            case LOOP_STATUS_REVERSE:
                loopSitting(commander, true, target);
                break;
            case SIT:
                commandSit(commander, target);
                break;
            case FOLLOW:
                commandFollow(commander, target);
                break;
            case WONDER:
                commandWonder(commander, target);
                break;
            case LAND:
                commandLand(commander, target);
                break;
            case HALT:
                if (target != null) {
                    commandHalt(commander, target);
                } else {
                    commandNearby(EnumCommandType.HALT, commander, null, null, excludeEntity, (float) capTargetHolder.getSelectDistance());
                }
                break;
            case REACH:
                for (UUID entityUUID :
                        capTargetHolder.getCommandEntities()) {
                    commandReach(commander, (Mob) serverWorld.getEntity(entityUUID), pos);
                }
                break;
            case GUARD:
                if (target != null) {
                    commandGuard(commander, target);
                }
                break;
            case GUI:
//                ScreenDragon.openGui(commander, target);
                break;
            case DEBUG:
                DragonTongue.LOGGER.debug("Debug triggered, set a breakpoint at MessageCommandEntity#232");
                break;
            default:
                break;
        }

    }

    /**
     * Command tamed entities around to attack target
     *
     * @param command
     * @param commander     Command issuer
     * @param target        Target to attack, set null to stop attacking
     * @param pos           Only used for dragons, breath at desired location
     * @param excludeEntity Indicate which entity to exclude
     * @param radius        Tamed within the radius will attack
     */
    private static void commandNearby(EnumCommandType command, LivingEntity commander, @Nullable LivingEntity target, BlockPos pos, @Nonnull Predicate<? super Entity> excludeEntity, float radius) {


        // Get entities around commander, a range cube of size radius*radius, the
        // commander is automatically excluded
        if (commander instanceof Player) {
            List<Entity> entities = commander.level.getEntities(commander,
                    (new AABB(commander.getX(), commander.getY(), commander.getZ(),
                            commander.getX() + 1.0d, commander.getY() + 1.0d, commander.getZ() + 1.0d)
                            .inflate(radius)),
                    ((Predicate<Entity>) entityGet -> !entityGet.isSpectator()
                            && entityGet.isPickable()
                            && (entityGet instanceof LivingEntity)
                            && (entityGet != target))
                            .and(excludeEntity));

            // Main.LOGGER.debug("Get "+entities.size()+" entities in "+radius);
            // Main.LOGGER.debug("Execute at "+commander.getPosition()+" within "+(new
            // AxisAlignedBB(commander.getPosX(), commander.getPosY(), commander.getPosZ(),
            // commander.getPosX() + 1.0d, commander.getPosY() + 1.0d, commander.getPosZ() +
            // 1.0d)
            // .grow(radius)).toString());

            // iteration through entities to set their target
            for (Entity tamed : entities) {
                if (tamed instanceof TamableAnimal) {
//                    ((LivingEntity) tamed).addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
                    switch (command) {
                        case ATTACK:
                            // Attack friendly, exit
                            if (!util.isOwner(target, commander)) {
                                commandAttack(commander, (Mob) tamed, target);
                            }
                            break;
                        case BREATH:
                            commandBreath(commander, (LivingEntity) tamed, pos);
                            break;
                        case HALT:
                            commandHalt(commander, (LivingEntity) tamed);
                            break;
                    }
                }
            }
        } // end if player
    }// end function commandTarget

    /**
     * Command entity attack target
     * This sets MobEntity.attackTarget to *target* if *commander* is the owner of the *tamed*
     *
     * @param commander
     * @param tamed
     * @param target
     */
    public static void commandAttack(LivingEntity commander, @Nullable Mob tamed,
                                     LivingEntity target) {
        if (!util.isOwner(tamed, commander)) {
            return;
        }

        if (tamed instanceof TamableAnimal
                && !util.isOwner(target, commander)
                && !Objects.equals(tamed.getKillCredit(), target)
                && !commander.isAlliedTo(target)) {
            util.resetGoals(tamed.targetSelector);
//            util.resetGoals(tamed.goalSelector);
            if (DragonTongue.isIafPresent && IafDragonBehaviorHelper.setDragonAttackTarget(tamed, target)) {
                // For dragons
                return;
            }
            // For vanilla creatures
//            tamed.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
//                iCapTargetHolder.setCommandStatus(CommandStatus.ATTACK);
//            });

            tamed.setTarget(target);
        }
    }

    public static void commandBreath(LivingEntity commander, @Nullable LivingEntity tamed, BlockPos pos) {
        if (!util.isOwner(tamed, commander)) {
            return;
        }

        if (DragonTongue.isIafPresent) {
            IafDragonBehaviorHelper.setDragonBreathTarget(tamed, pos);
        }
    }

    /**
     * Command tamed to stop attacking or moving
     * The command status is also removed, so they will wander
     *
     * @param commander
     * @param tamed
     */
    public static void commandHalt(LivingEntity commander, @Nullable LivingEntity tamed) {
        if (!(tamed instanceof Animal) || !util.isOwner(tamed, commander)) {
            return;
        }
        Animal animalEntity = (Animal) tamed;

        util.resetGoals(animalEntity.targetSelector);
//        util.resetGoals(animalEntity.goalSelector);
        if (DragonTongue.isIafPresent && IafHelperClass.isDragon(animalEntity)) {
            IafDragonBehaviorHelper.setDragonHalt(animalEntity);
        } else {
            animalEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setDestination(animalEntity.blockPosition());
                // If no command was issued, do as the vanilla way
                if (iCapTargetHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
                    iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
                }
            });
            animalEntity.getNavigation().stop();
            animalEntity.setTarget(null);
        }

    }

    /**
     * Command tamed to reach destination
     *
     * @param commander
     * @param tamed
     * @param pos
     */
    public static void commandReach(LivingEntity commander, Mob tamed, BlockPos pos) {
        if (!(tamed instanceof Animal) || !util.isOwner(tamed, commander)) {
            return;
        }

        Animal animalEntity = (Animal) tamed;
        BlockPos blockPos = (pos != null ? pos : animalEntity.blockPosition());

        util.resetGoals(tamed.targetSelector);
//        util.resetGoals(tamed.goalSelector);
        if (DragonTongue.isIafPresent && IafHelperClass.isDragon(animalEntity)) {
//            // If destination is too far, fly there
//            if (blockPos.getY() > commander.getPosY() + 10 || blockPos.distanceSq(animalEntity.getPosition()) > 45 * 45) {
//                if (!IafDragonBehaviorHelper.isDragonInAir(animalEntity)) {
//                    IafDragonBehaviorHelper.setDragonTakeOff(animalEntity);
//                }
//            }
//            IafDragonBehaviorHelper.setDragonFlightTarget(animalEntity, Vector3d.copyCentered(blockPos));
//            IafDragonBehaviorHelper.setDragonTargetPosition(animalEntity, blockPos);
            IafDragonBehaviorHelper.setDragonReach(animalEntity, blockPos);
        } else {
            animalEntity.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
            animalEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setDestination(pos);
                iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
            });
        }
    }

    public static void commandGuard(LivingEntity commander, LivingEntity tamed) {
        if (!(tamed instanceof Animal) || !util.isOwner(tamed, commander)) {
            return;
        }
        if (!(commander instanceof Player)) {
            return;
        }
        ICapabilityInfoHolder capTargetHolder = tamed.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tamed));
        EnumCommandSettingType.AttackDecisionType attackDecisionType = (EnumCommandSettingType.AttackDecisionType) capTargetHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE);
        Player playerEntity = (Player) commander;
        if (attackDecisionType == EnumCommandSettingType.AttackDecisionType.DEFAULT) {
            playerEntity.displayClientMessage(new TextComponent("Guard"), true);
            capTargetHolder.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.GUARD);
        } else if (attackDecisionType == EnumCommandSettingType.AttackDecisionType.GUARD) {
            playerEntity.displayClientMessage(new TextComponent("Default"), true);
            capTargetHolder.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.DEFAULT);
        }
    }

    public static void commandSit(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TamableAnimal && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 1);
            } else {
                util.setByteTag(target, "Sitting", (byte) 1);
            }
            target.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
            });
        }
    }

    public static void commandFollow(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TamableAnimal && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 2);
            } else {
                util.setByteTag(target, "Sitting", (byte) 0);
            }
            target.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
                iCapTargetHolder.setDestination(null);
            });
        }
    }

    public static void commandWonder(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TamableAnimal && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 0);
            } else {
            }
            // Dragons will have to finish its path before command actual changes, so remove the path
            ((TamableAnimal) target).getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0f);
            target.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
                if (iCapTargetHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD) {
                    iCapTargetHolder.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.DEFAULT);
                }
            });
        }
    }

    public static void commandLand(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TamableAnimal && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Flying").isPresent()) {
                util.setByteTag(target, "Flying", (byte) 0);
            }
        }
    }

    private static void loopSitting(LivingEntity commander, boolean reverse, @Nullable LivingEntity target) {
        if (target instanceof TamableAnimal && util.isOwner(target, commander)) {

            if (util.getByteTag(target, "Command").isPresent()) {
                switch (util.getByteTag(target, "Command").get()) {
                    case (byte) 0:
                        util.setByteTag(target, "Command", reverse ? (byte) 1 : 2);
                        break;
                    case 1:
                        util.setByteTag(target, "Command", reverse ? (byte) 2 : 0);
                        break;
                    case 2:
                        util.setByteTag(target, "Command", reverse ? (byte) 0 : 1);
                        break;
                    default:
                        break;
                }
            } else if (util.getByteTag(target, "Sitting").isPresent()) {
                if (util.getByteTag(target, "Sitting").get() == 1) {
                    util.setByteTag(target, "Sitting", (byte) 0);
                } else if (util.getByteTag(target, "Sitting").get() == 0) {
                    util.setByteTag(target, "Sitting", (byte) 1);
                }
            }
        }

    }


}
