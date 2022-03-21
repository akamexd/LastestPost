package me.akamex.latestpost;

import me.akamex.latestpost.config.Messages;
import me.akamex.latestpost.vk.post.Post;
import me.akamex.latestpost.vk.post.PostService;
import me.luckkyyz.luckapi.config.MessageConfig;
import me.luckkyyz.luckapi.config.SettingConfig;
import me.luckkyyz.luckapi.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

class PostCheckingTask extends Scheduler {

    private final MessageConfig<Messages> messageConfig;
    private final PostService postService;

    private Post lastChecked;

    PostCheckingTask(Plugin plugin, SettingConfig config, MessageConfig<Messages> messageConfig, PostService postService) {
        super(plugin);
        this.messageConfig = messageConfig;
        this.postService = postService;

        runTaskTimer(config.getInt("updateTime") * 20L);
    }

    @Override
    public void run() {
        postService.getLatestPost().thenAccept(post -> Bukkit.getScheduler().runTask(plugin, () -> {
            if(lastChecked == null) {
                lastChecked = post;
                return;
            }

            if(post.getId() == 0 || post.getId() == lastChecked.getId()) {
                return;
            }
            lastChecked = post;
            messageConfig.getMessage(Messages.NEWS).broadcast();
        }));
    }
}
