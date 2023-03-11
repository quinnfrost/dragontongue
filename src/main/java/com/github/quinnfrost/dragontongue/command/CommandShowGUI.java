package com.github.quinnfrost.dragontongue.command;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class CommandShowGUI implements Command<CommandSource> {
    private static final CommandShowGUI CMD = new CommandShowGUI();
    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("showgui")
                .requires(commandSource ->
                        commandSource.hasPermissionLevel(0)
                )
                .then(Commands.argument("target", EntityArgument.entity()))
                .executes(CMD);
    }
    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(
                new StringTextComponent("Command showGUI called"),
                false
        );

        try {
            Entity entity = context.getArgument("target", Entity.class);
            if (IafHelperClass.isDragon(entity)) {
                ServerPlayerEntity serverPlayerEntity = context.getSource().asPlayer();
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
                                                                 ((EntityDragonBase) entity).inventory,
                                                                 (EntityDragonBase) entity
                                                         );
                                                     }
                                                 }

                );
            } else {
                context.getSource().sendFeedback(
                        new StringTextComponent("None dragon entity selected"),
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
