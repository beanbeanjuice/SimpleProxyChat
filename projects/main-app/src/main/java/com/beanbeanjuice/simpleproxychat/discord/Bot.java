package com.beanbeanjuice.simpleproxychat.discord;

import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Bot {

    private final Config config;
    private final Consumer<String> errorLogger;
    private JDA bot;

    private final Queue<Runnable> runnables;

    public Bot(final Config config, final Consumer<String> errorLogger) {
        this.config = config;
        this.errorLogger = errorLogger;
        runnables = new ConcurrentLinkedQueue<>();

        if (!config.getAsBoolean(ConfigDataKey.USE_DISCORD)) {
            bot = null;
            return;
        }

        config.addReloadListener(this::updateActivity);
        config.addReloadListener(this::updateStatus);
    }

    public void sendMessage(final String messageToSend) {
        if (bot == null) return;

        this.getBotTextChannel().ifPresentOrElse(
                (mainTextChannel) -> {
                    String message = Helper.sanitize(messageToSend);
                    message = Arrays.stream(message.split(" ")).map((originalString) -> {
                        if (!originalString.startsWith("@")) return originalString;
                        String name = originalString.replace("@", "");

                        List<Member> potentialMembers = mainTextChannel.getMembers();
                        Optional<Member> potentialMember = potentialMembers
                                .stream()
                                .filter((member) -> ((member.getNickname() != null && member.getNickname().equalsIgnoreCase(name)) || member.getEffectiveName().equalsIgnoreCase(name)))
                                .findFirst();

                        return potentialMember.map(IMentionable::getAsMention).orElse(originalString);
                    }).collect(Collectors.joining(" "));

                    mainTextChannel.sendMessage(message).queue();
                },
                () -> errorLogger.accept("There was an error sending a message to Discord. Does the channel exist? Does the bot have access to the channel?")
        );


    }

    /**
     * Embed needs to be sanitized before running this function.
     * @param embed The {@link MessageEmbed} to send in the channel.
     */
    public void sendMessageEmbed(final MessageEmbed embed) {
        if (bot == null) return;

        this.getBotTextChannel().ifPresentOrElse(
                (channel) -> channel.sendMessageEmbeds(sanitizeEmbed(embed)).queue(),
                () -> errorLogger.accept("There was an error sending a message to Discord. Does the channel exist? Does the bot have access to the channel?")
        );
    }

    public Optional<TextChannel> getBotTextChannel() {
        return Optional.ofNullable(bot.getTextChannelById(config.getAsString(ConfigDataKey.CHANNEL_ID)));
    }

    private MessageEmbed sanitizeEmbed(final MessageEmbed oldEmbed) {
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

    public void updateChannelTopic(final String topic) {
        if (bot == null) return;

        this.getBotTextChannel().ifPresentOrElse(
                (textChannel) -> textChannel.getManager().setTopic(topic).queue(),
                () -> errorLogger.accept("There was an error updating the Discord channel topic. Does the channel exist? Does the bot have access to the channel?")
        );
    }

    public void channelUpdaterFunction(final int players) {
        if (bot == null) return;
        String topicMessage = config.getAsString(ConfigDataKey.DISCORD_TOPIC_ONLINE).replace("%online%", String.valueOf(players));
        this.updateChannelTopic(topicMessage);
    }

    public Optional<JDA> getJDA() {
        return Optional.ofNullable(bot);
    }

    public void addRunnableToQueue(final Runnable runnable) {
        runnables.add(runnable);
    }

    public void start() throws InterruptedException {
        String token = config.getAsString(ConfigDataKey.BOT_TOKEN);
        if (token.isEmpty() || token.equalsIgnoreCase("TOKEN_HERE") || token.equalsIgnoreCase("null")) return;

        bot = JDABuilder
                .createLight(token)
                .setActivity(Activity.watching("Starting Proxy..."))
                .enableCache(CacheFlag.ROLE_TAGS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build().awaitReady();

        sendProxyStatus(true);

        this.updateActivity();
        this.updateStatus();

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

    public void updateStatus() {
        this.getJDA().ifPresent((jda) -> {
            OnlineStatus status;

            try {
                status = OnlineStatus.valueOf(config.getAsString(ConfigDataKey.BOT_ACTIVITY_STATUS));
            } catch (Exception e) {
                status = OnlineStatus.IDLE;
            }
            jda.getPresence().setStatus(status);
        });
    }

    public void sendProxyStatus(final boolean isStart) {
        if (!config.getAsBoolean(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED)) return;

        if (isStart) {
            this.sendMessageEmbed(
                    new EmbedBuilder()
                            .setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_ENABLED))
                            .setColor(Color.GREEN)
                            .build()
            );
        } else {
            this.sendMessageEmbed(
                    new EmbedBuilder()
                            .setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_DISABLED))
                            .setColor(Color.RED)
                            .build()
            );
        }
    }

    public void stop() {
        sendProxyStatus(false);

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
