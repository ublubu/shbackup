package ublubu.shbackup;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Backupper {
    private final Config config;

    private final Lock lock; // Only one backup job at a time.

    private boolean seenPlayerSinceLastBackup = false;

    private long nextTime; // When to run the next backup

    public Backupper(Config config) {
        this.config = config;
        this.lock = new ReentrantLock();
        this.nextTime = now() + config.intervalSeconds;
    }

    public synchronized void tick(MinecraftServer server) {
        // Should we attempt a backup?
        if (now() < nextTime || !seenPlayerSinceLastBackup) {
            // No, we should not.
            seenPlayerSinceLastBackup = seenPlayerSinceLastBackup
                    || server.getPlayerManager().getCurrentPlayerCount() > 0;
            return;
        }

        // Yes, we should.
        nextTime += config.intervalSeconds;
        seenPlayerSinceLastBackup = false;
        this.doBackup(server);
    }

    public void shutdown(MinecraftServer server) {
        // Attempt the backup immediately. Queued server tasks do not run after STOPPED event.
        if (lock.tryLock()) {
            try {
                // The server just saved everything, so we only need to run the script now.
                // If we run the entire backup sequence, we will hang waiting for the (stopped) server.
                sendMessage(server, "starting backup");
                unsafe_runScript(server);
                sendMessage(server, "finished backup");
            } finally {
                lock.unlock();
            }
        } // If the last backup is still running, skip this one.
    }

    public void doBackup(MinecraftServer server) {
        // Don't do this as a ServerTask because that delays the server processing new ticks.
        new Thread(() -> backup(server)).start();
    }

    private void backup(MinecraftServer server) {
        if (lock.tryLock()) {
            try {
                unsafe_backup(server);
            } finally {
                lock.unlock();
            }
        } // If the last backup is still running, skip this one.
    }

    // This is not protected by a mutex.
    private void unsafe_backup(MinecraftServer server) {
        sendMessage(server, "starting backup");
        disableSaving(server);

        // Don't bother flushing to disk.
        // If we do it as a ServerTask, it lags the server.
        // If we do it in a separate thread, it can crash, since it's not threadsafe.
        // Worst case, we won't back up recently queued updates until later.
        //
        /* i.e. We aren't doing this:
        >> server.getPlayerManager().saveAllPlayerData();
        >> server.save(false, true, true);
         */

        unsafe_runScript(server);

        enableSaving(server);
        sendMessage(server, "finished backup");
    }

    // This is not protected by a mutex.
    private void unsafe_runScript(MinecraftServer server) {
        var pb = new ProcessBuilder().command("sh", "-c", config.cmd).redirectErrorStream(true);
        pb.environment().put("WORLD", server.getSaveProperties().getLevelName());
        try {
            pb.start().waitFor();
        } catch (IOException | InterruptedException e) {
            ShbackupMod.LOGGER.error("running backup command", e);
            sendMessage(server, String.format("backup failed - '%s'", e.getMessage()));
        }
    }

    public static void disableSaving(MinecraftServer server) {
        for (var world : server.getWorlds()) {
            if (world != null && !world.savingDisabled)
                world.savingDisabled = true;
        }
    }

    public static void enableSaving(MinecraftServer server) {
        for (var world : server.getWorlds()) {
            if (world != null && world.savingDisabled)
                world.savingDisabled = false;
        }
    }

    public static long now() {
        return Instant.now().getEpochSecond();
    }

    public static void sendMessage(MinecraftServer server, String msg) {
        var fullText = String.format("Shbackup %s: %s", Instant.now(), msg);
        server.getCommandSource().sendFeedback(Text.literal(fullText), false);
    }
}
