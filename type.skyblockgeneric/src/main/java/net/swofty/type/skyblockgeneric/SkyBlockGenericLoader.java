package net.swofty.type.skyblockgeneric;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.swofty.commons.*;
import net.swofty.commons.item.ItemType;
import net.swofty.commons.item.attribute.ItemAttribute;
import net.swofty.commons.item.reforge.ReforgeLoader;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.data.mongodb.*;
import net.swofty.type.generic.packet.HypixelPacketClientListener;
import net.swofty.type.generic.packet.HypixelPacketServerListener;
import net.swofty.type.skyblockgeneric.block.attribute.BlockAttribute;
import net.swofty.type.skyblockgeneric.block.placement.BlockPlacementManager;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.data.monogdb.*;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.entity.ServerCrystalImpl;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.generic.entity.npc.NPCDialogue;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.villager.NPCVillagerDialogue;
import net.swofty.type.generic.entity.villager.HypixelVillagerNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.CraftableComponent;
import net.swofty.type.skyblockgeneric.item.components.MuseumComponent;
import net.swofty.type.skyblockgeneric.item.components.ServerOrbComponent;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.set.impl.SetRepeatable;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.levels.CustomLevelAward;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.unlocks.CustomLevelUnlock;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.MissionRepeater;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.museum.MuseumableItemCategory;
import net.swofty.type.skyblockgeneric.noteblock.SkyBlockSongsHandler;
import net.swofty.type.skyblockgeneric.redis.RedisAuthenticate;
import net.swofty.type.generic.redis.RedisOriginServer;
import net.swofty.type.skyblockgeneric.region.SkyBlockMiningConfiguration;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.server.attribute.SkyBlockServerAttributes;
import net.swofty.type.skyblockgeneric.server.eventcaller.CustomEventCaller;
import net.swofty.type.skyblockgeneric.user.SkyBlockIsland;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockScoreboard;
import net.swofty.type.generic.user.categories.CustomGroups;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulZone;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.utility.LaunchPads;
import net.swofty.type.generic.utility.MathUtility;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

public record SkyBlockGenericLoader(HypixelTypeLoader typeLoader) {
    @Getter
    private static MinecraftServer server;

    @SneakyThrows
    public void initialize(MinecraftServer server) {
        SkyBlockGenericLoader.server = server;
        CustomWorlds mainInstance = typeLoader.getMainInstance();

        /**
         * Setup launchpads
         */
        if (mainInstance != null) {
            LaunchPads.register(MinecraftServer.getSchedulerManager());
        }

        /**
         * Register SkyBlock databases
         */
        ConnectionString cs = new ConnectionString(Configuration.get("mongodb"));
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        MongoClient mongoClient = MongoClients.create(settings);

        RegionDatabase.connect(mongoClient);
        IslandDatabase.connect(mongoClient);
        FairySoulDatabase.connect(mongoClient);
        CoopDatabase.connect(mongoClient);
        CrystalDatabase.connect(mongoClient);

        /**
         * Register items
         */
        ItemAttribute.registerItemAttributes();
        PlayerItemUpdater.updateLoop(MinecraftServer.getSchedulerManager());
        File configDir = new File("./configuration");
        File itemsDir = new File(configDir, "items");
        try {
            List<File> yamlFiles = YamlFileUtils.getYamlFiles(itemsDir);
            Logger.info("Found " + yamlFiles.size() + " YAML files to load");
            for (File file : yamlFiles) {
                try {
                    Map<String, Object> data = YamlFileUtils.loadYaml(file);
                    List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

                    if (items != null) {
                        for (Map<String, Object> itemConfig : items) {
                            try {
                                ItemConfigParser.parseItem(itemConfig);
                            } catch (Exception e) {
                                Logger.error("Failed to parse item " + itemConfig.get("id"));
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Logger.warn("No items found in " + file.getName());
                    }
                } catch (IOException e) {
                    Logger.error("Failed to load " + file.getName(), e);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to scan for YAML files", e);
        }

        /**
         * Register commands
         */
        loopThroughPackage("net.swofty.type.skyblockgeneric.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error("Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
                e.printStackTrace();
            }
        });

        /**
         * Register SkyBlock NPCs
         */
        if (mainInstance != null) {
            loopThroughPackage("net.swofty.type.skyblockgeneric.entity.npc.npcs", HypixelNPC.class)
                    .forEach(HypixelNPC::register);
            loopThroughPackage("net.swofty.type.skyblockgeneric.entity.npc.npcs", NPCDialogue.class)
                    .forEach(HypixelNPC::register);
            loopThroughPackage("net.swofty.type.skyblockgeneric.entity.villager.villagers", HypixelVillagerNPC.class)
                    .forEach(HypixelVillagerNPC::register);
            loopThroughPackage("net.swofty.type.skyblockgeneric.entity.villager.villagers", NPCVillagerDialogue.class)
                    .forEach(HypixelVillagerNPC::register);
        }

        /**
         * Register entities
         */
        loopThroughPackage("net.swofty.type.skyblockgeneric.entity.mob.mobs", SkyBlockMob.class)
                .forEach(mob -> MobRegistry.registerExtraMob(mob.getClass()));

        MathUtility.delay(() -> SkyBlockMob.runRegionPopulators(MinecraftServer.getSchedulerManager()), 50);

        /**
         * Start generic SkyBlock tablist
         */
        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event ->
                HypixelGenericLoader.LAST_TICK.set(event.getTickMonitor()));
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        benchmarkManager.enable(Duration.ofDays(3));
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<SkyBlockPlayer> players = getLoadedPlayers();
            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= (long) 1e6; // bytes to MB
            TickMonitor tickMonitor = HypixelGenericLoader.LAST_TICK.get();
            double TPS = 1000 / tickMonitor.getTickTime();

            if (TPS < 20) {
                HypixelGenericLoader.getLoadedPlayers().forEach(player -> {
                    player.getLogHandler().debug("§cServer TPS is below 20! TPS: " + TPS);
                });
                Logger.error("Server TPS is below 20! TPS: " + TPS);
            }

            final Component header = Component.text("§bYou are playing on §e§lMC.HYPIXEL.NET")
                    .append(Component.newline())
                    .append(Component.text("§7RAM USAGE: §8" + ramUsage + " MB"))
                    .append(Component.newline())
                    .append(Component.text("§7TPS: §8" + TPS))
                    .append(Component.newline());
            final Component footer = Component.newline()
                    .append(Component.text("§a§lActive Effects"))
                    .append(Component.newline())
                    .append(Component.text("§7No effects active. Drink potions or splash them on the"))
                    .append(Component.newline())
                    .append(Component.text("§7ground to buff yourself!"))
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("§d§lCookie Buff"))
                    .append(Component.newline())
                    .append(Component.text("§7Not active! Obtain booster cookies from the community"))
                    .append(Component.newline())
                    .append(Component.text("§7shop in the hub."))
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("§aRanks, Boosters & MORE! §c§lSTORE.HYPIXEL.NET"));
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();


        /**
         * Handle server attributes
         */
        SkyBlockCalendar.tick(MinecraftServer.getSchedulerManager());
        SkyBlockServerAttributes.loadAttributes(AttributeDatabase.getDocument("attributes"));
        SkyBlockServerAttributes.saveAttributeLoop();

        /**
         * Register Placement Rules
         */
        BlockPlacementManager.registerAll();

        /**
         * Start data loop
         */
        SkyBlockDataHandler.startRepeatSetValueLoop();

        /**
         * Attempt to start the song service
         */
        boolean hasAllSongs = Arrays.stream(Songs.values()).allMatch(song -> song.getPath().toFile().exists());
        if (hasAllSongs) {
            Logger.info("All songs have been found, starting song service for this instance.");
            SkyBlockSongsHandler.isEnabled = true;
        } else {
            Logger.error("Not all songs have been found, song service will not start for this instance.");
            SkyBlockSongsHandler.isEnabled = false;
        }

        /**
         * Register packet event
         */
        loopThroughPackage("net.swofty.type.skyblockgeneric.packets.client", HypixelPacketClientListener.class)
                .forEach(HypixelPacketClientListener::cacheListener);
        loopThroughPackage("net.swofty.type.skyblockgeneric.packets.server", HypixelPacketServerListener.class)
                .forEach(HypixelPacketServerListener::cacheListener);
        HypixelPacketClientListener.register(HypixelConst.getEventHandler());
        HypixelPacketServerListener.register(HypixelConst.getEventHandler());

        /**
         * Load regions
         */
        SkyBlockRegion.cacheRegions();
        SkyBlockMiningConfiguration.startRepeater(MinecraftServer.getSchedulerManager());
        MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("skyblock:island"),
                DimensionType.builder()
                        .ambientLight(2)
                        .build());
        SkyBlockIsland.runVacantLoop(MinecraftServer.getSchedulerManager());

        /**
         * Load fairy souls
         */
        FairySoul.cacheFairySouls();

        /**
         * Initialize reforges
         */
        ReforgeLoader.loadAllReforges();

        /**
         * Create audiences
         */
        CustomGroups.registerAudiences();
        PlayerStatistics.run();

        /**
         * Start repeaters
         */
        SkyBlockScoreboard.start();
        PlayerHolograms.updateAll(MinecraftServer.getSchedulerManager());

        /**
         * Register Bazaar propagator
         */
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Thread.startVirtualThread(() -> {
                SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                    player.getBazaarConnector().processAllPendingTransactions();
                });
            });
            return TaskSchedule.seconds(15);
        });

        /**
         * Register holograms and fairy souls
         */
        if (mainInstance != null) {
            ServerHolograms.spawnAll(HypixelConst.getInstanceContainer());
            String zone = typeLoader.getType().skyblockName();
            FairySoul.spawnEntities(HypixelConst.getInstanceContainer(), FairySoulZone.valueOf(zone.toUpperCase()));
        }

        /**
         * Spawn server crystals
         */
        if (HypixelConst.getInstanceContainer() != null) {
            Thread.startVirtualThread(() -> {
                CrystalDatabase.getAllCrystals().forEach(crystal -> {
                    if (crystal.serverType != HypixelConst.getTypeLoader().getType()) return;

                    ItemType type = crystal.itemType;
                    SkyBlockItem item = new SkyBlockItem(type);
                    ServerOrbComponent asCrystal = item.getComponent(ServerOrbComponent.class);
                    ServerCrystalImpl crystalImpl = new ServerCrystalImpl(
                            asCrystal.getSpawnMaterialFunction(),
                            crystal.url,
                            asCrystal.getValidBlocks()
                    );

                    crystalImpl.setInstance(HypixelConst.getInstanceContainer(),
                            new Pos(crystal.position.x(), crystal.position.y(), crystal.position.z()));
                });
            });
        }

        /**
         * Register blocks
         */
        BlockAttribute.registerBlockAttributes();

        /**
         * Register event
         */
        loopThroughPackage("net.swofty.type.skyblockgeneric.enchantment.impl", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("net.swofty.type.skyblockgeneric.event.custom", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("net.swofty.type.skyblockgeneric.event.actions", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("net.swofty.type.skyblockgeneric.item.events", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("net.swofty.type.skyblockgeneric.mission.missions", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);

        // Register missions
        loopThroughPackage("net.swofty.type.skyblockgeneric.mission.missions", SkyBlockMission.class)
                .forEach((event) -> {
                    MissionData.registerMission(event.getClass());
                });
        loopThroughPackage("net.swofty.type.skyblockgeneric.mission.missions", MissionRepeater.class)
                .forEach((event) -> {
                    event.getTask(MinecraftServer.getSchedulerManager());
                });
        loopThroughPackage("net.swofty.type.skyblockgeneric.item.set.sets", SetRepeatable.class)
                .forEach((event) -> {
                    event.getTask(MinecraftServer.getSchedulerManager());
                });
        loopThroughPackage("net.swofty.type.skyblockgeneric.enchantment.impl", SkyBlockValueEvent.class)
                .forEach(SkyBlockValueEvent::cacheEvent);
        loopThroughPackage("net.swofty.type.skyblockgeneric.item.set.sets", SkyBlockValueEvent.class)
                .forEach(SkyBlockValueEvent::cacheEvent);
        loopThroughPackage("net.swofty.type.skyblockgeneric.item.events", SkyBlockValueEvent.class)
                .forEach(SkyBlockValueEvent::cacheEvent);
        SkyBlockValueEvent.register(); // Value events are SkyBlock-specific
        CustomEventCaller.start(); // Value events are SkyBlock-specific
        HypixelEventHandler.register(HypixelConst.getEventHandler());

        /**
         * Cache SkyBlock levels
         */
        SkyBlockLevelRequirement.loadFromYaml();

        /**
         * Cache custom collections
         */
        Thread.startVirtualThread(() -> {
            // Collection Unlocks
            CollectionCategories.getCategories().forEach(category -> {
                Arrays.stream(category.getCollections()).forEach(collection -> {
                    List<CollectionCategory.ItemCollectionReward> rewards = List.of(collection.rewards());
                    rewards.parallelStream().forEach(reward -> {
                        Arrays.stream(reward.unlocks()).forEach(unlock -> {
                            if (unlock instanceof CollectionCategory.UnlockCustomAward award) {
                                CustomCollectionAward.AWARD_CACHE.put(award.getAward(),
                                        Map.entry(collection.type(), reward.requirement()));
                            }
                        });
                    });
                });
            });

            // Level Unlocks
            Arrays.stream(SkyBlockLevelRequirement.values()).forEach(requirement -> {
                requirement.getUnlocks().forEach(unlock -> {
                    if (unlock instanceof CustomLevelUnlock award) {
                        CustomLevelAward.addToCache(requirement.asInt(), award.getAward());
                    }
                });
            });
        });

        /**
         * Load item recipes
         */
        Arrays.stream(ItemType.values()).forEach(type -> {
            SkyBlockItem item = new SkyBlockItem(type);
            if (item.hasComponent(CraftableComponent.class)) {
                CraftableComponent craftableComponent = item.getComponent(CraftableComponent.class);
                if (!craftableComponent.isDefaultCraftable()) return;

                try {
                    craftableComponent.getRecipes().forEach(SkyBlockRecipe::init);
                } catch (Exception e) {
                    Logger.error("Failed to initialize recipe for " + type.name());
                    e.printStackTrace();
                }
            }
        });
        CollectionCategories.getCategories().forEach(category -> {
            Arrays.stream(category.getCollections()).forEach(collection -> {
                List<SkyBlockRecipe<?>> recipes = new ArrayList<>();
                Arrays.stream(collection.rewards()).forEach(reward -> {
                    Arrays.stream(reward.unlocks()).forEach(unlock -> {
                        if (unlock instanceof CollectionCategory.UnlockRecipe recipe) {
                            try {
                                List<SkyBlockRecipe<?>> recipeInstances = recipe.getRecipes();

                                recipeInstances.forEach(recipeInstance -> {
                                    recipeInstance.setCanCraft((player) -> {
                                        int amount = player.getCollection().get(collection.type());
                                        return new SkyBlockRecipe.CraftingResult(
                                                amount >= reward.requirement(),
                                                new String[]{"§7You must have §c" + collection.type().getDisplayName()
                                                        + " Collection "
                                                        + StringUtility.getAsRomanNumeral(collection.getPlacementOf(reward))}
                                        );
                                    });
                                    recipes.add(recipeInstance);
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Logger.error("Failed to parse recipe " + collection.type() + " : " + reward.requirement());
                            }
                        }
                    });
                });
                recipes.forEach(SkyBlockRecipe::init);
            });
        });

        /**
         * Register Museum items
         */
        Arrays.stream(ItemType.values()).forEach(itemType -> {
            SkyBlockItem item = new SkyBlockItem(itemType);
            if (item.hasComponent(MuseumComponent.class)) {
                MuseumComponent museumComponent = item.getComponent(MuseumComponent.class);
                MuseumableItemCategory.addItem(museumComponent.getCategory(), itemType);
            }
        });

        /**
         * Register SkyBlock levels
         */
        SkyBlockLevelCause.initializeCauses();

        /**
         * Handle ConnectionManager
         */
        MinecraftServer.getConnectionManager().setPlayerProvider((gameProfile, playerConnection) -> {
            SkyBlockPlayer player = new SkyBlockPlayer(playerConnection, gameProfile);

            UUID uuid = gameProfile.getPlayer().getUuid();
            String username = gameProfile.getPlayer().getUsername();

            if (RedisOriginServer.origin.containsKey(uuid)) {
                player.setOriginServer(RedisOriginServer.origin.get(uuid));
                RedisOriginServer.origin.remove(uuid);
            }

            if (RedisAuthenticate.toAuthenticate.contains(uuid)) {
                player.setHasAuthenticated(false);
                RedisAuthenticate.toAuthenticate.remove(uuid);
            }

            Logger.info("Received new player: " + username + " (" + uuid + ")");

            return player;
        });
    }

    public static List<SkyBlockPlayer> getLoadedPlayers() {
        List<SkyBlockPlayer> players = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers()
                .stream()
                .filter(player -> {
                    try {
                        SkyBlockDataHandler.getUser(player.getUuid());
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                })
                .filter(player -> player.getInstance() != null)
                .forEach(player -> players.add((SkyBlockPlayer) player));
        return players;
    }

    public static @Nullable SkyBlockPlayer getFromUUID(UUID uuid) {
        return getLoadedPlayers().stream().filter(player -> player.getUuid().toString().equalsIgnoreCase(uuid.toString())).findFirst().orElse(null);
    }

    public static @Nullable SkyBlockPlayer getPlayerFromProfileUUID(UUID uuid) {
        return getLoadedPlayers().stream().filter(player -> player.getProfiles().getCurrentlySelected().equals(uuid)).findFirst().orElse(null);
    }

    public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);

        return subTypes.stream()
                .map(subClass -> {
                    try {
                        return clazz.cast(subClass.getDeclaredConstructor().newInstance());
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                             InvocationTargetException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull);
    }
}
