package net.vadamdev.jafarbot.music;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.vadamdev.dbk.framework.DBKFramework;
import net.vadamdev.jafarbot.JafarBot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 19/05/2025
 */
public class InactivityChecker {
    private final JDA jda;
    private final GuildMusicManager musicManager;

    private final Map<String, Long> idleTimes;

    public InactivityChecker(JDA jda, GuildMusicManager musicManager) {
        this.jda = jda;
        this.musicManager = musicManager;

        this.idleTimes = new HashMap<>();

        DBKFramework.getScheduledExecutorMonoThread().scheduleAtFixedRate(this::cleanup, 0, 1, TimeUnit.SECONDS);
    }

    private void cleanup() {
        if(idleTimes.isEmpty())
            return;

        final int maxIdleTimeMs = JafarBot.get().getConfig().MAX_IDLE_TIME * 1000;
        if(maxIdleTimeMs < 1)
            return;

        for(Map.Entry<String, Long> entry : idleTimes.entrySet()) {
            final long idleTimeMs = System.currentTimeMillis() - entry.getValue();
            if(idleTimeMs <= maxIdleTimeMs)
                continue;

            final Guild guild = jda.getGuildById(entry.getKey());
            if(guild == null)
                continue; //TODO: throw error ?

            musicManager.getOrCreate(guild).tryDisconnect(guild);
        }
    }

    public void add(String guildId) {
        idleTimes.put(guildId, System.currentTimeMillis());
    }

    public boolean remove(String guildId) {
        return idleTimes.remove(guildId) != null;
    }
}
