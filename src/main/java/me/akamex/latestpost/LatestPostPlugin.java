package me.akamex.latestpost;

import me.akamex.latestpost.config.Messages;
import me.akamex.latestpost.user.DatabaseUserService;
import me.akamex.latestpost.user.UserService;
import me.akamex.latestpost.user.news.BookNewsOpenProcessor;
import me.akamex.latestpost.user.news.NewsOpenProcessor;
import me.akamex.latestpost.vk.post.PostService;
import me.akamex.latestpost.vk.post.UrlPostService;
import me.luckkyyz.luckapi.LuckApi;
import me.luckkyyz.luckapi.command.ChatCommand;
import me.luckkyyz.luckapi.command.ExecutingChecks;
import me.luckkyyz.luckapi.command.ExecutingStrategy;
import me.luckkyyz.luckapi.config.MessageConfig;
import me.luckkyyz.luckapi.config.SettingConfig;
import me.luckkyyz.luckapi.database.HikariDatabase;
import me.luckkyyz.luckapi.database.HikariQueryExecutors;
import me.luckkyyz.luckapi.database.QueryExecutors;
import me.luckkyyz.luckapi.database.serialize.DatabaseSerializers;
import me.luckkyyz.luckapi.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.HashSet;
import java.util.Set;

@Plugin(name = "LatestPost", version = "1.0.0-SNAPSHOT")
@Author("akamex")
@ApiVersion(ApiVersion.Target.v1_13)
@Commands(
        @Command(name = "news")
)
public final class LatestPostPlugin extends JavaPlugin {

    private LuckApi luckApi;
    private HikariDatabase database;
    private final Set<Scheduler> schedulers = new HashSet<>();

    @Override
    public void onEnable() {
        SettingConfig config = new SettingConfig(this);
        MessageConfig<Messages> messageConfig = new MessageConfig<>(this, Messages.values());

        luckApi = LuckApi.bootstrapWith(this);

        database = DatabaseSerializers.yaml().deserialize(config.getSection("database"));
        QueryExecutors databaseExecutors = new HikariQueryExecutors(database, this);

        PostService postService = new UrlPostService(config, config.getInt("updateTime", 30) * 1000L);
        UserService userService = new DatabaseUserService(databaseExecutors);
        NewsOpenProcessor newsOpenProcessor = new BookNewsOpenProcessor(userService);

        new UserListener(this, postService, userService, newsOpenProcessor);

        ChatCommand.registerCommand("news", ExecutingStrategy.newBuilder().commandStrategy()
                .addCheck(ExecutingChecks.player(), session -> session.getExecutor().send(messageConfig.getMessage(Messages.ONLY_PLAYER)))
                .addAction(session -> postService.getLatestPost().thenAccept(post -> Bukkit.getScheduler().runTask(this, () -> {
                    Player player = session.getExecutor().getPlayer();
                    if(!player.isOnline()) {
                        return;
                    }

                    if(post.getId() == 0) {
                        messageConfig.getMessage(Messages.NO_NEWS).send(player);
                        return;
                    }

                    messageConfig.getMessage(Messages.LAST_NEWS_OPEN).send(player);
                    newsOpenProcessor.openNews(player, post);
                }))
        ));

        schedulers.add(new PostCheckingTask(this, config, messageConfig, postService));
    }

    @Override
    public void onDisable() {
        if(luckApi != null) {
            luckApi.cancel();
        }
        if(database != null && !database.isClosed()) {
            database.close();
        }
        schedulers.stream()
                .filter(scheduler -> !scheduler.isCancelled())
                .forEach(Scheduler::cancel);
    }

}
