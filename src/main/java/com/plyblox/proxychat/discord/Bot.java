package com.plyblox.proxychat.discord;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class Bot {

    private final ProxyChat plugin;
    private final JDA bot;

    public Bot(@NotNull String token, @NotNull ProxyChat plugin) throws InterruptedException {
        this.plugin = plugin;

        bot = JDABuilder
                .createLight(token)
                .setActivity(Activity.watching("Proxy"))
                .enableCache(CacheFlag.ROLE_TAGS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build().awaitReady();

        bot.addEventListener(new DiscordChatHandler(plugin));
    }

    public void sendMessage(@NotNull String message) {
        bot.getTextChannelById((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID)).sendMessage(message).queue();
    }

    public void sendMessageEmbed(@NotNull MessageEmbed embed) {
        bot.getTextChannelById((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID)).sendMessageEmbeds(embed).queue();
    }

    public void updateChannelTopic(@NotNull String topic) {
        bot.getTextChannelById((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID)).getManager().setTopic(topic).queue();
    }

    public void startChannelTopicUpdater() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            String topicMessage = String.format("There are %d players online.", plugin.getProxy().getPlayers().size());
            updateChannelTopic(topicMessage);
        }, 1, 1, TimeUnit.MINUTES);
    }

}
