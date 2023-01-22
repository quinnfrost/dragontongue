package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.IafTestClass;
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
    private EnumCommandEntity action;
    private UUID commander;
    private UUID target;
    private BlockPos blockPos;

    // Add a command entity
    public MessageCommandEntity(EnumCommandEntity action, UUID commander, UUID addTarget) {
        this.action = action;
        this.commander = commander;
        this.target = addTarget;
        this.blockPos = new BlockPos(0, 128, 0);
    }

    // Command attack target
    public MessageCommandEntity(EnumCommandEntity action, UUID commander, @Nullable EntityRayTraceResult target) {
        this.action = action;
        this.commander = commander;
        this.target = target != null ? target.getEntity().getUniqueID()
                : UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.blockPos = new BlockPos(0, 128, 0);
    }

    public MessageCommandEntity(EnumCommandEntity action, UUID commander, BlockRayTraceResult position) {
        this.action = action;
        this.commander = commander;
        this.target = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.blockPos = new BlockPos(position.getHitVec());

    }


    public MessageCommandEntity(PacketBuffer buf) {
        this.action = EnumCommandEntity.valueOf(buf.readString());
        this.commander = buf.readUniqueId();
        this.target = buf.readUniqueId();
        this.blockPos = buf.readBlockPos();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(action.name());
        buf.writeUniqueId(commander);
        buf.writeUniqueId(target);
        buf.writeBlockPos(blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            ServerWorld serverWorld = contextSupplier.get().getSender().getServerWorld();
            EnumCommandEntity action = this.action;
            LivingEntity commander = (LivingEntity) serverWorld.getEntityByUuid(this.commander);
            LivingEntity targetEntity = (LivingEntity) serverWorld.getEntityByUuid(this.target);
            BlockPos targetPos = this.blockPos;

            if (commander == null || action == null) {
                return;
            }
            commandEntity(serverWorld, commander, action, targetEntity, targetPos, null);
        });
        return true;
    }

    public static void commandEntity(ServerWorld serverWorld, LivingEntity commander, EnumCommandEntity action,
                                     @Nullable LivingEntity target,
                                     BlockPos pos, @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) entity -> true;
        }

        switch (action) {
            case ADD:
                if (target != null) {
                    target.addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
                    if (util.isOwner(target, commander)) {
                        commander.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                            iCapabilityInfoHolder.addCommandEntity(target.getUniqueID());
                        });
                    }
                    DragonTongue.debugTarget = (MobEntity) target;

                }
                break;
            case SET:
                if (target != null) {
                    target.addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
                    if (util.isOwner(target, commander)) {
                        commander.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                            iCapabilityInfoHolder.setCommandEntity(target.getUniqueID());
                        });
                    }
                }
                break;
            case REMOVE:
                if (target != null) {
                    target.addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
                    if (util.isOwner(target, commander)) {
                        commander.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                            iCapabilityInfoHolder.removeCommandEntity(target.getUniqueID());
                        });
                        target.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                            iCapabilityInfoHolder.setCommandStatus(EnumCommandStatus.NONE);
                        });

                    }
                }
                break;
            case ATTACK:
                commander.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE)
                        .ifPresent(iCapabilityInfoHolder -> {
                            for (UUID entity :
                                    iCapabilityInfoHolder.getCommandEntities()) {
                                commandAttack(commander,
                                        (LivingEntity) serverWorld.getEntityByUuid(entity), target);
                            }
                        });
                break;
            case NEARBY_ATTACK:
                commandNearbyAttack(commander, Config.NEARBY_RANGE.get().floatValue(), target, excludeEntity);
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
                commandHalt(commander, target);
                break;
            case REACH:
                commander.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                    for (UUID entityUUID :
                            iCapabilityInfoHolder.getCommandEntities()) {
                        commandReach(commander, (LivingEntity) serverWorld.getEntityByUuid(entityUUID), pos);
                    }
                });
                break;
            case CIRCLE:
                commander.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE)
                        .ifPresent(iCapabilityInfoHolder -> {
                            for (UUID entity :
                                    iCapabilityInfoHolder.getCommandEntities()) {
                                commandCircle(commander,
                                        (LivingEntity) serverWorld.getEntityByUuid(entity), pos);
                            }
                        });
                break;
            case GUARD:
                break;
            default:
                DragonTongue.LOGGER.warn("False calling on commandEntity with type:" + action);
                break;
        }

    }

    /**
     * Command tamed entities around to attack target
     *
     * @param commander     Command issuer
     * @param radius        Tamed within the radius will attack
     * @param target        Target to attack, set null to stop attacking
     * @param excludeEntity Indicate which entity to exclude
     */
    private static void commandNearbyAttack(LivingEntity commander, float radius, @Nullable LivingEntity target,
                                            @Nonnull Predicate<? super Entity> excludeEntity) {
        // Attack friendly, exit
        if (util.isOwner(target, commander)) {
            return;
        }

        // commander.setLastAttackedEntity(target);

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
            for (Entity entity : entities) {
                if (entity instanceof TameableEntity) {
                    commandAttack(commander, (LivingEntity) entity, target);
                    ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.GLOWING, 20, 0, true, false));
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
    public static void commandAttack(LivingEntity commander, @Nullable LivingEntity tamed,
                                     @Nullable LivingEntity target) {
        if (tamed == null) {
            return;
        }
        // TODO: 不要用setAttackTarget，因为它不检查是否可以攻击
        if (target == null || (tamed instanceof TameableEntity && !util.isOwner(target, commander)
                && !Objects.equals(tamed.getAttackingEntity(), target) && !commander.isOnSameTeam(target))) {
            if (!IafTestClass.setDragonAttackTarget(tamed, target)) {
                ((TameableEntity) tamed).setAttackTarget(target);
            }
        }
    }

    public static void commandAttack(LivingEntity commander, @Nullable LivingEntity tamed,
                                     @Nullable EntityRayTraceResult entityRayTraceResult) {
        commandAttack(commander, tamed, entityRayTraceResult != null ? (LivingEntity) entityRayTraceResult.getEntity()
                : null);
    }

    public static void commandSit(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 1);
            } else {
                util.setByteTag(target, "Sitting", (byte) 1);
            }
        }
    }

    public static void commandFollow(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 2);
            } else {
                util.setByteTag(target, "Sitting", (byte) 0);
            }
        }
    }

    public static void commandWonder(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Command").isPresent()) {
                util.setByteTag(target, "Command", (byte) 0);
            } else {
            }
            target.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                iCapabilityInfoHolder.setCommandStatus(EnumCommandStatus.NONE);
            });
        }
    }

    public static void commandLand(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            if (util.getByteTag(target, "Flying").isPresent()) {
                util.setByteTag(target, "Flying", (byte) 0);
            } else {
            }
        }
    }

    public static void commandHalt(LivingEntity commander, @Nullable LivingEntity target) {
        if (target instanceof TameableEntity && util.isOwner(target, commander)) {
            AnimalEntity animalEntity = (AnimalEntity) target;
            IafTestClass.setPetReach(animalEntity,null);

        }
    }

    public static void commandReach(LivingEntity commander, LivingEntity target, @Nonnull BlockPos pos) {
        if (target == null) {
            return;
        }
        if (target instanceof AnimalEntity && util.isOwner(target, commander)) {
            AnimalEntity animalEntity = (AnimalEntity) target;
            if (pos.getY() > commander.getPosY() + 1 || pos.distanceSq(animalEntity.getPosition()) > 30 * 30) {
                IafTestClass.setDragonTakeOff(animalEntity);
            }
            IafTestClass.setPetReach(animalEntity,pos);
        }
    }

    public static void commandCircle(LivingEntity commander, @Nullable LivingEntity target, @Nonnull BlockPos pos) {
        if (target == null) {
            return;
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
