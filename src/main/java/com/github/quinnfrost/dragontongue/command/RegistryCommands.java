package com.github.quinnfrost.dragontongue.command;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;

public class RegistryCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dragon").requires(commandSource -> commandSource.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("setting", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    return SharedSuggestionProvider.suggest(Arrays.asList("home", "breath"), builder);
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
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.asList("none", "without_blast", "any"), builder))
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
