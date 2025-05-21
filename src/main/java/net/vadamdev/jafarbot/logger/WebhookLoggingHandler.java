package net.vadamdev.jafarbot.logger;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import net.vadamdev.jafarbot.JafarBot;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 03/05/2025
 */
public class WebhookLoggingHandler {
    private WebhookClient client;
    private volatile boolean ready;

    private ScheduledExecutorService executor;

    private final Deque<WebhookEmbed> buffer;

    public WebhookLoggingHandler() {
        this.ready = false;

        this.buffer = new ConcurrentLinkedDeque<>();
    }

    public void init(String url) {
        try {
            init(new WebhookClientBuilder(url));
        }catch (Exception ignored) {
            JafarBot.getLogger().warn("Webhook logging handler failed to initialize! (Is the URL valid?)");
            ready = false;
        }
    }

    public void init(WebhookClientBuilder clientBuilder) {
        if(client != null)
            client.close();

        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "JafarBot-WebhookLogger");
            thread.setDaemon(true);
            return thread;
        });

        client = clientBuilder
                .setWait(false)
                .setExecutorService(executor)
                .build().setTimeout(30000);

        ready = true;

        executor.scheduleAtFixedRate(this::flush, 5, 5, TimeUnit.SECONDS);

        JafarBot.getLogger().info("The webhook logger has been initialized !");
    }

    private void flush() {
        if(!ready || buffer.isEmpty() || client.isShutdown())
            return;

        //Using the buffer as synchronized is probably not mandatory since new embeds will be added at the end of the queue
        //TODO: check if its actually necessary
        synchronized(buffer) {
            final List<WebhookEmbed> toSend = new ArrayList<>();
            for(int i = 0; i < 10; i++) {
                final WebhookEmbed embed = buffer.poll();
                if(embed == null)
                    break;

                toSend.add(embed);
            }

            if(!toSend.isEmpty())
                client.send(toSend);
        }
    }

    public void appendLog(WebhookEmbed embed) {
        //Discard log if the webhook client is not ready
        if(!ready)
            return;

        buffer.add(embed);
    }

    public void shutdown() {
        ready = false;
        buffer.clear();

        if(client != null) {
            client.close();
            client = null;
        }

        if(!executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }
    }

    public boolean isReady() {
        return ready;
    }
}
