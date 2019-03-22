package software.kloud.silver.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.kloud.sc.SilverCommunication;
import software.kloud.silver.redis.entities.Page;

import java.io.IOException;

@Service
public class Serializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T extends SilverCommunication> Page serialize(T entity) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(entity);
        Page page = new Page(json, entity.getClass());
        page.setKey(entity.getSilverIdentifier());
        return page;
    }

    public <T extends SilverCommunication> Page serializeAtKey(T entity, String key) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(entity);
        Page page = new Page(json, entity.getClass());
        page.setKey(key);
        return page;
    }

    public <T extends SilverCommunication> T deserialize(Page page, Class<T> type) throws IOException {
        return objectMapper.readValue(page.getContent(), type);
    }
}
