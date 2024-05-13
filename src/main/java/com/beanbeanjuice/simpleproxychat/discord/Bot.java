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

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Bot {

    private final Config config;
    private JDA bot;

    private final Queue<Runnable> runnables;

    public Bot(Config config) {
        this.config = config;
        runnables = new ConcurrentLinkedQueue<>();

        if (!config.getAsBoolean(ConfigDataKey.USE_DISCORD)) {
            bot = null;
            return;
        }
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
        String topicMessage = config.getAsString(ConfigDataKey.DISCORD_TOPIC_ONLINE).replace("%online%", String.valueOf(players));
        this.updateChannelTopic(topicMessage);
    }

    public Optional<JDA> getJDA() {
        return Optional.ofNullable(bot);
    }

    public void addRunnableToQueue(Runnable runnable) {
        runnables.add(runnable);
    }

    public void start() throws InterruptedException {
        bot = JDABuilder
                .createLight(config.getAsString(ConfigDataKey.BOT_TOKEN))
                .setActivity(Activity.watching("Starting Proxy..."))
                .enableCache(CacheFlag.ROLE_TAGS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build().awaitReady();

        this.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_ENABLED))
                        .setColor(Color.GREEN)
                        .build()
        );

        this.updateActivity();

        runnables.forEach(Runnable::run);
    }

    public void updateActivity() {
        this.getJDA().ifPresent((jda) -> {
            Activity.ActivityType type;
            String text;

            try {
                type = Activity.ActivityType.valueOf(config.getAsString(ConfigDataKey.BOT_ACTIVITY_TYPE));
                text = config.getAsString(ConfigDataKey.BOT_ACTIVITY_TEXT);
            } catch (Exception e) {
                type = Activity.ActivityType.WATCHING;
                text = "CONFIG ERROR";
            }
            jda.getPresence().setActivity(Activity.of(type, text));
        });
    }

    public void stop() {
        this.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_DISABLED))
                        .setColor(Color.RED)
                        .build()
        );

        this.updateChannelTopic(config.getAsString(ConfigDataKey.DISCORD_TOPIC_OFFLINE));

        this.getJDA().ifPresent((jda) -> {
            try {
                jda.shutdown();
                if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                    jda.shutdownNow(); // Cancel all remaining requests
                    jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
                }
            } catch (InterruptedException ignored) { }
        });
    }

}
