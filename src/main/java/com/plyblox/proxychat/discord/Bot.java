package com.plyblox.proxychat.discord;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.Config;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        message = Arrays.stream(message.split(" ")).map((originalString) -> {
            if (!originalString.startsWith("@")) return originalString;
            String name = originalString.replace("@", "");

            List<Member> potentialMembers = bot.getTextChannelById((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID)).getMembers();
            Optional<Member> potentialMember = potentialMembers
                    .stream()
                    .filter((member) -> ((member.getNickname() != null && member.getNickname().equalsIgnoreCase(name)) || member.getEffectiveName().equalsIgnoreCase(name)))
                    .findFirst();

            return potentialMember.map(IMentionable::getAsMention).orElse(originalString);
        }).collect(Collectors.joining(" "));

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
        }, 3, 3, TimeUnit.MINUTES);
    }

}
