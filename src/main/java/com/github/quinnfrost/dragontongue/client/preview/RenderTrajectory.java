package com.github.quinnfrost.dragontongue.client.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
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

    public static void renderTrajectory(PoseStack matrixStack) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer playerEntity = minecraft.player;
        ParticleEngine particleManager = minecraft.particleEngine;
        Level world = minecraft.level;
        ItemStack itemStack = playerEntity.getMainHandItem();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty() && playerEntity.isShiftKeyDown()) {
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
                    PreviewEntity<Entity> entity = (PreviewEntity) previewEntity.getConstructor(Level.class).newInstance(world);
                    List<Entity> targets = entity.initializeEntities(playerEntity, itemStack);
                    if (targets != null) {
                        Iterator var29 = targets.iterator();

                        while (var29.hasNext()) {
                            Entity target = (Entity) var29.next();
                            entity = (PreviewEntity) previewEntity.getConstructor(Level.class).newInstance(world);
                            Entity e = (Entity) entity;
                            e.setPos(target.getX(), target.getY(), target.getZ());
                            e.setDeltaMovement(target.getDeltaMovement());
                            e.yRot = target.yRot;
                            e.xRot = target.xRot;
                            e.xRotO = target.xRotO;
                            e.yRotO = target.yRotO;
                            world.addFreshEntity(e);
                            ArrayList<Vec3> trajectory = new ArrayList(128);

                            for (short cycle = 0; e.isAlive(); ++cycle) {
                                entity.simulateShot(target);
                                if (cycle > 512) {
                                    break;
                                }

                                Vec3 newPoint = new Vec3(e.getX(), e.getY(), e.getZ());
                                if ((double) Mth.sqrt(playerEntity.distanceToSqr(newPoint)) > (Double) pathStart) {
                                    trajectory.add(newPoint);
                                }
                            }

//                            Color color1 = new Color(0x00D9FF);
//                            Color color2 = new Color(0xFF1515);
                            Color color1 = new Color(Integer.parseInt("00D9FF", 16));
                            Color color2 = new Color(Integer.parseInt("FF1515", 16));

                            Iterator var18 = trajectory.iterator();

                            while (var18.hasNext()) {
                                Vec3 vec3d = (Vec3) var18.next();
                                double distanceFromPlayer = Math.sqrt(playerEntity.distanceToSqr(vec3d));
                                Vec3 end = (Vec3) trajectory.get(trajectory.size() - 1);
                                double totalDistance = Math.sqrt(playerEntity.distanceToSqr(end));
                                float pointScale = (float) (distanceFromPlayer / totalDistance);

                                Particle point = particleManager.createParticle(ParticleTypes.WITCH, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
                                if (point != null) {
                                    if (trajectory.indexOf(vec3d) % 2 == 0) {
                                        point.setColor((float) color1.getRed() / 255.0F, (float) color1.getGreen() / 255.0F, (float) color1.getBlue() / 255.0F);
                                    } else {
                                        point.setColor((float) color2.getRed() / 255.0F, (float) color2.getGreen() / 255.0F, (float) color2.getBlue() / 255.0F);
                                    }

                                    point.scale(pointScale / 2.0f);
                                    point.remove();
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
