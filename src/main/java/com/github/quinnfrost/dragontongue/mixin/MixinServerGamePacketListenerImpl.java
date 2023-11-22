package com.github.quinnfrost.dragontongue.mixin;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.*;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {

    @Shadow public double vehicleFirstGoodX;

    @Shadow public double vehicleFirstGoodY;

    @Shadow public double vehicleFirstGoodZ;

    @Shadow public double vehicleLastGoodX;

    @Shadow public double vehicleLastGoodY;

    @Shadow public double vehicleLastGoodZ;

    @Shadow @Final public static Logger LOGGER;

    @Inject(
            method = "handleMoveVehicle",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$handleMoveVehicle(ServerboundMoveVehiclePacket par1, CallbackInfo ci) {
        $handleMoveVehicle(par1);
        ci.cancel();
    }

    public void $handleMoveVehicle(ServerboundMoveVehiclePacket pPacket) {
        ServerGamePacketListenerImpl that = (ServerGamePacketListenerImpl)(Object) this;

        PacketUtils.ensureRunningOnSameThread(pPacket, that, that.player.getLevel());
        if (containsInvalidValues(pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getYRot(), pPacket.getXRot())) {
            that.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity entity = that.player.getRootVehicle();
            if (entity != that.player && entity.getControllingPassenger() == that.player && entity == that.lastVehicle) {
                ServerLevel serverlevel = that.player.getLevel();
                double entityX = entity.getX();
                double entityY = entity.getY();
                double entityZ = entity.getZ();
                double packetX = clampHorizontal(pPacket.getX());
                double packetY = clampVertical(pPacket.getY());
                double packetZ = clampHorizontal(pPacket.getZ());
                float packetYRot = Mth.wrapDegrees(pPacket.getYRot());
                float packetXRot = Mth.wrapDegrees(pPacket.getXRot());
                double deltaX = packetX - that.vehicleFirstGoodX;
                double deltaY = packetY - that.vehicleFirstGoodY;
                double deltaZ = packetZ - that.vehicleFirstGoodZ;
                double motionLengthSqr = entity.getDeltaMovement().lengthSqr();
                double positionDeltaLengthSqr = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                if (positionDeltaLengthSqr - motionLengthSqr > 100.0D && !that.isSingleplayerOwner()) {
                    LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), that.player.getName().getString(), deltaX, deltaY, deltaZ);
                    that.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }

                boolean isNotCollidedNow = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
                deltaX = packetX - that.vehicleLastGoodX;
                deltaY = packetY - that.vehicleLastGoodY - 1.0E-6D;
                deltaZ = packetZ - that.vehicleLastGoodZ;
                boolean verticalCollisionBelow = entity.verticalCollisionBelow;
                entity.move(MoverType.PLAYER, new Vec3(deltaX, deltaY, deltaZ));
                deltaX = packetX - entity.getX();
                deltaY = packetY - entity.getY();
                if (deltaY > -0.5D || deltaY < 0.5D) {
                    deltaY = 0.0D;
                }

                deltaZ = packetZ - entity.getZ();
                positionDeltaLengthSqr = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                boolean collisionTestFailed = false;
                if (positionDeltaLengthSqr > 0.0625D) {
                    collisionTestFailed = true;
                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), that.player.getName().getString(), Math.sqrt(positionDeltaLengthSqr));
                    LOGGER.warn(String.format("Server side (%.2f %.2f %.2f) -> (%.2f %.2f %.2f)",
                            entityX, entityY, entityZ,
                            entity.getX(), entity.getY(), entity.getZ()
                            ));
                    LOGGER.warn(String.format("Server side vehicle pos: (%.2f %.2f %.2f) Packet pos: (%.2f %.2f %.2f) Diff: %.2f",
                            entityX, entityY, entityZ,
                            packetX, packetY, packetZ,
                            deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
                            ));
                    LOGGER.warn(String.format("Deltas: %.2f %.2f %.2f",
                            deltaX, deltaY, deltaZ
                            ));
                    LOGGER.warn(String.format("Vehicle first good pos: (%.2f %.2f %.2f) Vehicle last good pos: (%.2f %.2f %.2f)",
                            vehicleFirstGoodX, vehicleFirstGoodY, vehicleFirstGoodZ,
                            vehicleLastGoodX, vehicleLastGoodY, vehicleLastGoodZ
                            ));
                    LOGGER.warn(String.format("Vehicle status: OnGround: %s CollisionBelow: %s", entity.isOnGround(), verticalCollisionBelow));
                }

                entity.absMoveTo(packetX, packetY, packetZ, packetYRot, packetXRot);
                that.player.absMoveTo(packetX, packetY, packetZ, that.player.getYRot(), that.player.getXRot()); // Forge - Resync player position on vehicle moving
                boolean isNotCollidedMoved = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
                if (isNotCollidedNow && (collisionTestFailed || !isNotCollidedMoved)) {
                    entity.absMoveTo(entityX, entityY, entityZ, packetYRot, packetXRot);
                    that.player.absMoveTo(packetX, packetY, packetZ, that.player.getYRot(), that.player.getXRot()); // Forge - Resync player position on vehicle moving
                    that.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }

                that.player.getLevel().getChunkSource().move(that.player);
                that.player.checkMovementStatistics(that.player.getX() - entityX, that.player.getY() - entityY, that.player.getZ() - entityZ);
                that.clientVehicleIsFloating = deltaY >= -0.03125D && !verticalCollisionBelow && !that.server.isFlightAllowed() && !entity.isNoGravity() && that.noBlocksAround(entity);
                that.vehicleLastGoodX = entity.getX();
                that.vehicleLastGoodY = entity.getY();
                that.vehicleLastGoodZ = entity.getZ();
            }

        }
    }
}
