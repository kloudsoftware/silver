package software.kloud.silver.redis.util;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import software.kloud.sc.SilverCommunication;
import software.kloud.silver.redis.entities.Page;

@Service
public class Serializer {
    private final Gson GSON = new Gson();

    public <T extends SilverCommunication> Page serialize(T entity) {
        String json = GSON.toJson(entity);
        Page page = new Page(json, entity.getClass());
        page.setKey(entity.getSilverIdentifier());
        return page;
    }

    public <T extends SilverCommunication> Page serializeAtKey(T entity, String key) {
        String json = GSON.toJson(entity);
        Page page = new Page(json, entity.getClass());
        page.setKey(key);
        return page;
    }

    public <T extends SilverCommunication> T deserialize(Page page, Class<T> type) {
        return GSON.fromJson(page.getContent(), type);
    }
}
