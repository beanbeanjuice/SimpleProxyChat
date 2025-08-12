package com.beanbeanjuice.simpleproxychat.common;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Scanner;

public class CommonUpdateChecker {

    private final String currentVersion;
    private final long spigotId;

    /**
     * Instantiate a new {@link CommonUpdateChecker} class.
     * @param currentVersion THe current version of the plugin.
     * @param spigotId The spigotId found on spigotmc.
     */
    public CommonUpdateChecker(final String currentVersion, final long spigotId) {
        this.currentVersion = currentVersion;
        this.spigotId = spigotId;
    }

    private Optional<String> getRemoteVersion() {
        try (InputStream is = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + spigotId + "/").toURL().openStream(); Scanner scann = new Scanner(is)) {
            if (scann.hasNext()) return Optional.of(scann.next());
        } catch (IOException | URISyntaxException ignored) { }
        return Optional.empty();
    }

    /**
     * Get the current spigot version if there is an update.
     * @return An {@link Optional}. Will be empty if current version is latest.
     */
    public Optional<String> getUpdate() {
        String remoteVersion = getRemoteVersion().orElse(null);
        if (remoteVersion == null) return Optional.empty();
        return compare(currentVersion, remoteVersion) < 0 ? Optional.of(remoteVersion) : Optional.empty();
    }

    private static int compare(final String version1, final String version2) {
        DefaultArtifactVersion v1 = new DefaultArtifactVersion(version1);
        DefaultArtifactVersion v2 = new DefaultArtifactVersion(version2);

        return v1.compareTo(v2);
    }

}
