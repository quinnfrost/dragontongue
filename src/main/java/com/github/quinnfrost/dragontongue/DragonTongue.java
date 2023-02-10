package com.github.quinnfrost.dragontongue;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.github.quinnfrost.dragontongue.client.render.RenderEvent;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayRenderEvent;
import com.github.quinnfrost.dragontongue.command.RegistryCommands;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.container.RegistryContainers;
import com.github.quinnfrost.dragontongue.event.ClientEvents;
import com.github.quinnfrost.dragontongue.event.ServerEvents;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(References.MOD_ID)
@Mod.EventBusSubscriber(modid = References.MOD_ID)
public class DragonTongue
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static IEventBus eventBus;
//    public static CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static boolean isIafPresent = false;
    public static MobEntity debugTarget;
    public DragonTongue() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Test if Ice-And-Fire is installed
        if(util.isClassPresent(References.IAF_CLASS_NAME)) {
            LOGGER.info("Ice and fire mod found, let's roll.");
            isIafPresent = true;
        } else {
            LOGGER.info("Ice and fire mod not found.");
            isIafPresent = false;
        }

        // The registration to the event bus must happen in your mod class constructor
        eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.registerModContent(eventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
//        PROXY.init();
//        MinecraftForge.EVENT_BUS.register(CommonEvents.class);
        MinecraftForge.EVENT_BUS.register(ServerEvents.class);

        if (isIafPresent) {
            IafServerEvent.register(MinecraftForge.EVENT_BUS);
        }

        // Todo: more settings!
        // Todo: settings hot reload
        // Load configs
        Config.loadConfig(Config.CLIENT_CONFIG,
                FMLPaths.CONFIGDIR.get().resolve(References.CLIENT_CONFIG_NAME + "-client.toml"));
        Config.loadConfig(Config.COMMON_CONFIG,
                FMLPaths.CONFIGDIR.get().resolve(References.COMMON_CONFIG_NAME + "-common.toml"));

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
//        LOGGER.info("HELLO FROM PREINIT");
//        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        // Setup sided proxy
//        CommonProxy.commonInit();

        // Register the custom capability
        CapabilityInfoHolder.register();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
//        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);

        // Setup client proxy
//        ClientProxy.clientInit();

        KeyBindRegistry.registerKeyBind();

        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        MinecraftForge.EVENT_BUS.register(new OverlayRenderEvent(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(RenderEvent.class);


        event.enqueueWork(() -> {
            ScreenManager.registerFactory(RegistryContainers.CONTAINER_DRAGON.get(), ScreenDragon::new);
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
//        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
//        LOGGER.info("Got IMC {}", event.getIMCStream().
//                map(m->m.getMessageSupplier().get()).
//                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
//        LOGGER.info("HELLO from server starting");
        RegistryCommands.registerCommands(event.getServer().getFunctionManager().getCommandDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {

    }

    @SubscribeEvent
    public static void bakeAttributes(EntityAttributeCreationEvent creationEvent) {
        // creationEvent.put(FIRSTENTITY.get(),
        // EntityFirstEntity.setAttribute().create());
        // GlobalEntityTypeAttributes.put(FIRSTENTITY.get(),
        // EntityFirstEntity.setAttribute().create());
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        CapabilityInfoHolder.onAttachCapabilitiesEvent(event);
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
//            LOGGER.info("HELLO from Register Block");
        }
    }
}
