package com.github.quinnfrost.dragontongue.client.gui;

import com.github.quinnfrost.dragontongue.References;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

// TODO: 模仿GuiDragon做一个界面
public class GUITest extends Screen {
    private static final int WIDTH = 179;
    private static final int HEIGHT = 151;

    private ResourceLocation GUI = new ResourceLocation(References.MOD_ID,"textures/gui/spawner_gui.png");
    public GUITest() {
        super(new StringTextComponent("Headline"));
    }

    @Override
    protected void init() {
        int relX = (this.width - WIDTH)/2;
        int relY = (this.height-HEIGHT)/2;

        addButton(new Button(relX + 10, relY + 10, 160, 20, new StringTextComponent("Test"), button -> {
            minecraft.player.sendChatMessage("Test button");
            minecraft.displayGuiScreen(null);
        }));

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color4f(1.0F,1.0F,1.0F,1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(matrixStack,relX, relY, 0, 0, WIDTH, HEIGHT);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void open() {
        Minecraft.getInstance().displayGuiScreen(new GUITest());
    }
}
