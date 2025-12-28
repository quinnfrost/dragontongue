package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathingStuckHandler;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.ai.*;
import com.github.quinnfrost.dragontongue.iceandfire.*;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.RegistryBrains;
import com.github.quinnfrost.dragontongue.iceandfire.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;

@Mixin(EntityDragonBase.class)
public abstract class MixinEntityDragonBase extends TamableAnimal {
    protected MixinEntityDragonBase(EntityType<? extends TamableAnimal> type, Level worldIn) {
        super(type, worldIn);
    }

    @Shadow(remap = false)
    protected int blockBreakCounter;

    @Shadow(remap = false)
    protected abstract boolean isIceInWater();

    @Shadow(remap = false)
    public abstract boolean isModelDead();

    @Shadow(remap = false)
    public abstract int getDragonStage();

    @Shadow(remap = false)
    public abstract boolean canMove();

    @Shadow(remap = false)
    public abstract boolean isFlying();

    @Shadow(remap = false)
    protected abstract int calculateDownY();

    @Shadow(remap = false)
    protected abstract boolean isBreakable(BlockPos pos, BlockState state, float hardness);

    @Shadow(remap = false)
    public abstract void updateParts();

    @Shadow(remap = false)
    public float prevDragonPitch;

    @Shadow(remap = false)
    public abstract float getDragonPitch();

    @Shadow(remap = false)
    private boolean isOverAir;

    @Shadow(remap = false)
    protected abstract boolean isOverAirLogic();

    @Shadow(remap = false)
    public IafDragonLogic logic;

    @Shadow(remap = false)
    public abstract void setBreathingFire(boolean breathing);

    @Shadow(remap = false)
    public abstract void setDragonPitch(float pitch);

    @Shadow(remap = false)
    public IafDragonFlightManager flightManager;

    @Shadow
    protected abstract boolean shouldTarget(Entity entity);

    @Shadow
    public int navigatorType;

    @Shadow
    public abstract void setFlying(boolean flying);

    @Shadow
    public abstract void setHovering(boolean hovering);

    @Shadow
    protected abstract PathingStuckHandler createStuckHandler();

    @Shadow
    protected abstract PathNavigation createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type);

    @Shadow
    protected abstract PathNavigation createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler);

    @Shadow
    public abstract Animation getAnimation();

    @Shadow
    public static Animation ANIMATION_EPIC_ROAR;

    @Shadow
    public abstract void setAnimation(Animation animation);

    @Shadow
    public abstract SoundEvent getRoarSound();

    @Shadow
    protected abstract boolean isOwnersPet(LivingEntity living);

    @Shadow
    public static Animation ANIMATION_ROAR;

    @Shadow
    public abstract boolean hasFlightClearance();

    @Shadow
    protected int flyHovering;
    @Shadow
    protected int fireTicks;
    @Shadow
    public float sleepProgress;
    @Shadow
    public static Animation ANIMATION_SHAKEPREY;

    @Shadow
    public abstract int getArmorOrdinal(ItemStack stack);

    @Shadow
    public String prevArmorResLoc;
    @Shadow
    public String armorResLoc;
    @Shadow
    public DragonType dragonType;
    @Shadow
    public double maximumHealth;
    @Shadow
    public double minimumHealth;
    @Shadow
    public double minimumDamage;
    @Shadow
    public double maximumDamage;
    @Shadow
    public double maximumSpeed;
    @Shadow
    public double maximumArmor;
    @Shadow
    public double minimumSpeed;
    @Shadow
    public double minimumArmor;
    @Shadow
    @Final
    private static UUID ARMOR_MODIFIER_UUID;

    @Shadow
    protected abstract double calculateArmorModifier();

    @Shadow
    public abstract void breakBlock();

    @Shadow
    public abstract BlockPos getRestrictCenter();

    @Shadow
    public abstract int getAgeInDays();

    @Shadow
    public boolean hasHomePosition;
    @Shadow public int flyTicks;

    @Shadow public abstract boolean isGoingDown();

    @Shadow public float flyProgress;

    @Shadow public abstract boolean isChained();

    @Shadow public abstract int getCommand();

    @Shadow public abstract boolean isHovering();

    public ICapabilityInfoHolder cap = this.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(this));

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,

            MemoryModuleType.LIVING_ENTITIES,
            MemoryModuleType.VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,

            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN

    );
    private static final ImmutableList<SensorType<? extends Sensor<? super EntityDragonBase>>> SENSOR_TYPES = ImmutableList.of(
//            RegistryBrains.SENSOR_TEST,
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS

    );


    @Inject(
            method = "<init>",
            at = @At(value = "RETURN")
    )
    public void $EntityDragonBase(EntityType t, Level world, DragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed, CallbackInfo ci) {
        this.minimumSpeed = 0.18d;
        this.maximumSpeed = 0.45d;
        this.minimumArmor = 1D;
        this.maximumArmor = 20D;

        this.sensing = new Sensing(this);

        NbtOps nbtdynamicops = NbtOps.INSTANCE;
        this.brain = this.makeBrain(new Dynamic<>(nbtdynamicops, nbtdynamicops.createMap(ImmutableMap.of(nbtdynamicops.createString("memories"), nbtdynamicops.emptyMap()))));

        this.flightManager = new IafAdvancedDragonFlightManager((EntityDragonBase) (Object) this);
        this.logic = new IafAdvancedDragonLogic((EntityDragonBase) (Object) this);

        this.setPathfindingMalus(BlockPathTypes.FENCE, 0.0F);

    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        breakBlock();

        if (this.hasHomePosition) {
            this.getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level.dimension(), this.getRestrictCenter()));
        }

        this.level.getProfiler().push("dragonBrain");
        this.getBrain().tick((ServerLevel) this.level, (EntityDragonBase) (Object) this);
        this.level.getProfiler().pop();
    }

    public Brain<EntityDragonBase> getBrain() {
        return (Brain<EntityDragonBase>) super.getBrain();
    }

    protected Brain.Provider<EntityDragonBase> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamicIn) {
        Brain<EntityDragonBase> brain = this.brainProvider().makeBrain(dynamicIn);
//        this.initBrain(brain);
        return brain;
    }

    private void initBrain(Brain<EntityDragonBase> dragonBrain) {
        dragonBrain.setSchedule(RegistryBrains.SCHEDULE_DEFAULT);

        // Core activity should be the very basic activity, handles the basic movement when the specific memory item is set
        dragonBrain.addActivity(Activity.CORE, RegistryBrains.core());
        // Other activities should only set correspond memory item base on condition
//        dragonBrain.registerActivity(RegistryBrains.ACTIVITY_DRAGON_DEFAULT, RegistryBrains.vanilla());
        dragonBrain.addActivity(RegistryBrains.ACTIVITY_IDLE, RegistryBrains.idle());
        dragonBrain.addActivity(RegistryBrains.ACTIVITY_ATTACK, RegistryBrains.attack());

        dragonBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        dragonBrain.setDefaultActivity(RegistryBrains.ACTIVITY_IDLE);
        dragonBrain.useDefaultActivity();

        dragonBrain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
    }

    @Override
    public boolean isPushable() {
        if (!super.isPushable()) {
            return false;
        }
        return this.getDragonStage() < 4;
    }

    @Override
    protected void pushEntities() {
        Vec3 oldMotion = this.getDeltaMovement();
        super.pushEntities();
        if (!isPushable()) {
            this.setDeltaMovement(oldMotion);
        }
    }

    @Override
    public boolean isInWater() {
        return super.isInWater() && this.wasEyeInWater;
//        return super.isInWater();
    }

    @Override
    public void travel(Vec3 Vector3d) {
        if (this.getAnimation() == ANIMATION_SHAKEPREY || !this.canMove() && !this.isVehicle() || this.isOrderedToSit()) {
            if (this.getNavigation().getPath() != null) {
                this.getNavigation().stop();
            }
            Vector3d = new Vec3(0, 0, 0);
        }
        super.travel(Vector3d);
    }

    public boolean useFlyingPathFinder() {
        return this.isFlying() || this.isHovering();
    }

    @Inject(
            remap = false,
            method = "bakeAttributes",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void roadblock$bakeAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(head$bakeAttributes());
        cir.cancel();
    }

    private static AttributeSupplier.Builder head$bakeAttributes() {
        return Mob.createMobAttributes()
                //HEALTH
                .add(Attributes.MAX_HEALTH, 20.0D)
                //SPEED
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                //ATTACK
                .add(Attributes.ATTACK_DAMAGE, 1)
                //FOLLOW RANGE
                .add(Attributes.FOLLOW_RANGE, Math.min(2048, IafConfig.dragonTargetSearchLength))
                //ARMOR
                .add(Attributes.ARMOR, 4);
    }

    @Inject(
            method = "registerGoals()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $registerGoals(CallbackInfo ci) {
        roadblock$registerGoals();
        ci.cancel();
    }

    public void roadblock$registerGoals() {
        this.goalSelector.addGoal(0, new DragonAIAsYouWish((EntityDragonBase) (Object) this));
        this.goalSelector.addGoal(0, new DragonAICalmLook((EntityDragonBase) (Object) this));
        this.targetSelector.addGoal(3, new DragonAIGuard<>((EntityDragonBase) (Object) this, LivingEntity.class, false, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
                return (!(entity instanceof Player) || !((Player) entity).isCreative())
                        && DragonUtils.canHostilesTarget(entity)
                        && DragonUtils.isAlive(entity)
                        && util.isHostile(entity);
            }
        }));

        this.goalSelector.addGoal(0, new DragonAIRide<>((EntityDragonBase) (Object) this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new DragonAIMate((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(3, new DragonAIReturnToRoost((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(4, new DragonAIEscort((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(5, new DragonAIAttackMelee((EntityDragonBase) (Object) this, 1.5D, false));
        this.goalSelector.addGoal(6, new AquaticAITempt(this, 1.0D, IafItemRegistry.FIRE_STEW, false));
        this.goalSelector.addGoal(7, new DragonAIWander((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(8, new DragonAIWatchClosest(this, LivingEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new DragonAILookIdle((EntityDragonBase) (Object) this));

        this.targetSelector.addGoal(1, new DragonAIOwnerTarget((EntityDragonBase) (Object) this));
        this.targetSelector.addGoal(2, new DragonAIDefendOwner((EntityDragonBase) (Object) this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new DragonAITargetNonTamed<>((EntityDragonBase) (Object) this, LivingEntity.class, false, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
//                DragonTongue.LOGGER.debug("Getting inner class instance: " + this);
//                DragonTongue.LOGGER.debug("Getting outer class instance: " + MixinEntityDragonBase.this);
                return (!(entity instanceof Player) || !((Player) entity).isCreative()) && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
            }
        }));
        this.targetSelector.addGoal(5, new DragonAITarget((EntityDragonBase) (Object) this, LivingEntity.class, true, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
                return entity instanceof LivingEntity && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
            }
        }));
        this.targetSelector.addGoal(6, new DragonAITargetItems((EntityDragonBase) (Object) this, false));
    }

    @Inject(
            remap = false,
            method = "createNavigator(Lnet/minecraft/world/World;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate$MovementType;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/PathingStuckHandler;FF)Lnet/minecraft/pathfinding/PathNavigator;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler, float width, float height, CallbackInfoReturnable<PathNavigation> cir) {
        cir.setReturnValue(roadblock$createNavigator(worldIn, type, stuckHandler, width, height));
        cir.cancel();
    }

    protected PathNavigation roadblock$createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler, float width, float height) {
        IafAdvancedDragonPathNavigator newNavigator = new IafAdvancedDragonPathNavigator((EntityDragonBase) (Object) this, level, IafAdvancedDragonPathNavigator.MovementType.valueOf(type.name()), width, height);
        this.navigation = newNavigator;
        newNavigator.setCanFloat(true);

        newNavigator.getPathingOptions().withJumpCost(0.7).withSwimCost(0.7);

        newNavigator.getNodeEvaluator().setCanOpenDoors(true);
        return newNavigator;
    }

    @Inject(
            remap = false,
            method = "switchNavigator(I)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $switchNavigator(int navigatorType, CallbackInfo ci) {
        roadblock$switchNavigator(navigatorType);
        ci.cancel();
    }

    protected void roadblock$switchNavigator(int navigatorType) {
        if (navigatorType == 0) {
            this.moveControl = new IafAdvancedDragonMoveController.GroundMoveHelper(this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.WALKING, createStuckHandler().withTeleportSteps(5));
            this.navigatorType = 0;
            this.setFlying(false);
            this.setHovering(false);
        } else if (navigatorType == 1) {
            this.moveControl = new IafAdvancedDragonMoveController.FlightMoveHelper((EntityDragonBase) (Object) this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.FLYING);
            this.navigatorType = 1;
        } else {
            this.moveControl = new IafAdvancedDragonMoveController.PlayerFlightMoveHelper<>((EntityDragonBase) (Object) this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.FLYING);
            this.navigatorType = 2;
        }
    }

    @Inject(
            method = "updateAITasks",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$updateAITasks(CallbackInfo ci) {
//        head$updateAITasks();
//        ci.cancel();
    }

    protected void head$updateAITasks() {
        super.customServerAiStep();
        breakBlock();

        this.level.getProfiler().push("dragonBrain");
        this.getBrain().tick((ServerLevel) this.level, (EntityDragonBase) (Object) this);
        this.level.getProfiler().pop();
    }

    @Inject(
            remap = false,
            method = "openInventory",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $openGUI(Player playerEntity, CallbackInfo ci) {
//        roadblock$openGUI(playerEntity);
//        ci.cancel();
    }

    public void roadblock$openGUI(Player playerEntity) {
        ContainerDragon.openGui(playerEntity, this);
    }

    @Inject(
            remap = false,
            method = "updateAttributes",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$updateAttributes(CallbackInfo ci) {
        head$updateAttributes();
        ci.cancel();
    }

    protected void head$updateAttributes() {
        prevArmorResLoc = armorResLoc;
        final int armorHead = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.HEAD));
        final int armorNeck = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.CHEST));
        final int armorLegs = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.LEGS));
        final int armorFeet = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.FEET));
        armorResLoc = dragonType.getName() + "|" + armorHead + "|" + armorNeck + "|" + armorLegs + "|" + armorFeet;
        IceAndFire.PROXY.updateDragonArmorRender(armorResLoc);
        if (this.getAgeInDays() <= 125) {
            final double healthStep = (maximumHealth - minimumHealth) / 125F;
            final double attackStep = (maximumDamage - minimumDamage) / 125F;
            final double speedStep = (maximumSpeed - minimumSpeed) / 125F;
            final double armorStep = (maximumArmor - minimumArmor) / 125F;

            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.round(minimumHealth + (healthStep * this.getAgeInDays())));
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.round(minimumDamage + (attackStep * this.getAgeInDays())));
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(minimumSpeed + (speedStep * this.getAgeInDays()));
            final double baseValue = minimumArmor + (armorStep * this.getAgeInDays());
            this.getAttribute(Attributes.ARMOR).setBaseValue(baseValue);
            if (!this.level.isClientSide) {
                this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
                this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Dragon armor bonus", calculateArmorModifier(), AttributeModifier.Operation.ADDITION));
            }
        }
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(Math.min(2048, IafConfig.dragonTargetSearchLength));
    }

    @Inject(
            remap = false,
            method = "calculateArmorModifier",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$calculateArmorModifier(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(head$calculateArmorModifier());
        cir.cancel();
    }

    private double head$calculateArmorModifier() {
        double val = 1D;
        final EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            switch (getArmorOrdinal(getItemBySlot(slot))) {
                case 1:
                    val += 2D;
                    break;
                case 2:
                case 4:
                    val += 3D;
                    break;
                case 3:
                    val += 5D;
                    break;
                case 5:
                case 6:
                case 8:
                    val += 10D;
                    break;
                case 7:
                    val += 1.5D;
                    break;
            }
        }
        return val;
    }

    @Inject(
            remap = false,
            method = "breakBlock()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $breakBlock(CallbackInfo ci) {
        roadblock$breakBlock();
        ci.cancel();
    }

    public void roadblock$breakBlock() {
        if (this.blockBreakCounter > 0 || IafConfig.dragonBreakBlockCooldown == 0) {
            --this.blockBreakCounter;
            if ((this.blockBreakCounter == 0 || IafConfig.dragonBreakBlockCooldown == 0) && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
                if (IafConfig.dragonGriefing != 2 && (!this.isTame() || IafConfig.tamedDragonGriefing)) {
                    if (!isModelDead() && this.getDragonStage() >= 3 && (this.canMove() || this.getControllingPassenger() != null)) {
                        final int bounds = 1;//(int)Math.ceil(this.getRenderSize() * 0.1);
                        final int flightModifier =
                                (isFlying() && this.getTarget() != null) ? -1 : 1;
//                        final int yMinus = calculateDownY();
                        int yMinus = calculateDownY();
                        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
                            yMinus = 0;
                        }
                        BlockPos.betweenClosedStream(
                                (int) Math.floor(this.getBoundingBox().minX) - bounds,
                                (int) Math.floor(this.getBoundingBox().minY) + yMinus,
                                (int) Math.floor(this.getBoundingBox().minZ) - bounds,
                                (int) Math.floor(this.getBoundingBox().maxX) + bounds,
                                (int) Math.floor(this.getBoundingBox().maxY) + bounds + flightModifier,
                                (int) Math.floor(this.getBoundingBox().maxZ) + bounds
                        ).forEach(pos -> {
                            if (MinecraftForge.EVENT_BUS.post(new GenericGriefEvent(this, pos.getX(), pos.getY(), pos.getZ())))
                                return;
                            final BlockState state = level.getBlockState(pos);
                            final float hardness = IafConfig.dragonGriefing == 1 || this.getDragonStage() <= 3 ? 2.0F : 5.0F;
                            if (isBreakable(pos, state, hardness)) {
                                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6F, 1, 0.6F));
                                if (!level.isClientSide) {
                                    if (random.nextFloat() <= IafConfig.dragonBlockBreakingDropChance && DragonUtils.canDropFromDragonBlockBreak(state)) {
                                        level.destroyBlock(pos, true);
                                    } else {
                                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/entity/EntityDragonBase;doesWantToLand()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$doesWantToLand(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(head$doesWantToLand());
        cir.cancel();
    }
    public boolean head$doesWantToLand() {
        return this.flyTicks > 6000 && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE && this.getCommand() == 0 || isGoingDown() || flyTicks > 40 && this.flyProgress == 0 || this.isChained() && flyTicks > 100;
    }

    @Inject(
            method = "tick()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $tick(CallbackInfo ci) {
        roadblock$tick();
        ci.cancel();
    }

    public void roadblock$tick() {
        super.tick();
        refreshDimensions();
        updateParts();
        this.prevDragonPitch = getDragonPitch();
        level.getProfiler().push("dragonLogic");

        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
            this.maxUpStep = 0.5F;
        } else {
            this.maxUpStep = Math.max(1.2F, 1.2F + (Math.min(this.getAgeInDays(), 125) - 25) * 1.8F / 100F);
        }

        isOverAir = isOverAirLogic();
        logic.updateDragonCommon();
        if (this.isModelDead()) {
            if (!level.isClientSide && level.isEmptyBlock(new BlockPos(this.getX(), this.getBoundingBox().minY, this.getZ())) && this.getY() > -1) {
                this.move(MoverType.SELF, new Vec3(0, -0.2F, 0));
            }
            this.setBreathingFire(false);

            float dragonPitch = this.getDragonPitch();
            if (dragonPitch > 0) {
                dragonPitch = Math.min(0, dragonPitch - 5);
                this.setDragonPitch(dragonPitch);
            }
            if (dragonPitch < 0) {
                this.setDragonPitch(Math.max(0, dragonPitch + 5));
            }
        } else {
            if (level.isClientSide) {
                logic.updateDragonClient();
            } else {
                logic.updateDragonServer();
                logic.updateDragonAttack();
            }
        }
        level.getProfiler().pop();
        level.getProfiler().push("dragonFlight");
        if (useFlyingPathFinder() && !level.isClientSide) {
            this.flightManager.update();
        }
        level.getProfiler().pop();
    }

    @Inject(
            remap = false,
            method = "roar()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $roar(CallbackInfo ci) {
        roadblock$roar();
        ci.cancel();
    }

    public void roadblock$roar() {
        if (EntityGorgon.isStoneMob(this) || this.isModelDead()) {
            return;
        }
        if (random.nextBoolean()) {
            if (this.getAnimation() != ANIMATION_EPIC_ROAR) {
                this.setAnimation(ANIMATION_EPIC_ROAR);
                this.playSound(this.getRoarSound(), this.getSoundVolume() + 3 + Math.max(0, this.getDragonStage() - 2), this.getVoicePitch() * 0.7F);
            }
            if (this.getDragonStage() > 3) {
                final int size = (this.getDragonStage() - 3) * 30;
                final List<Entity> entities = level.getEntities(this, this.getBoundingBox().expandTowards(size, size, size));
                for (final Entity entity : entities) {
                    final boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                    if (entity instanceof LivingEntity && !isStrongerDragon) {
                        LivingEntity living = (LivingEntity) entity;
                        if (this.isOwnedBy(living) || this.isOwnersPet(living)) {
                            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50 * size, 0, false, false));
                        } else {
                            if (living.getItemBySlot(EquipmentSlot.HEAD).getItem() != IafItemRegistry.EARPLUGS) {
                                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50 * size, 0, false, false));
                            }
                        }
                    }
                }
            }
        } else {
            if (this.getAnimation() != ANIMATION_ROAR) {
                this.setAnimation(ANIMATION_ROAR);
                this.playSound(this.getRoarSound(), this.getSoundVolume() + 2 + Math.max(0, this.getDragonStage() - 3), this.getVoicePitch());
            }
            if (this.getDragonStage() > 3) {
                final int size = (this.getDragonStage() - 3) * 30;
                final List<Entity> entities = level.getEntities(this, this.getBoundingBox().expandTowards(size, size, size));
                for (final Entity entity : entities) {
                    final boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                    if (entity instanceof LivingEntity && !isStrongerDragon) {
                        LivingEntity living = (LivingEntity) entity;
                        if (this.isOwnedBy(living) || this.isOwnersPet(living)) {
                            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * size, 0, false, false));
                        } else {
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30 * size, 0, false, false));
                        }
                    }
                }
            }
        }
    }

    @Inject(
            remap = false,
            method = "isAllowedToTriggerFlight()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $isAllowedToTriggerFlight(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(roadblock$isAllowedToTriggerFlight());
        cir.cancel();
    }

    public boolean roadblock$isAllowedToTriggerFlight() {
        return (this.hasFlightClearance() && this.onGround || this.isInWater()) && !this.isOrderedToSit() && this.getPassengers().isEmpty() && !this.isBaby() && !this.isSleeping() && this.canMove();
    }

}
