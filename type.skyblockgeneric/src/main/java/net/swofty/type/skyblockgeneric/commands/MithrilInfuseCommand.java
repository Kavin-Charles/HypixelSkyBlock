package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.component.DataComponents;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(aliases = "mithrilinfuse",
        description = "Mithril Infuses the minion in the players hand",
        usage = "/mithrilinfuse",
        permission = Rank.ADMIN,
        allowsConsole = false)
public class MithrilInfuseCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = ((SkyBlockPlayer) sender);
            if (!(new SkyBlockItem(player.getItemInMainHand()).hasComponent(MinionComponent.class))) {
                player.sendMessage("§cMithril Infusions can only be applied to minions.");
                return;
            }
            player.updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
                try {
                    itemAttributeHandler.setMithrilInfused(true);
                    player.sendMessage("§aMithril Infusion applied to " + item.getDisplayName() + ".");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§c" + e.getMessage());
                }
            });
        });
    }
}
