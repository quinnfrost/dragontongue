package com.github.quinnfrost.dragontongue.client.preview;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class RenderTrajectory {
    static HashSet<PreviewProvider> previewProviders = new HashSet(2);
    static String primaryDotColor = "75aaff";
    static String secondaryDotColor = "e7ed49";
    static double pathStart = 2.0;

    static {
        previewProviders.add(new BasicPlugin());
    }

    public static void renderTrajectory(MatrixStack matrixStack) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity playerEntity = minecraft.player;
        ParticleManager particleManager = minecraft.particles;
        World world = minecraft.world;
        ItemStack itemStack = playerEntity.getHeldItemMainhand();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty() && playerEntity.isSneaking()) {
            Class<? extends PreviewEntity> previewEntity = null;
            Iterator var8 = previewProviders.iterator();

            while (var8.hasNext()) {
                PreviewProvider previewProvider = (PreviewProvider) var8.next();
                Class<? extends PreviewEntity> cl = previewProvider.getPreviewEntityFor(playerEntity, item);
                if (cl != null) {
                    previewEntity = cl;
                    break;
                }
            }

            if (previewEntity != null) {
                try {
                    PreviewEntity<Entity> entity = (PreviewEntity) previewEntity.getConstructor(World.class).newInstance(world);
                    List<Entity> targets = entity.initializeEntities(playerEntity, itemStack);
                    if (targets != null) {
                        Iterator var29 = targets.iterator();

                        while (var29.hasNext()) {
                            Entity target = (Entity) var29.next();
                            entity = (PreviewEntity) previewEntity.getConstructor(World.class).newInstance(world);
                            Entity e = (Entity) entity;
                            e.setPosition(target.getPosX(), target.getPosY(), target.getPosZ());
                            e.setMotion(target.getMotion());
                            e.rotationYaw = target.rotationYaw;
                            e.rotationPitch = target.rotationPitch;
                            e.prevRotationPitch = target.prevRotationPitch;
                            e.prevRotationYaw = target.prevRotationYaw;
                            world.addEntity(e);
                            ArrayList<Vector3d> trajectory = new ArrayList(128);

                            for (short cycle = 0; e.isAlive(); ++cycle) {
                                entity.simulateShot(target);
                                if (cycle > 512) {
                                    break;
                                }

                                Vector3d newPoint = new Vector3d(e.getPosX(), e.getPosY(), e.getPosZ());
                                if ((double) MathHelper.sqrt(playerEntity.getDistanceSq(newPoint)) > (Double) pathStart) {
                                    trajectory.add(newPoint);
                                }
                            }

                            Color color1 = new Color(0x00D9FF);
                            Color color2 = new Color(0xFF1515);
                            Iterator var18 = trajectory.iterator();

                            while (var18.hasNext()) {
                                Vector3d vec3d = (Vector3d) var18.next();
                                double distanceFromPlayer = Math.sqrt(playerEntity.getDistanceSq(vec3d));
                                Vector3d end = (Vector3d) trajectory.get(trajectory.size() - 1);
                                double totalDistance = Math.sqrt(playerEntity.getDistanceSq(end));
                                float pointScale = (float) (distanceFromPlayer / totalDistance);

                                Particle point = particleManager.addParticle(ParticleTypes.END_ROD, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
                                if (point != null) {
                                    if (trajectory.indexOf(vec3d) % 2 == 0) {
                                        point.setColor((float) color1.getRed() / 255.0F, (float) color1.getGreen() / 255.0F, (float) color1.getBlue() / 255.0F);
                                    } else {
                                        point.setColor((float) color2.getRed() / 255.0F, (float) color2.getGreen() / 255.0F, (float) color2.getBlue() / 255.0F);
                                    }

                                    point.multiplyParticleScaleBy(pointScale / 2.5f);
                                    point.setExpired();
                                }
                            }
                        }
                    }
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                         InstantiationException var26) {
                    var26.printStackTrace();
                }
            }
        }

    }

}
