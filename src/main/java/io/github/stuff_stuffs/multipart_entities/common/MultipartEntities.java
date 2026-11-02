package io.github.stuff_stuffs.multipart_entities.common;

import io.github.stuff_stuffs.multipart_entities.common.network.PlayerInteractMultipartReceiver;
import net.fabricmc.api.ModInitializer;

public class MultipartEntities implements ModInitializer {

    @Override
    public void onInitialize() {
        PlayerInteractMultipartReceiver.init();
    }
}
