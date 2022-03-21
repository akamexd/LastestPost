package me.akamex.latestpost.user.news;

import me.akamex.latestpost.user.UserService;
import me.akamex.latestpost.vk.post.Post;
import org.bukkit.entity.Player;

public class NewsOpenProcessorImpl implements NewsOpenProcessor {

    public final UserService userService;

    public NewsOpenProcessorImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void openNews(Player player, Post post) {
        userService.getUser(player).thenAccept(user -> user.setPostId(post.getId()));
    }
}
