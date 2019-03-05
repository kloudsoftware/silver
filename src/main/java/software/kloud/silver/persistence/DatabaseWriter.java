package software.kloud.silver.persistence;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.kloud.sc.SilverCommunication;
import software.kloud.silver.redis.util.Serializer;
import software.kloud.silver.redis.entities.Page;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseWriter {
    private final static Gson gson = new Gson();
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
    public void writeToDatabase() {
        Page page = redisTemplate.opsForList().rightPopAndLeftPush(WAIT_QUEUE, WORK_QUEUE);
        while (page != null) {
            writeIntern(page);
            redisTemplate.opsForList().remove(WORK_QUEUE, 1, page);
            page = redisTemplate.opsForList().rightPopAndLeftPush(WAIT_QUEUE, WORK_QUEUE);
        }
    }

    private <T extends SilverCommunication> void writeIntern(Page page) {
        var clazz = page.getTypeAsClass();
        T payload = gson.fromJson(page.getContent(), (Type) clazz);

        String simpleNameReplaced = payload.getClass().getSimpleName().replace("JpaRecord", "Repository");

        for (String bean : this.beanNamesCache) {
            if (bean.equalsIgnoreCase(simpleNameReplaced)) {
                //noinspection unchecked
                JpaRepository<T, Integer> repo = ((JpaRepository<T, Integer>) ctx.getBean(bean));
                var entity = repo.save(payload);
                var newPage = serializer.serializeAtKey(entity, entity.getSilverIdentifier());
                redisTemplate.opsForValue().set(entity.getSilverIdentifier(), newPage);
                return;
            }
        }
    }
}
