package com.beanbeanjuice.simpleproxychathelper.fabric;

import com.beanbeanjuice.simpleproxychathelper.shared.config.Config;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProxyChatHelperFabric implements ModInitializer {

    public static final String MOD_ID = "simpleproxychathelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private Config options;

    @Override
    public void onInitialize() {
        LOGGER.info("SimpleProxyChatHelper Fabric initializing...");
        options = new Config();
        
        // Initial setup for Fabric mod.
        // Full plugin messaging and silent join/quit implementation will follow.
    }
}
