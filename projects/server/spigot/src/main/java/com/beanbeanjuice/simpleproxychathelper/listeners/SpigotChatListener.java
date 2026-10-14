package com.beanbeanjuice.simpleproxychathelper.listeners;

import com.beanbeanjuice.simpleproxychathelper.HelperTcpClient;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SpigotChatListener implements Listener {

    private final HelperTcpClient client;

    public SpigotChatListener(HelperTcpClient client) {
        this.client = client;
    }

    // NORMAL + ignoreCancelled=true:
    //   - Fires AFTER LOWEST/LOW-priority cancels from ban/mute plugins (LiteBans, AdvancedBan)
    //     and chat-filter plugins (ChatSentinel's moderation handler), so genuinely blocked
    //     messages are not relayed.
    //   - Fires AFTER chat-filter plugins that mutate the message via setMessage(...) at LOW
    //     priority, so event.getMessage() reflects the censored/filtered text rather than the
    //     original (avoids leaking content that was meant to be censored).
    //   - Fires BEFORE HIGHEST-priority re-broadcasters that cancel the event purely so they
    //     can render the chat themselves (e.g. ChatSentinel's live-delete-click feature).
    //     MONITOR + ignoreCancelled=true would silently drop every message on servers with
    //     those features enabled.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        client.sendChat(
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                event.getMessage()
        );
    }

}
