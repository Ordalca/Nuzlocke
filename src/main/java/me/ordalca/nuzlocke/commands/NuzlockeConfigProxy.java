package me.ordalca.nuzlocke.commands;

import com.pixelmonmod.pixelmon.api.config.api.yaml.YamlConfigFactory;
import java.io.IOException;

public class NuzlockeConfigProxy {
    private static NuzlockeConfig nuzlocke;

    public static void reload() {
        try {
            nuzlocke = YamlConfigFactory.getInstance(NuzlockeConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static NuzlockeConfig getNuzlocke() { return nuzlocke; }
}