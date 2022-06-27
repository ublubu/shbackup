package ublubu.shbackup.config;

import java.time.format.DateTimeFormatter;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import ublubu.shbackup.ShbackupMod;

@Config(name = ShbackupMod.MOD_ID)
public class ConfigPOJO implements ConfigData {
    @Comment("\nShould every world have its own backup folder?\n")
    @ConfigEntry.Gui.NoTooltip()
    @ConfigEntry.Gui.Excluded
    public boolean perWorldBackup = true;

    @Comment("""
            \nTime between automatic backups in seconds
            When set to 0 backups will not be performed automatically
            """)
    @ConfigEntry.Gui.Tooltip()
    @ConfigEntry.Category("Create")
    public long backupInterval = 3600;

    @Comment("\nShould backups be done even if there are no players?\n")
    @ConfigEntry.Gui.NoTooltip()
    @ConfigEntry.Category("Create")
    public boolean doBackupsOnEmptyServer = false;

    @Comment("\nShould backup be made on server shutdown?\n")
    @ConfigEntry.Gui.NoTooltip()
    @ConfigEntry.Category("Create")
    public boolean shutdownBackup = true;

    @Comment("\nA path to the backup folder\n")
    @ConfigEntry.Gui.NoTooltip()
    public String path = "backup/";
}
