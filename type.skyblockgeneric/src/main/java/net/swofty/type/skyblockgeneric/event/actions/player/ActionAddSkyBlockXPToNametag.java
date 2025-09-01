package net.swofty.type.skyblockgeneric.event.actions.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionAddSkyBlockXPToNametag implements HypixelEventClass {

    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void run(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!event.isFirstSpawn()) return;

        MathUtility.delay(() -> {
            DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
            Rank rank = player.getRank();

            Team team = new TeamBuilder("ZZZZ" + player.getUsername(), MinecraftServer.getTeamManager())
                    .prefix(Component.text(
                            "§8[" + experience.getLevel().getColor() + experience.getLevel() + "§8] " +
                                    rank.getPrefix()
                    ))
                    .teamColor(rank.getTextColor())
                    .build();
            player.setTeam(team);
            player.getTeam().sendUpdatePacket();
        }, 15);
    }
}
