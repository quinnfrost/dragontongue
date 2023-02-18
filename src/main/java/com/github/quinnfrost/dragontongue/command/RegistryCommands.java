package com.github.quinnfrost.dragontongue.command;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
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
import java.util.Arrays;

public class RegistryCommands {
    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("dragon").requires(commandSource -> commandSource.hasPermissionLevel(0))
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("setting", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    return ISuggestionProvider.suggest(Arrays.asList("home", "breath"), builder);
                                })
                                .then(Commands.argument("bool", BoolArgumentType.bool())
                                        .executes(context -> {
                                            Entity entity = EntityArgument.getEntity(context, "target");
                                            ICapabilityInfoHolder cap = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
                                            String str = StringArgumentType.getString(context, "setting");
                                            boolean value = BoolArgumentType.getBool(context, "bool");
                                            cap.setReturnHome(value);
                                            return 0;
                                        }))
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.asList("none", "without_blast", "any"), builder))
                                        .executes(context -> {
                                            Entity entity = EntityArgument.getEntity(context, "target");
                                            ICapabilityInfoHolder cap = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
                                            String str = StringArgumentType.getString(context, "setting");
                                            String value = StringArgumentType.getString(context, "value");
                                            if (str.equals("breath")) {
                                                switch (value) {
                                                    case "none":
                                                        cap.setObjectSetting(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.NONE);
                                                        break;
                                                    case "without_blast":
                                                        cap.setObjectSetting(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.WITHOUT_BLAST);
                                                        break;
                                                    case "any":
                                                        cap.setObjectSetting(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.ANY);
                                                        break;
                                                }

                                            }

                                            return 0;
                                        })
                                )
                        ))
        );

        // new command


    }
}
