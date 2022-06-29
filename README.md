<img src="./src/main/resources/assets/shbackup/icon.png" align="right" width="128px"/>

# Shbackup - Minecraft Backup-Script Mod

This mod runs a user-defined bash command on an interval, so you can `rdiff-backup` your world.

## Features

- Runs script on user-defined time interval.
- Runs script on server shutdown.
- Only runs script if a player has been online since the previous backup.
- Messages in chat when backup runs.
    - And an error message when it fails--_may pose a security risk if you don't trust your players._
- Doesn't force the server to flush saves to disk before the backup.
    - _Pro:_ Less lag during backups.
    - _Con:_ Recent changes may not be included until a later backup. (worst case, until server restart)
- Tiny codebase. 3 classes, ~200 LOC.

## Dependencies

https://github.com/shedaniel/cloth-config

## License

GPL v3

Use at your own risk.

## This mod was inspired by

https://github.com/Szum123321/textile_backup

https://github.com/sefodopo/FabricAutoBackup
