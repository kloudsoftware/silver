package software.kloud;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.*;
import software.kloud.SilverApplication;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EntityLoader {
    private static List<Class<? extends Annotation>> springAnnotations;

    static {
        springAnnotations = new ArrayList<>();
        springAnnotations.add(Repository.class);
        springAnnotations.add(Entity.class);
    }

    private static boolean checkIfClassIsSpringStereotype(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(a -> springAnnotations.contains(a));
    }

    @Bean
    public BeanDefinitionRegistryPostProcessor getRegistry() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                for (Class<?> clazz : SilverApplication.LoadedClassesHolder.getInstance().getAll()) {
                    if (!checkIfClassIsSpringStereotype(clazz)) {
                        continue;
                    }
                    AnnotatedBeanDefinition def = new AnnotatedGenericBeanDefinition(clazz);
                    registry.registerBeanDefinition(clazz.getName(), def);
                }
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

            }
        };

    }
}
