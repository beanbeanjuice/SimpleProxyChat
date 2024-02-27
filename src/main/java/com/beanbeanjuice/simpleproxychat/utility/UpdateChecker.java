package com.beanbeanjuice.simpleproxychat.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    public static void checkUpdate(Consumer<String> functionToRun) {
        try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 115305 + "/~").openStream(); Scanner scann = new Scanner(is)) {
            if (scann.hasNext()) functionToRun.accept(scann.next());
        } catch (IOException ignored) { }
    }

}
