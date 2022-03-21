package me.akamex.latestpost.config;

import me.luckkyyz.luckapi.config.MessagePath;

public enum Messages implements MessagePath {

    ONLY_PLAYER("onlyPlayer", "&cЭта команда только для игроков!"),
    LAST_NEWS_OPEN("lastOpen", "&aПоследняя новость была открыта!"),
    NEWS("news", "&aВ группе появилась новая новость! Клик чтобы узнать"),
    NO_NEWS("noNews", "&cВ группе нет новостей :(");

    private final String path, defaultValue;

    Messages(String path, String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}
