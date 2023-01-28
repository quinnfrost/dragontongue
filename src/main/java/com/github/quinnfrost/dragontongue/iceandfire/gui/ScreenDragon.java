package com.github.quinnfrost.dragontongue.iceandfire.gui;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ScreenDragon extends ContainerScreen<ContainerDragon> {
    private static final ResourceLocation textureGuiDragon = new ResourceLocation(References.MOD_ID, "textures/gui/dragon.png");
    public static EntityDragonBase referencedDragon;
    int startYOffset = 75;
    int lineSpacing = 9;
    private float mousePosX;
    private float mousePosY;


    public ScreenDragon(ContainerDragon screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.ySize = 214;
    }

    @Override
    protected void init() {
        super.init();
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;

        Button button1 = new Button(
                relX + this.xSize + 10,
                relY + startYOffset,
                20,
                20,
                new StringTextComponent("Test"),
                button -> {
                    minecraft.player.sendMessage(ITextComponent.getTextComponentOrEmpty("Test button"), Util.DUMMY_UUID);
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Test button"),true);
                    minecraft.displayGuiScreen(null);
                });
        button1.setAlpha(0.5f);
        addButton(button1);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.mousePosX = mouseX;
        this.mousePosY = mouseY;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;

        this.getMinecraft().getTextureManager().bindTexture(textureGuiDragon);
        this.blit(matrixStack, relX, relY, 0, 0, xSize, ySize);

        float dragonScale = 1F / Math.max(0.0001F, referencedDragon.getRenderScale());

        drawEntityOnScreen(relX + 88, relY + (int) (0.5F * (referencedDragon.flyProgress)) + 55, dragonScale * 23F, relX + 51 - mousePosX, relY + 75 - 50 - mousePosY, referencedDragon);

        FontRenderer font = this.getMinecraft().fontRenderer;
        List<String> stringList = new ArrayList<>();

        stringList.add(referencedDragon.getCustomName() == null ? translateToLocal("dragon.unnamed") : translateToLocal("dragon.name") + " " + referencedDragon.getCustomName().getString());
        stringList.add(translateToLocal("dragon.health") + " " + Math.floor(Math.min(referencedDragon.getHealth(), referencedDragon.getMaxHealth())) + " / " + referencedDragon.getMaxHealth());
        stringList.add(translateToLocal("dragon.gender") + translateToLocal((referencedDragon.isMale() ? "dragon.gender.male" : "dragon.gender.female")));
        stringList.add(translateToLocal("dragon.hunger") + referencedDragon.getHunger() + "/100");
        stringList.add(translateToLocal("dragon.stage") + " " + referencedDragon.getDragonStage() + " " + translateToLocal("dragon.days.front") + referencedDragon.getAgeInDays() + " " + translateToLocal("dragon.days.back"));
        stringList.add(referencedDragon.getOwner() != null ? translateToLocal("dragon.owner") + referencedDragon.getOwner().getName().getString() : translateToLocal("dragon.untamed"));

        int offset = 0;
        for (String displayString:
                stringList) {
            font.drawString(matrixStack, displayString, relX + xSize / 2 - font.getStringWidth(displayString) / 2, relY + startYOffset + offset, 0XFFFFFF);
            offset += lineSpacing;
        }
    }

    public static void drawEntityOnScreen(int posX, int posY, float scale, float mouseX, float mouseY, LivingEntity livingEntity) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float f1 = (float) Math.atan(mouseY / 40.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(posX, posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale(scale, scale, scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        float f2 = livingEntity.renderYawOffset;
        float f3 = livingEntity.rotationYaw;
        float f4 = livingEntity.rotationPitch;
        float f5 = livingEntity.prevRotationYawHead;
        float f6 = livingEntity.rotationYawHead;
        livingEntity.renderYawOffset = 180.0F + f * 20.0F;
        livingEntity.rotationYaw = 180.0F + f * 40.0F;
        livingEntity.rotationPitch = -f1 * 20.0F;
        livingEntity.rotationYawHead = livingEntity.rotationYaw;
        livingEntity.prevRotationYawHead = livingEntity.rotationYaw;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderermanager.renderEntityStatic(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        livingEntity.renderYawOffset = f2;
        livingEntity.rotationYaw = f3;
        livingEntity.rotationPitch = f4;
        livingEntity.prevRotationYawHead = f5;
        livingEntity.rotationYawHead = f6;
        RenderSystem.popMatrix();
    }

    public static String translateToLocal(String s) {
        return I18n.format(s, new Object[0]);
    }

    public static void openGui(LivingEntity player, Entity referencedDragon) {
        if (DragonTongue.isIafPresent && referencedDragon instanceof EntityDragonBase && player instanceof PlayerEntity) {
            EntityDragonBase dragon = (EntityDragonBase) referencedDragon;
            PlayerEntity playerEntity = (PlayerEntity) player;
            if (!referencedDragon.world.isRemote) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
                serverPlayerEntity.openContainer(new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return serverPlayerEntity.getDisplayName();
                    }

                    @Nullable
                    @Override
                    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                        return new ContainerDragon(
                                p_createMenu_1_,
                                p_createMenu_2_,
                                dragon.dragonInventory,
                                dragon
                        );
                    }
                });

            } else {
                ScreenDragon.referencedDragon = dragon;
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.GUI,
                        playerEntity.getUniqueID(),
                        dragon.getUniqueID()
                ));
            }
        }
    }

}
