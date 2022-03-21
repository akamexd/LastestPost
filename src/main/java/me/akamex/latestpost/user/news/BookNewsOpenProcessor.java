package me.akamex.latestpost.user.news;

import me.luckkyyz.luckapi.util.itemstack.ItemBuilders;
import me.akamex.latestpost.user.UserService;
import me.akamex.latestpost.vk.post.Post;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

public class BookNewsOpenProcessor extends NewsOpenProcessorImpl {

    public BookNewsOpenProcessor(UserService userService) {
        super(userService);
    }

    @Override
    public void openNews(Player player, Post post) {
        super.openNews(player, post);

        player.openBook(ItemBuilders.newBookBuilder()
                .setPages(post.getText())
                .setDisplay("News book")
                .setAuthor("Akamex")
                .setPageGeneration(BookMeta.Generation.ORIGINAL)
                .setTitle("News book")
                .create());
    }
}
