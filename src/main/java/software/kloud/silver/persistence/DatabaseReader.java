package software.kloud.silver.persistence;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import software.kloud.sc.SilverCommunication;
import software.kloud.sc.SilverRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DatabaseReader {
    private final ApplicationContext ctx;
    private final List<String> beanNamesCache;

    public DatabaseReader(ApplicationContext ctx) {
        this.ctx = ctx;
        this.beanNamesCache = buildBeanNamesCache();
    }

    private List<String> buildBeanNamesCache() {
        return Arrays.stream(ctx.getBeanDefinitionNames())
                .filter(n -> n.contains("Repository"))
                .collect(Collectors.toUnmodifiableList());
    }


    public <T extends SilverCommunication> Optional<T> read(String silverId, Class<? extends T> clazz) {

        String simpleNameReplaced = clazz.getSimpleName().replace("JpaRecord", "Repository");

        for (String bean : this.beanNamesCache) {
            if (bean.equalsIgnoreCase(simpleNameReplaced)) {
                //noinspection unchecked
                SilverRepository<T> repo = ((SilverRepository<T>) ctx.getBean(bean));
                return repo.findBySilverIdentifier(silverId);
            }
        }

        return Optional.empty();
    }
}
