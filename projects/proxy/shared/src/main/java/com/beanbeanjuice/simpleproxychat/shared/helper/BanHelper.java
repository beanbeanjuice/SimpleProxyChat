package com.beanbeanjuice.simpleproxychat.shared.helper;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class BanHelper {

    private YamlDocument yamlBans;
    private final File configFolder;
    @Getter private final ArrayList<String> bannedPlayers;

    public BanHelper(File configFolder) {
        this.configFolder = configFolder;
        this.bannedPlayers = new ArrayList<>();
    }

    public void initialize() {
        try {
            yamlBans = loadBans("bannedPlayers.yml");
            yamlBans.update();
            yamlBans.save();
            readBans();
        } catch (IOException ignored) { }
    }

    public void reload() {
        try {
            yamlBans.reload();
            bannedPlayers.clear();
            readBans();
        } catch (IOException ignored) { }
    }

    public void addBan(String playerName) {
        try {
            bannedPlayers.add(playerName);

            yamlBans.set("bannedPlayers", bannedPlayers.toArray(new String[0]));
            yamlBans.save();
            this.reload();
        } catch (IOException ignored) { }
    }

    public void removeBan(String playerName) {
        try {
            bannedPlayers.removeIf((nameInArray) -> nameInArray.equalsIgnoreCase(playerName));

            yamlBans.set("bannedPlayers", bannedPlayers.toArray(new String[0]));
            yamlBans.save();
            this.reload();
        } catch (IOException ignored) { }
    }

    public boolean isBanned(String playerName) {
        return bannedPlayers.stream().anyMatch(s -> s.equalsIgnoreCase(playerName));
    }

    private void readBans() throws IOException {
        bannedPlayers.addAll(yamlBans.getStringList("bannedPlayers"));
    }

    private YamlDocument loadBans(String fileName) throws IOException {
        return YamlDocument.create(
                new File(configFolder, fileName),
                Objects.requireNonNull(getClass().getResourceAsStream("/" + fileName)),
                GeneralSettings.DEFAULT,
                DumperSettings.DEFAULT
        );
    }

}
