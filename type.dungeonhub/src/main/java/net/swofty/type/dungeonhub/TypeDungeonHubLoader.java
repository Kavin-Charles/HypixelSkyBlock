package net.swofty.type.dungeonhub;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.proxyapi.redis.ProxyToClient;
import net.swofty.proxyapi.redis.ServiceToClient;
import net.swofty.type.dungeonhub.tab.DungeonServerModule;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.animalnpc.HypixelAnimalNPC;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.villager.HypixelVillagerNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;

import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeDungeonHubLoader implements SkyBlockTypeLoader {

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_DUNGEON_HUB;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeDungeonHubLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {

    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> switch (type) {
                    default -> new Pos(-28.5, 121, 0.5, 90, 0);
                }, // Spawn position
                false // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new SkyBlockPlayersOnlineModule(1),
                        new SkyBlockPlayersOnlineModule(2),
                        new DungeonServerModule(),
                        new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.dungeonhub.events",
                HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        List<HypixelNPC> npcs = new ArrayList<>();

        npcs.addAll(SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.dungeonhub.npcs",
                HypixelNPC.class
        ).toList());

        return npcs;
    }

    @Override
    public List<ServiceToClient> getServiceRedisListeners() {
        return List.of();
    }

    @Override
    public List<ProxyToClient> getProxyRedisListeners() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.ITEM_TRACKER, ServiceType.DATA_MUTEX);
    }

    @Override
    public List<HypixelVillagerNPC> getVillagerNPCs() {
        return new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.dungeonhub.villagers",
                HypixelVillagerNPC.class
        ).toList());
    }

    @Override
    public List<HypixelAnimalNPC> getAnimalNPCs() {
        return new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.dungeonhub.animalnpcs",
                HypixelAnimalNPC.class
        ).toList());
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.DUNGEON_HUB;
    }
}
