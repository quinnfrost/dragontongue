package com.github.quinnfrost.dragontongue.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.github.quinnfrost.dragontongue.References;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = References.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_ITEM = "item";
    public static final String CATEGORY_BOW = "bow";

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.IntValue COMMAND_ENTITIES_MAX;

    public static ForgeConfigSpec.BooleanValue TRIDENT_TELEPORT;
    public static ForgeConfigSpec.DoubleValue COMMAND_DISTANCE_MAX;
    public static ForgeConfigSpec.DoubleValue NEARBY_RANGE;

    static {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        COMMAND_ENTITIES_MAX = COMMON_BUILDER.comment("Max entities to command")
                        .defineInRange("Max command entites", 10, 1, 20);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Item settings").push(CATEGORY_ITEM);
        TRIDENT_TELEPORT = COMMON_BUILDER.comment("Do trident teleport thrower on hit").define("Trident teleport", false);
        COMMAND_DISTANCE_MAX = COMMON_BUILDER.comment("Max distance in block the entity can be targeted").defineInRange("Max command distance",256f,0,512);
        NEARBY_RANGE = COMMON_BUILDER.comment("Max distance in block the entity nearby will attack").defineInRange("Max nearby range",128f,0,512);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Bow and projectile preview settings").push(CATEGORY_BOW);

        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    private static void setupItemConfig(){

    }

    public static void loadConfig(ForgeConfigSpec spec, Path path){
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent){}

    @SubscribeEvent
    public static void onReload(final ModConfig.Reloading configEvent){}

}
