package io.github.maste.customclans.commands.subcommands;

import io.github.maste.customclans.commands.AbstractClanSubcommand;
import io.github.maste.customclans.config.MessageManager;
import io.github.maste.customclans.models.ClanInfo;
import io.github.maste.customclans.services.ClanService;
import io.github.maste.customclans.util.MiniMessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class InfoSubcommand extends AbstractClanSubcommand {

    private final ClanService clanService;
    private final io.github.maste.customclans.config.PluginConfig pluginConfig;

    public InfoSubcommand(
            JavaPlugin plugin,
            MessageManager messages,
            ClanService clanService,
            io.github.maste.customclans.config.PluginConfig pluginConfig
    ) {
        super(plugin, messages, "info", "clans.use", true);
        this.clanService = clanService;
        this.pluginConfig = pluginConfig;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        handleAction(sender, clanService.getClanInfo(asPlayer(sender)), result -> {
            ClanInfo clanInfo = result.value();
            Player player = asPlayer(sender);
            long onlineCount = clanInfo.members().stream()
                    .filter(member -> plugin.getServer().getPlayer(member.playerUuid()) != null)
                    .count();

            messages.send(sender, "info.header");
            messages.sendList(
                    sender,
                    "info.lines",
                    Placeholder.unparsed("name", clanInfo.clan().name()),
                    Placeholder.unparsed("tag", clanInfo.clan().tag()),
                    Placeholder.unparsed("color", pluginConfig.formatColorDisplayName(clanInfo.clan().tagColor())),
                    Placeholder.unparsed("president", clanInfo.presidentName()),
                    Placeholder.unparsed("member_count", String.valueOf(clanInfo.members().size())),
                    Placeholder.unparsed("max_members", String.valueOf(pluginConfig.maxClanSize())),
                    Placeholder.unparsed("online_count", String.valueOf(onlineCount)),
                    MiniMessageUtil.placeholders(result.placeholders())
            );
        });
    }
}
