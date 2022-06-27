package ublubu.shbackup.config;

import me.shedaniel.autoconfig.ConfigHolder;

public class ConfigHelper {
    public static final ConfigHelper INSTANCE = new ConfigHelper();
    private ConfigHolder<ConfigPOJO> configHolder;

    public static void updateInstance(ConfigHolder<ConfigPOJO> ch) { INSTANCE.configHolder = ch; }

    public ConfigPOJO get() { return configHolder.get(); }

    public void save() { configHolder.save(); }
}
