package net.swofty.type.dungeonhub.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

public class ActionPlayerJoin implements HypixelEventClass {

    @SneakyThrows
    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(AsyncPlayerConfigurationEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        event.setSpawningInstance(HypixelConst.getInstanceContainer());
        Logger.info("Player " + player.getUsername() + " joined the server from origin server " + player.getOriginServer());
        player.setRespawnPoint(HypixelConst.getTypeLoader()
                .getLoaderValues()
                .spawnPosition()
                .apply(player.getOriginServer())
        );
    }
}
