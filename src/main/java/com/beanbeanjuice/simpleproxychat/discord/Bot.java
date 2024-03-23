package com.beanbeanjuice.simpleproxychat.discord;

import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Bot {

    private final Config config;
    private final JDA bot;

    public Bot(Config config) throws InterruptedException {
        this.config = config;

        if (!config.getAsBoolean(ConfigDataKey.USE_DISCORD)) {
            bot = null;
            return;
        }

        bot = JDABuilder
                .createLight(config.getAsString(ConfigDataKey.BOT_TOKEN))
                .setActivity(Activity.watching("Proxy"))
                .enableCache(CacheFlag.ROLE_TAGS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build().awaitReady();
    }

    public void sendMessage(String message) {
        if (bot == null) return;

        message = Helper.sanitize(message);

        message = Arrays.stream(message.split(" ")).map((originalString) -> {
            if (!originalString.startsWith("@")) return originalString;
            String name = originalString.replace("@", "");

            List<Member> potentialMembers = bot.getTextChannelById(config.getAsString(ConfigDataKey.CHANNEL_ID)).getMembers();
            Optional<Member> potentialMember = potentialMembers
                    .stream()
                    .filter((member) -> ((member.getNickname() != null && member.getNickname().equalsIgnoreCase(name)) || member.getEffectiveName().equalsIgnoreCase(name)))
                    .findFirst();

            return potentialMember.map(IMentionable::getAsMention).orElse(originalString);
        }).collect(Collectors.joining(" "));

        bot.getTextChannelById(config.getAsString(ConfigDataKey.CHANNEL_ID)).sendMessage(message).queue();
    }

    /**
     * Embed needs to be sanitized before running this function.
     * @param embed The {@link MessageEmbed} to send in the channel.
     */
    public void sendMessageEmbed(MessageEmbed embed) {
        if (bot == null) return;
        bot.getTextChannelById(config.getAsString(ConfigDataKey.CHANNEL_ID))
                .sendMessageEmbeds(sanitizeEmbed(embed))
                .queue();
    }

    private MessageEmbed sanitizeEmbed(MessageEmbed oldEmbed) {
        EmbedBuilder embedBuilder = new EmbedBuilder(oldEmbed);

        if (oldEmbed.getTitle() != null)
            embedBuilder.setTitle(Helper.sanitize(oldEmbed.getTitle()));

        if (oldEmbed.getAuthor() != null)
            embedBuilder.setAuthor(
                    Helper.sanitize(oldEmbed.getAuthor().getName()),
                    oldEmbed.getAuthor().getUrl(),
                    oldEmbed.getAuthor().getIconUrl()
            );

        if (oldEmbed.getDescription() != null)
            embedBuilder.setDescription(Helper.sanitize(oldEmbed.getDescription()));

        if (oldEmbed.getFooter() != null)
            embedBuilder.setFooter(
                    Helper.sanitize(oldEmbed.getFooter().getText()),
                    oldEmbed.getFooter().getIconUrl()
            );

        if (!oldEmbed.getFields().isEmpty()) {
            List<MessageEmbed.Field> fields = new ArrayList<>(oldEmbed.getFields());  // Make copy.
            embedBuilder.clearFields();  // Clear fields.

            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(
                        Helper.sanitize(field.getName()),
                        Helper.sanitize(field.getValue()),
                        field.isInline()
                );
            }
        }

        return embedBuilder.build();
    }

    public void updateChannelTopic(String topic) {
        if (bot == null) return;
        bot.getTextChannelById(config.getAsString(ConfigDataKey.CHANNEL_ID)).getManager().setTopic(topic).queue();
    }

    public void channelUpdaterFunction(int players) {
        if (bot == null) return;
        String topicMessage = String.format("There are %d players online.", players);
        updateChannelTopic(topicMessage);
    }

    public Optional<JDA> getJDA() {
        return Optional.ofNullable(bot);
    }

}
