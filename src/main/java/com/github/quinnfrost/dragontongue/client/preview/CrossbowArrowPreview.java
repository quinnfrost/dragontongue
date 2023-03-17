package com.github.quinnfrost.dragontongue.client.preview;

import com.google.common.collect.Lists;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CrossbowArrowPreview extends Entity implements PreviewEntity<AbstractArrow> {
    private boolean inGround;

    public CrossbowArrowPreview(Level level) {
        super(EntityType.ARROW, level);
    }

    public List<AbstractArrow> initializeEntities(Player player, ItemStack associatedItem) {
        if (associatedItem.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(associatedItem)) {
            List<ItemStack> chargedArrows = getChargedProjectiles(associatedItem);
            if (chargedArrows.size() > 0) {
                List<AbstractArrow> arrows = new ArrayList(chargedArrows.size());

                for(int i = 0; i < chargedArrows.size(); ++i) {
                    AbstractArrow arrow = getArrow(this.level, player, associatedItem, (ItemStack)chargedArrows.get(i));
                    Vec3 vec31 = player.getUpVector(1.0F);
                    Quaternion quaternion;
                    if (i == 0) {
                        quaternion = new Quaternion(new Vector3f(vec31), 0.0F, true);
                    } else if (i == 1) {
                        quaternion = new Quaternion(new Vector3f(vec31), -10.0F, true);
                    } else {
                        quaternion = new Quaternion(new Vector3f(vec31), 10.0F, true);
                    }

                    Vec3 vec3 = player.getViewVector(1.0F);
                    Vector3f vector3f = new Vector3f(vec3);
                    vector3f.transform(quaternion);
                    arrow.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), 3.15F, 0.0F);
                    arrows.add(arrow);
                }

                return arrows;
            }
        }

        return null;
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack p_40942_) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundtag = p_40942_.getTag();
        if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9)) {
            ListTag listtag = compoundtag.getList("ChargedProjectiles", 10);

            for(int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag1 = listtag.getCompound(i);
                list.add(ItemStack.of(compoundtag1));
            }
        }

        return list;
    }

    private static AbstractArrow getArrow(Level p_40915_, LivingEntity p_40916_, ItemStack crossbow, ItemStack arrows) {
        ArrowItem arrowitem = (ArrowItem)(arrows.getItem() instanceof ArrowItem ? arrows.getItem() : Items.ARROW);
        AbstractArrow abstractarrow = arrowitem.createArrow(p_40915_, arrows, p_40916_);
        if (p_40916_ instanceof Player) {
            abstractarrow.setCritArrow(true);
        }

        abstractarrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        abstractarrow.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            abstractarrow.setPierceLevel((byte)i);
        }

        return abstractarrow;
    }

    public void simulateShot(AbstractArrow simulatedEntity) {
        super.tick();
        boolean flag = this.noPhysics;
        Vec3 vec3 = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = vec3.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2(vec3.y, d0) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        Vec3 vec33;
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                vec33 = this.position();
                Iterator var8 = voxelshape.toAabbs().iterator();

                while(var8.hasNext()) {
                    AABB aabb = (AABB)var8.next();
                    if (aabb.move(blockpos).contains(vec33)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            this.discard();
        } else {
            Vec3 vec32 = this.position();
            vec33 = vec32.add(vec3);
            HitResult hitresult = this.level.clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (((HitResult)hitresult).getType() != HitResult.Type.MISS) {
                vec33 = ((HitResult)hitresult).getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = simulatedEntity.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && ((HitResult)hitresult).getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    Entity entity1 = simulatedEntity.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && ((HitResult)hitresult).getType() != HitResult.Type.MISS && !flag) {
                    this.hasImpulse = true;
                }

                if (entityhitresult == null || simulatedEntity.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;
            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            double d4 = vec3.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d5, -d1) * 57.2957763671875));
            } else {
                this.setYRot((float)(Mth.atan2(d5, d1) * 57.2957763671875));
            }

            this.setXRot((float)(Mth.atan2(d6, d4) * 57.2957763671875));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;
            if (this.isInWater()) {
                this.discard();
            }

            this.setDeltaMovement(vec3.scale((double)f));
            if (!this.isNoGravity() && !flag) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - 0.05000000074505806, vec34.z);
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }

    }

    protected static float lerpRotation(float p_37274_, float p_37275_) {
        while(p_37275_ - p_37274_ < -180.0F) {
            p_37274_ -= 360.0F;
        }

        while(p_37275_ - p_37274_ >= 180.0F) {
            p_37274_ += 360.0F;
        }

        return Mth.lerp(0.2F, p_37274_, p_37275_);
    }

    protected void defineSynchedData() {
    }

    protected void readAdditionalSaveData(CompoundTag p_20052_) {
    }

    protected void addAdditionalSaveData(CompoundTag p_20139_) {
    }

    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

}
