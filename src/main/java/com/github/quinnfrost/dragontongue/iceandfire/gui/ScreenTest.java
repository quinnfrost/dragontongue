package com.github.quinnfrost.dragontongue.iceandfire.gui;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class ScreenTest extends Screen {
    private final int xSize = 176;
    private final int ySize = 214;
    private int relX = (this.width - this.xSize) / 2;
    private int relY = (this.height - this.ySize) / 2;
    int startYOffset = 75;
    int lineSpacing = 9;
    private float mousePosX;
    private float mousePosY;
    private EntityDragonBase dragon;

    private ResourceLocation GUI = new ResourceLocation(References.MOD_ID, "textures/gui/spawner_gui.png");
    private static final ResourceLocation textureGuiDragon = new ResourceLocation(References.MOD_ID, "textures/gui/dragon.png");

    public ScreenTest(Entity entity) {
        super(new TextComponent("Headline"));
        if (IafHelperClass.isDragon(entity)) {
            this.dragon = (EntityDragonBase) entity;
        }
    }

    @Override
    protected void init() {
        relX = (this.width - this.xSize) / 2;
        relY = (this.height - this.ySize) / 2;

        List<Button> buttonList = new ArrayList<>();

        Button button1 = new Button(
                relX + 10,
                relY + startYOffset + lineSpacing * 10,
                160,
                20,
                new TextComponent("Test"),
                button -> {
                    minecraft.player.sendMessage(Component.nullToEmpty("Test button"), Util.NIL_UUID);
                    minecraft.player.displayClientMessage(Component.nullToEmpty("Test button"),true);
                    minecraft.setScreen(null);
                });
        button1.setAlpha(0.5f);
        addButton(button1);

    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
        relX = (this.width - this.xSize) / 2;
        relY = (this.height - this.ySize) / 2;

        this.getMinecraft().getTextureManager().bind(textureGuiDragon);
        this.blit(matrixStack, relX, relY, 0, 0, xSize, ySize);

        float dragonScale = 1F / Math.max(0.0001F, dragon.getScale());
        this.mousePosX = mouseX;
        this.mousePosY = mouseY;
        drawEntityOnScreen(relX + 88, relY + (int) (0.5F * (dragon.flyProgress)) + 55, dragonScale * 23F, relX + 51 - mousePosX, relY + 75 - 50 - mousePosY, dragon);

        Font font = this.getMinecraft().font;
        List<String> stringList = new ArrayList<>();

        stringList.add(dragon.getCustomName() == null ? translateToLocal("dragon.unnamed") : translateToLocal("dragon.name") + " " + dragon.getCustomName().getString());
        stringList.add(translateToLocal("dragon.health") + " " + Math.floor(Math.min(dragon.getHealth(), dragon.getMaxHealth())) + " / " + dragon.getMaxHealth());
        stringList.add(translateToLocal("dragon.gender") + translateToLocal((dragon.isMale() ? "dragon.gender.male" : "dragon.gender.female")));
        stringList.add(translateToLocal("dragon.hunger") + dragon.getHunger() + "/100");
        stringList.add(translateToLocal("dragon.stage") + " " + dragon.getDragonStage() + " " + translateToLocal("dragon.days.front") + dragon.getAgeInDays() + " " + translateToLocal("dragon.days.back"));
        stringList.add(dragon.getOwner() != null ? translateToLocal("dragon.owner") + dragon.getOwner().getName().getString() : translateToLocal("dragon.untamed"));

        int offset = 0;
        for (String displayString:
             stringList) {
            font.draw(matrixStack, displayString, relX + xSize / 2 - font.width(displayString) / 2, relY + startYOffset + offset, 0XFFFFFF);
            offset += lineSpacing;
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public static void drawEntityOnScreen(int posX, int posY, float scale, float mouseX, float mouseY, LivingEntity livingEntity) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float f1 = (float) Math.atan(mouseY / 40.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(posX, posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        PoseStack matrixstack = new PoseStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale(scale, scale, scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.mul(quaternion1);
        matrixstack.mulPose(quaternion);
        float f2 = livingEntity.yBodyRot;
        float f3 = livingEntity.yRot;
        float f4 = livingEntity.xRot;
        float f5 = livingEntity.yHeadRotO;
        float f6 = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + f * 20.0F;
        livingEntity.yRot = 180.0F + f * 40.0F;
        livingEntity.xRot = -f1 * 20.0F;
        livingEntity.yHeadRot = livingEntity.yRot;
        livingEntity.yHeadRotO = livingEntity.yRot;
        EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderermanager.overrideCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderermanager.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.endBatch();
        entityrenderermanager.setRenderShadow(true);
        livingEntity.yBodyRot = f2;
        livingEntity.yRot = f3;
        livingEntity.xRot = f4;
        livingEntity.yHeadRotO = f5;
        livingEntity.yHeadRot = f6;
        RenderSystem.popMatrix();
    }

    public static String translateToLocal(String s) {
        return I18n.get(s, new Object[0]);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
