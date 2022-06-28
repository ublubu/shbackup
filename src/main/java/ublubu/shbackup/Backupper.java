package ublubu.shbackup;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
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
        this.backup(server);
    }

    // Queues a backup task with the server.
    public void doBackup(MinecraftServer server) {
        server.send(new ServerTask(1, () -> {
            backup(server);
        }));
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
        server.getPlayerManager().saveAllPlayerData();
        server.save(false, true, true);

        var pb = new ProcessBuilder().command("sh", "-c", config.cmd).redirectErrorStream(true);
        pb.environment().put("WORLD", server.getSaveProperties().getLevelName());
        try {
            pb.start().waitFor();
        } catch (IOException | InterruptedException e) {
            ShbackupMod.LOGGER.error("running backup command", e);
            sendMessage(server, String.format("backup failed - '%s'", e.getMessage()));
        }

        enableSaving(server);
        sendMessage(server, "finished backup");
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
