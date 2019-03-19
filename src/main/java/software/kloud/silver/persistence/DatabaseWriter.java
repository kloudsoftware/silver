package software.kloud.silver.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.kloud.sc.SilverCommunication;
import software.kloud.silver.redis.entities.Page;
import software.kloud.silver.redis.util.Serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseWriter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, Page> redisTemplate;
    private final ApplicationContext ctx;
    private final Serializer serializer;
    private final List<String> beanNamesCache;

    @Value("${silver.redis.queues.wait}")
    private String WAIT_QUEUE;
    @Value("${silver.redis.queues.work}")
    private String WORK_QUEUE;

    public DatabaseWriter(RedisTemplate<String, Page> redisTemplate, ApplicationContext ctx, Serializer serializer) {
        this.redisTemplate = redisTemplate;
        this.ctx = ctx;
        this.serializer = serializer;
        this.beanNamesCache = this.buildBeanNamesCache();
    }

    private List<String> buildBeanNamesCache() {
        return Arrays.stream(ctx.getBeanDefinitionNames())
                .filter(n -> n.contains("Repository"))
                .collect(Collectors.toUnmodifiableList());
    }

    @Scheduled(cron = "0 * * * * *")
    public <T extends SilverCommunication> void writeToDatabase() throws IOException {
        var pages = new ArrayList<T>();
        Page page = redisTemplate.opsForList().rightPopAndLeftPush(WAIT_QUEUE, WORK_QUEUE);
        while (page != null) {
            redisTemplate.opsForList().remove(WORK_QUEUE, 1, page);
            page = redisTemplate.opsForList().rightPopAndLeftPush(WAIT_QUEUE, WORK_QUEUE);
            var clazz = Objects.requireNonNull(page).getTypeAsClass();
            @SuppressWarnings("unchecked")
            T payload = (T) objectMapper.readValue(page.getContent(), clazz);
            pages.add(payload);
        }

        List<T> collect = pages.stream()
                .sorted(Comparator.comparingInt(SilverCommunication::prioritySaveOrder))
                .collect(Collectors.toList());

        Collections.reverse(collect);

        collect.forEach(this::writeIntern);
    }

    private <T extends SilverCommunication> void writeIntern(T payload) {
        String simpleNameReplaced = payload.getClass().getSimpleName().replace("JpaRecord", "Repository");

        for (String bean : this.beanNamesCache) {
            if (bean.equalsIgnoreCase(simpleNameReplaced)) {
                //noinspection unchecked
                JpaRepository<T, Integer> repo = ((JpaRepository<T, Integer>) ctx.getBean(bean));
                var entity = repo.save(payload);
                Page newPage = null;
                try {
                    newPage = serializer.serializeAtKey(entity, entity.getSilverIdentifier());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                redisTemplate.opsForValue().set(entity.getSilverIdentifier(), newPage);
                return;
            }
        }
    }
}
