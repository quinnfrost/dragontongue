package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MessageCommandEntity {
    public static BlockPos INVALID_POS = new BlockPos(0, 0.5, 0);
    private EnumCommandEntity action;
    private UUID commanderUUID;
    private UUID targetUUID;
    private BlockPos blockPos;

    // Add a command entity
    public MessageCommandEntity(EnumCommandEntity action, UUID commanderUUID, UUID addTarget) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = addTarget;
        this.blockPos = INVALID_POS;
    }

    // Command attack target
    public MessageCommandEntity(EnumCommandEntity action, UUID commanderUUID, @Nullable EntityRayTraceResult targetUUID) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = targetUUID != null ? targetUUID.getEntity().getUniqueID()
                : UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.blockPos = INVALID_POS;
    }

    public MessageCommandEntity(EnumCommandEntity action, UUID commanderUUID, @Nullable EntityRayTraceResult targetUUID, BlockRayTraceResult blockRayTraceResult) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = targetUUID != null ? targetUUID.getEntity().getUniqueID()
                : UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.blockPos = new BlockPos(blockRayTraceResult.getHitVec());
    }

    public MessageCommandEntity(EnumCommandEntity action, UUID commanderUUID, BlockRayTraceResult position) {
        this.action = action;
        this.commanderUUID = commanderUUID;
        this.targetUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.blockPos = new BlockPos(position.getHitVec());

    }


    public MessageCommandEntity(PacketBuffer buf) {
        this.action = EnumCommandEntity.valueOf(buf.readString());
        this.commanderUUID = buf.readUniqueId();
        this.targetUUID = buf.readUniqueId();
        this.blockPos = buf.readBlockPos();
        if (!buf.readBoolean() && this.blockPos.equals(INVALID_POS)) {
            blockPos = null;
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(action.name());
        buf.writeUniqueId(commanderUUID);
        buf.writeUniqueId(targetUUID);
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

            ServerWorld serverWorld = contextSupplier.get().getSender().getServerWorld();
            EnumCommandEntity action = this.action;
            LivingEntity commander = (LivingEntity) serverWorld.getEntityByUuid(this.commanderUUID);
            LivingEntity targetEntity = (LivingEntity) serverWorld.getEntityByUuid(this.targetUUID);
            BlockPos targetPos = this.blockPos;

            if (commander == null || action == null) {
                return;
            }
            if (targetEntity != null) {
                targetEntity.addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
            }

            if (targetEntity instanceof MobEntity) {
                DragonTongue.debugTarget = (MobEntity) targetEntity;
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
    public static void commandEntity(ServerWorld serverWorld, LivingEntity commander, EnumCommandEntity action,
                                     @Nullable LivingEntity target,
                                     BlockPos pos, @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) entity -> true;
        }

        switch (action) {
            case ADD:
                if (util.isOwner(target, commander)) {
                    commander.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.addCommandEntity(target.getUniqueID());
                    });
                }
                break;
            case SET:
                if (util.isOwner(target, commander)) {
                    commander.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.setCommandEntities(new ArrayList<>(Arrays.asList(target.getUniqueID())));
                    });
                }
                break;
            case REMOVE:
                if (util.isOwner(target, commander)) {
                    commander.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.removeCommandEntity(target.getUniqueID());
                    });
                    target.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.setCommandStatus(EnumCommandStatus.NONE);
                    });

                }
                break;
            case ATTACK:
                List<UUID> uuids = commander.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(commander)).getCommandEntities();
                if (!uuids.isEmpty()) {
                    for (UUID entityUUID :
                            uuids) {
                        commandAttack(commander,
                                (LivingEntity) serverWorld.getEntityByUuid(entityUUID), target, pos);
                    }
                } else {
                    commandNearby(EnumCommandEntity.ATTACK, commander, target, pos, excludeEntity, Config.NEARBY_RANGE.get().floatValue());
                }
                break;
            case NEARBY_ATTACK:
                commandNearby(EnumCommandEntity.ATTACK, commander, target, pos, excludeEntity, Config.NEARBY_RANGE.get().floatValue());
                break;
            case LOOP_STATUS:
                loopSitting(commander, target);
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
                    commandNearby(EnumCommandEntity.HALT, commander, null, null, excludeEntity, Config.NEARBY_RANGE.get().floatValue());
                }
                break;
            case REACH:
                commander.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                    for (UUID entityUUID :
                            iCapTargetHolder.getCommandEntities()) {
                        commandReach(commander, (LivingEntity) serverWorld.getEntityByUuid(entityUUID), pos);
                    }
                });
                break;
            case GUARD:
                break;
            case GUI:
//                ScreenDragon.openGui(commander, target);
                break;
            default:
//                DragonTongue.LOGGER.warn("False calling on commandEntity with type:" + action);
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
    private static void commandNearby(EnumCommandEntity command, LivingEntity commander, @Nullable LivingEntity target, BlockPos pos, @Nonnull Predicate<? super Entity> excludeEntity, float radius) {


        // Get entities around commander, a range cube of size radius*radius, the
        // commander is automatically excluded
        if (commander instanceof PlayerEntity) {
            List<Entity> entities = commander.world.getEntitiesInAABBexcluding(commander,
                    (new AxisAlignedBB(commander.getPosX(), commander.getPosY(), commander.getPosZ(),
                            commander.getPosX() + 1.0d, commander.getPosY() + 1.0d, commander.getPosZ() + 1.0d)
                            .grow(radius)),
                    ((Predicate<Entity>) entityGet -> !entityGet.isSpectator()
                            && entityGet.canBeCollidedWith()
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
                if (tamed instanceof TameableEntity) {
                    ((LivingEntity) tamed).addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
                    switch (command) {
                        case ATTACK:
                            // Attack friendly, exit
                            if (!util.isOwner(target, commander)) {
                                commandAttack(commander, (LivingEntity) tamed, target, null);
                            }
                            break;
                        case HALT:
                            commandHalt(commander,(LivingEntity) tamed);
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
     * @param pos
     */
    public static void commandAttack(LivingEntity commander, @Nullable LivingEntity tamed,
                                     LivingEntity target, BlockPos pos) {
        if (!util.isOwner(tamed, commander)) {
            return;
        }
        // If target is null and a BlockPos is specified, set dragon breath target
        if (DragonTongue.isIafPresent && target == null) {
            IafDragonBehaviorHelper.setDragonAttackTarget(tamed, null, pos);
        }

        if (tamed instanceof TameableEntity
                && !util.isOwner(target, commander)
                && !Objects.equals(tamed.getAttackingEntity(), target)
                && !commander.isOnSameTeam(target)) {
            if (DragonTongue.isIafPresent && IafDragonBehaviorHelper.setDragonAttackTarget(tamed, target, pos)) {
                // For dragons
                return;
            }
            // For vanilla creatures
//            tamed.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
//                iCapTargetHolder.setCommandStatus(EnumCommandStatus.ATTACK);
//            });
            ((TameableEntity) tamed).setAttackTarget(target);
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
        if (!(tamed instanceof AnimalEntity) || !util.isOwner(tamed, commander)) {
            return;
        }
        AnimalEntity animalEntity = (AnimalEntity) tamed;

        if (DragonTongue.isIafPresent && IafHelperClass.isDragon(animalEntity)) {
            IafDragonBehaviorHelper.setDragonHalt(animalEntity);
        } else {
            animalEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setDestination(animalEntity.getPosition());
                // If no command was issued, do as the vanilla way
                if (iCapTargetHolder.getCommandStatus() != EnumCommandStatus.NONE) {
                    iCapTargetHolder.setCommandStatus(EnumCommandStatus.REACH);
                }
//            IafDragonBehaviorHelper.setDragonFlightTarget(animalEntity, animalEntity.getPosition());
            });
//        animalEntity.getNavigator().tryMoveToXYZ(animalEntity.getPosX(), animalEntity.getPosY(), animalEntity.getPosZ(), 1.0f);
            animalEntity.getNavigator().clearPath();
            animalEntity.setAttackTarget(null);
        }

    }

    /**
     * Command tamed to reach destination
     *
     * @param commander
     * @param tamed
     * @param pos
     */
    public static void commandReach(LivingEntity commander, LivingEntity tamed, BlockPos pos) {
        if (!(tamed instanceof AnimalEntity) || !util.isOwner(tamed, commander)) {
            return;
        }

        AnimalEntity animalEntity = (AnimalEntity) tamed;
        BlockPos blockPos = (pos != null ? pos : animalEntity.getPosition());

        if (DragonTongue.isIafPresent && IafHelperClass.isDragon(animalEntity)) {
            // If destination is too far, fly there
            if (blockPos.getY() > commander.getPosY() + 1 || blockPos.distanceSq(animalEntity.getPosition()) > 30 * 30) {
                IafDragonBehaviorHelper.setDragonTakeOff(animalEntity);
            }
            IafDragonBehaviorHelper.setDragonFlightTarget(animalEntity, blockPos);
            IafDragonBehaviorHelper.setDragonWalkTarget(animalEntity, blockPos);
        } else {
            animalEntity.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
        }
        animalEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
            iCapTargetHolder.setDestination(pos);
            iCapTargetHolder.setCommandStatus(EnumCommandStatus.REACH);
        });
    }


    public static void commandSit(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 1);
            } else {
                util.setByteTag(target, "Sitting", (byte) 1);
            }
            target.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.NONE);
            });
        }
    }

    public static void commandFollow(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 2);
            } else {
                util.setByteTag(target, "Sitting", (byte) 0);
            }
            target.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.NONE);
            });
        }
    }

    public static void commandWonder(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 0);
            } else {
            }
            target.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.NONE);
            });
        }
    }

    public static void commandLand(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Flying").isPresent()) {
                util.setByteTag(target, "Flying", (byte) 0);
            }
        }
    }

    private static void loopSitting(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {

            if (util.getByteTag(target, "Command").isPresent()) {
                switch (util.getByteTag(target, "Command").get()) {
                    case (byte) 0:
                        util.setByteTag(target, "Command", (byte) 1);
                        break;
                    case 1:
                        util.setByteTag(target, "Command", (byte) 2);
                        break;
                    case 2:
                        util.setByteTag(target, "Command", (byte) 0);
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
