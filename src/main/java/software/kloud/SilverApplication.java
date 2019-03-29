package software.kloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import software.kloud.classery.jar.JarUnpackingException;
import software.kloud.classery.loader.ClasseryLoader;
import software.kloud.silver.util.LocalDiskStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
@ComponentScan("software.kloud")
@EnableJpaRepositories(basePackages = {"software.kloud"})
public class SilverApplication {

    private static final File entityDirectory = new File("entities");

    public static void main(String[] args)
            throws JarUnpackingException, IOException {
        beforeSpringInit();
        SpringApplication.run(SilverApplication.class, args);
    }

    private static void beforeSpringInit()
            throws IOException, JarUnpackingException {
        var loader = new ClasseryLoader(
                LocalDiskStorage.getStaticRoot(), Collections.singletonList(entityDirectory));
        var allClasses = loader.load();

        LoadedClassesHolder.getInstance().addClasses(allClasses);
    }

    public static class LoadedClassesHolder {
        private List<Class<?>> classes = new ArrayList<>();

        private LoadedClassesHolder() {

        }

        public static LoadedClassesHolder getInstance() {
            return INSTANCE_HOLDER.INSTANCE;
        }

        void addClasses(List<Class<?>> classes) {
            this.classes = new ArrayList<>(classes);
        }

        public List<Class<?>> getAll() {
            return this.classes;
        }

        private static class INSTANCE_HOLDER {
            static LoadedClassesHolder INSTANCE = new LoadedClassesHolder();
        }
    }
}
