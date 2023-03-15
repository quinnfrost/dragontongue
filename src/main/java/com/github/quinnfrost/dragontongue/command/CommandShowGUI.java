package com.github.quinnfrost.dragontongue.command;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;

public class CommandShowGUI implements Command<CommandSourceStack> {
    private static final CommandShowGUI CMD = new CommandShowGUI();
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("showgui")
                .requires(commandSource ->
                        commandSource.hasPermission(0)
                )
                .then(Commands.argument("target", EntityArgument.entity()))
                .executes(CMD);
    }
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().sendSuccess(
                new TextComponent("Command showGUI called"),
                false
        );

        try {
            Entity entity = context.getArgument("target", Entity.class);
            if (IafHelperClass.isDragon(entity)) {
                ServerPlayer serverPlayerEntity = context.getSource().getPlayerOrException();
                serverPlayerEntity.openMenu(new MenuProvider() {
                                                     @Override
                                                     public Component getDisplayName() {
                                                         return serverPlayerEntity.getDisplayName();
                                                     }

                                                     @Nullable
                                                     @Override
                                                     public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
                                                         return new ContainerDragon(
                                                                 p_createMenu_1_,
                                                                 p_createMenu_2_,
                                                                 ((EntityDragonBase) entity).dragonInventory,
                                                                 (EntityDragonBase) entity
                                                         );
                                                     }
                                                 }

                );
            } else {
                context.getSource().sendSuccess(
                        new TextComponent("None dragon entity selected"),
                        false
                );
                return 1;
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

}
