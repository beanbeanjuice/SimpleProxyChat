package com.beanbeanjuice.simpleproxychathelper.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final String currentVersion;
    private final Consumer<String> notifyFunction;

    public UpdateChecker(final String currentVersion, final Consumer<String> notifyFunction) {
        this.currentVersion = currentVersion;
        this.notifyFunction = notifyFunction;
    }

    public Optional<String> getUpdate() {
        try (InputStream is = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + 116966 + "/").toURL().openStream(); Scanner scann = new Scanner(is)) {
            if (scann.hasNext()) return Optional.of(scann.next());
        } catch (IOException | URISyntaxException ignored) { }
        return Optional.empty();
    }

    public void checkUpdate() {
        getUpdate().ifPresent((spigotVersion) -> {
            if (currentVersion.equalsIgnoreCase(spigotVersion)) return;

            String message = String.format("[SimpleProxyChat] There is an update! You are on %s. The new one is %s! %s",
                    currentVersion,
                    spigotVersion,
                    "https://www.spigotmc.org/resources/116966/");

            notifyFunction.accept(message);
        });
    }

}
