package me.akamex.latestpost;

import me.akamex.latestpost.user.UserService;
import me.akamex.latestpost.user.news.NewsOpenProcessor;
import me.akamex.latestpost.vk.post.PostService;
import me.luckkyyz.luckapi.event.ExtendedListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.plugin.Plugin;

class UserListener extends ExtendedListener {

    private final PostService postService;
    private final UserService userService;
    private final NewsOpenProcessor newsOpenProcessor;

    UserListener(Plugin plugin, PostService postService, UserService userService, NewsOpenProcessor newsOpenProcessor) {
        super(plugin);
        this.postService = postService;
        this.userService = userService;
        this.newsOpenProcessor = newsOpenProcessor;
    }

    @EventHandler
    private void onMap(MapInitializeEvent event) {
        
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        userService.getUser(player).thenAccept(user -> postService.getLatestPost().thenAccept(post -> Bukkit.getScheduler().runTask(plugin, () -> {
            if(post.getId() == 0 || user.getPostId() == post.getId()) {
                return;
            }

            newsOpenProcessor.openNews(player, post);
        })));
    }

    private void handleQuit(PlayerEvent event) {
        userService.invalidateUser(event.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        handleQuit(event);
    }

    @EventHandler
    private void onKick(PlayerKickEvent event) {
        handleQuit(event);
    }

}
