package software.kloud.silver.redis.entities;

import software.kloud.sc.SilverCommunication;

import java.io.Serializable;

public class Page implements Serializable {
    private static final long serialVersionUID = -1791946046274283230L;

    private final String type;
    private String key;
    private String content;

    public Page(String content, Class<? extends SilverCommunication> type) {
        this.type = type.getCanonicalName();
        this.content = content;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public Class<? extends SilverCommunication> getTypeAsClass() {
        try {
            //noinspection unchecked
            return (Class<? extends SilverCommunication>) Class.forName(type);
        } catch (ClassNotFoundException e) {
            // FIXME better error handling
            e.printStackTrace();
            return null;
        }
    }
}
