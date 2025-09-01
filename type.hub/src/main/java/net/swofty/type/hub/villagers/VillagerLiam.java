package net.swofty.type.hub.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.villager.NPCVillagerDialogue;
import net.swofty.type.generic.entity.villager.NPCVillagerParameters;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class VillagerLiam extends NPCVillagerDialogue {

    public VillagerLiam() {
        super(new NPCVillagerParameters() {
            @Override
            public String[] holograms() {
                return new String[]{"&fLiam", "&e&lCLICK"};
            }

            @Override
            public Pos position() {
                return new Pos(10.5,70,-41.5);
            }

            @Override
            public boolean looking() {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.NONE;
            }
        });
    }

    @Override
    public void onClick(PlayerClickVillagerNPCEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        MissionData data = player.getMissionData();
        if (data.isCurrentlyActive("speak_to_villagers")) {
            if (data.getMission("speak_to_villagers").getKey().getCustomData()
                    .values()
                    .stream()
                    .anyMatch(value -> value.toString().contains(getID()))) {
                if (System.currentTimeMillis() -
                        (long) data.getMission("speak_to_villagers").getKey().getCustomData().get("last_updated") < 30) {
                    setDialogue(player, "quest-hello");
                }
            }
        }
    }

    @Override
    public DialogueSet[] getDialogueSets() {
        return Stream.of(
                DialogueSet.builder()
                        .key("quest-hello").lines(new String[]{
                                "§e[NPC] Liam§f: Did you know you have a SkyBlock Level?",
                                "§e[NPC] Liam§f: In fact, everyone does! You can see them in the tab list by holding [TAB]!",
                                "§e[NPC] Liam§f: You can level up by playing every aspect of the game!",
                                "§e[NPC] Liam§f: If you're curious, you can view your level, and more information in your SkyBlock Menu!"

                        }).build()
        ).toArray(NPCVillagerDialogue.DialogueSet[]::new);
    }
}
