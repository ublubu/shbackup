package ublubu.shbackup;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@me.shedaniel.autoconfig.annotation.Config(name = ShbackupMod.MOD_ID)
public class Config implements ConfigData {
    @Comment("seconds between backups")
    @ConfigEntry.Gui.Tooltip()
    @ConfigEntry.Category("Create")
    public long intervalSeconds = 900;

    @Comment("backup command")
    @ConfigEntry.Gui.NoTooltip()
    public String cmd = "echo 'backing up'";
}
