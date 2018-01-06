package de.minestar.xmas.unit;

import de.minestar.minestarlibrary.config.MinestarConfig;
import de.minestar.minestarlibrary.utils.ConsoleUtils;
import de.minestar.xmas.XMASCore;

import java.io.File;

public class Settings {

    /* VALUES */

    private static int additionalDays;

    /* USED FOR SETTING */

    private static MinestarConfig config;
    private static File configFile;

    private Settings() {

    }

    public static boolean init(File dataFolder, String pluginName, String pluginVersion) {
        configFile = new File(dataFolder, "config.yml");
        try {
            // LOAD EXISTING CONFIG FILE
            if (configFile.exists())
                config = new MinestarConfig(configFile, pluginName, pluginVersion);
            // CREATE A DEFAUL ONE
            else
                config = MinestarConfig.copyDefault(Settings.class.getResourceAsStream("/config.yml"), configFile);

            loadValues();
            return true;

        } catch (Exception e) {
            ConsoleUtils.printException(e, XMASCore.NAME, "Can't load the settings from " + configFile);
            return false;
        }
    }

    private static void loadValues() {

        additionalDays = config.getInt("xmas.additionalDays");
        
    }

    public static int getAdditionalDays() {
        return additionalDays;
    }
}
