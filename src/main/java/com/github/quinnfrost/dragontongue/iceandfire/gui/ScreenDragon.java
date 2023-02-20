package com.github.quinnfrost.dragontongue.iceandfire.gui;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import com.github.quinnfrost.dragontongue.event.ClientEvents;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ScreenDragon extends ContainerScreen<ContainerDragon> {
    private static final ResourceLocation textureGuiDragon = new ResourceLocation(IceAndFire.MODID, "textures/gui/dragon.png");
    public static EntityDragonBase referencedDragon;
    private static final GameSettings gameSettings = Minecraft.getInstance().gameSettings;
    private final List<Button> buttons = new ArrayList<>();
    private final float relCentralXOffset = xSize / 2;
    private final float relCentralYOffset = 75;
    private final int relRightPanelXOffset = this.xSize + 10;
    private final int relRightPanelYOffset = 5;
    private final int buttonHeight = 20;
    private final int buttonWidth = 40;
    private final int stringLineSpacing = 9;
    private float mousePosX;
    private float mousePosY;
    private ICapabilityInfoHolder cap;
    private Boolean shouldReturnRoost;
    private Boolean shouldSleep;
    private EnumCommandSettingType.CommandStatus commandStatus;
    private EnumCommandSettingType.BreathType breathType;
    private EnumCommandSettingType.DestroyType destroyType;
    private EnumCommandSettingType.AttackDecisionType attackDecisionType;
    private EnumCommandSettingType.MovementType movementType;
    private EnumCommandSettingType.GroundAttackType groundAttackType;
    private EnumCommandSettingType.AirAttackType airAttackType;


    public ScreenDragon(ContainerDragon screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
//        this.xSize = 176;
        this.ySize = 214;
    }

    @Override
    protected void init() {
        super.init();
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;

        cap = referencedDragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(referencedDragon));

        Button buttonReset = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset - 5,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(translateToLocal("dragontongue.gui.reset")),
                button -> {
                    cap.setShouldSleep(true);
                    cap.setReturnHome(true);
                    cap.setObjectSetting(EnumCommandSettingType.COMMAND_STATUS, EnumCommandSettingType.CommandStatus.NONE);
                    cap.setObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE, EnumCommandSettingType.GroundAttackType.ANY);
                    cap.setObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE, EnumCommandSettingType.AirAttackType.ANY);
                    cap.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.DEFAULT);

                    cap.setObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE, EnumCommandSettingType.MovementType.ANY);
                    cap.setObjectSetting(EnumCommandSettingType.DESTROY_TYPE, EnumCommandSettingType.DestroyType.ANY);
                    cap.setObjectSetting(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.ANY);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Reset"), true);
//                    minecraft.displayGuiScreen(null);
                });
        buttonReset.setAlpha(0.9f);
        buttons.add(buttonReset);

        Button buttonStatus = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(translateToLocal("")),
                button -> {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
                    if (ClientEvents.keySneakPressed) {
                        RegistryMessages.sendToServer(new MessageCommandEntity(
                                EnumCommandType.LOOP_STATUS_REVERSE, minecraft.player.getUniqueID(), referencedDragon.getUniqueID()
                        ));
                    } else if (ClientEvents.keySprintPressed) {
                        RegistryMessages.sendToServer(new MessageCommandEntity(
                                EnumCommandType.SIT, minecraft.player.getUniqueID(), referencedDragon.getUniqueID()
                        ));
                    } else {
                        RegistryMessages.sendToServer(new MessageCommandEntity(
                                EnumCommandType.LOOP_STATUS, minecraft.player.getUniqueID(), referencedDragon.getUniqueID()
                        ));
                    }

                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(String.valueOf(referencedDragon.getCommand())), true);
//                    minecraft.displayGuiScreen(null);
                });
        buttonStatus.setAlpha(0.9f);
        buttons.add(buttonStatus);

        Button buttonSleep = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 2,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
//                    minecraft.player.sendMessage(ITextComponent.getTextComponentOrEmpty("Test button"), Util.DUMMY_UUID);
                    shouldSleep = !shouldSleep;
                    cap.setShouldSleep(shouldSleep);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(String.valueOf(cap.getShouldSleep())), true);
//                    minecraft.displayGuiScreen(null);
                });
        buttonSleep.setAlpha(0.9f);
        buttons.add(buttonSleep);


        Button buttonRoost = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 3,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
//                    minecraft.player.sendMessage(ITextComponent.getTextComponentOrEmpty("Test button"), Util.DUMMY_UUID);
                    shouldReturnRoost = !shouldReturnRoost;
                    cap.setReturnHome(shouldReturnRoost);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(String.valueOf(cap.getReturnHome())), true);
//                    minecraft.displayGuiScreen(null);
                });
        buttonRoost.setAlpha(0.9f);
        buttons.add(buttonRoost);

        Button buttonBreath = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 4,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
                    if (ClientEvents.keySneakPressed) {
                        breathType = breathType.prev();
                    } else if (ClientEvents.keySprintPressed) {
                        breathType = EnumCommandSettingType.BreathType.ANY;
                    } else {
                        breathType = breathType.next();
                    }
                    cap.setObjectSetting(EnumCommandSettingType.BREATH_TYPE, breathType);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(cap.getObjectSetting(EnumCommandSettingType.BREATH_TYPE).toString()), true);
                });
        buttonBreath.setAlpha(0.9f);
        buttons.add(buttonBreath);

        Button buttonDestroy = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 5,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
                    if (ClientEvents.keySneakPressed) {
                        destroyType = destroyType.prev();
                    } else if (ClientEvents.keySprintPressed) {
                        destroyType = EnumCommandSettingType.DestroyType.ANY;
                    } else {
                        destroyType = destroyType.next();
                    }
                    cap.setObjectSetting(EnumCommandSettingType.DESTROY_TYPE, destroyType);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE).toString()), true);
                });
        buttonDestroy.setAlpha(0.9f);
        buttons.add(buttonDestroy);

        Button buttonMovement = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 6,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
                    if (ClientEvents.keySneakPressed) {
                        movementType = movementType.prev();
                    } else if (ClientEvents.keySprintPressed) {
                        movementType = EnumCommandSettingType.MovementType.ANY;
                    } else {
                        movementType = movementType.next();
                    }
                    cap.setObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE, movementType);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE).toString()), true);
                });
        buttonMovement.setAlpha(0.9f);
        buttons.add(buttonMovement);

        Button buttonAttackDecision = new Button(
                relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 7,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
                    if (ClientEvents.keySneakPressed) {
                        attackDecisionType = attackDecisionType.prev();
                    } else if (ClientEvents.keySprintPressed) {
                        attackDecisionType = EnumCommandSettingType.AttackDecisionType.DEFAULT;
                    } else {
                        attackDecisionType = attackDecisionType.next();
                    }
                    cap.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, attackDecisionType);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE).toString()), true);
                });
        buttonAttackDecision.setAlpha(0.9f);
        buttons.add(buttonAttackDecision);

        Button buttonGroundAttack = new Button(
        relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 8,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
                    if (ClientEvents.keySneakPressed) {
                        groundAttackType = groundAttackType.prev();
                    } else if (ClientEvents.keySprintPressed) {
                        groundAttackType = EnumCommandSettingType.GroundAttackType.ANY;
                    } else {
                        groundAttackType = groundAttackType.next();
                    }
                    cap.setObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE, groundAttackType);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(cap.getObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE).toString()), true);
                });
        buttonGroundAttack.setAlpha(0.9f);
        buttons.add(buttonGroundAttack);

        Button buttonAirAttack = new Button(
        relX + relRightPanelXOffset,
                relY + relRightPanelYOffset + buttonHeight * 9,
                buttonWidth,
                buttonHeight,
                new StringTextComponent(""),
                button -> {
                    if (ClientEvents.keySneakPressed) {
                        airAttackType = airAttackType.prev();
                    } else if (ClientEvents.keySprintPressed) {
                        airAttackType = EnumCommandSettingType.AirAttackType.ANY;
                    } else {
                        airAttackType = airAttackType.next();
                    }
                    cap.setObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE, airAttackType);
                    RegistryMessages.sendToServer(new MessageSyncCapability(referencedDragon));
                    minecraft.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(cap.getObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE).toString()), true);
                });
        buttonAirAttack.setAlpha(0.9f);
        buttons.add(buttonAirAttack);

        for (Button button :
                buttons) {
            addButton(button);
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
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
        if (referencedDragon == null) {
            return;
        }
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
        for (String displayString :
                stringList) {
            font.drawString(matrixStack, displayString, relX + relCentralXOffset - font.getStringWidth(displayString) / 2, relY + relCentralYOffset + offset, 0XFFFFFF);
            offset += stringLineSpacing;
        }

        stringList = new ArrayList<>();

        stringList.add(translateToLocal(""));
        stringList.add(translateToLocal("dragontongue.gui.command"));
        stringList.add(translateToLocal("dragontongue.gui.sleep"));
        stringList.add(translateToLocal("dragontongue.gui.return_roost"));
        stringList.add(translateToLocal("dragontongue.gui.breath"));
        stringList.add(translateToLocal("dragontongue.gui.destroy"));
        stringList.add(translateToLocal("dragontongue.gui.movement"));
        stringList.add(translateToLocal("dragontongue.gui.attack_decision"));
        stringList.add(translateToLocal("dragontongue.gui.ground_attack"));
        stringList.add(translateToLocal("dragontongue.gui.air_attack"));

        offset = 0;
        for (String displayString :
                stringList) {
            font.drawString(matrixStack, displayString, relX + relRightPanelXOffset + 40, relY + relRightPanelYOffset + offset, 0XFFFFFF);
            offset += buttonHeight;
        }

        commandStatus = cap.getCommandStatus();
        shouldSleep = cap.getShouldSleep();
        shouldReturnRoost = cap.getReturnHome();
        breathType = (EnumCommandSettingType.BreathType) cap.getObjectSetting(EnumCommandSettingType.BREATH_TYPE);
        destroyType = (EnumCommandSettingType.DestroyType) cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE);
        attackDecisionType = (EnumCommandSettingType.AttackDecisionType) cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE);
        movementType = (EnumCommandSettingType.MovementType) cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE);
        groundAttackType = (EnumCommandSettingType.GroundAttackType) cap.getObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE);
        airAttackType = (EnumCommandSettingType.AirAttackType) cap.getObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE);

        buttons.get(1).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.command." + referencedDragon.getCommand())
        ));
        buttons.get(2).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.boolean." + shouldSleep)
        ));
        buttons.get(3).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.boolean." + shouldReturnRoost)
        ));
        buttons.get(4).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.breath_type." + breathType)
        ));
        buttons.get(5).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.destroy_type." + destroyType)
        ));
        buttons.get(6).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.movement_type." + movementType)
        ));
        buttons.get(7).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.attack_decision." + attackDecisionType)
        ));
        buttons.get(8).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.ground_attack." + groundAttackType)
        ));
        buttons.get(9).setMessage(ITextComponent.getTextComponentOrEmpty(
                translateToLocal("dragontongue.gui.air_attack." + airAttackType)
        ));


    }

    // from GuiDragon#33
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
        return I18n.format(s);
    }

}
