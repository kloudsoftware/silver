package software.kloud.silver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public final class LocalDiskStorage {

    private static final Logger logger = LoggerFactory.getLogger(LocalDiskStorage.class);
    private File root;

    public LocalDiskStorage() {
        var root = new File(System.getProperty("user.home") + File.separator + ".silvercache");
        if (!root.isDirectory()) {
            if (!root.mkdir()) {
                logger.error("Failed to create " + root.getPath() + " check permissions or disk corruptions");
                System.exit(-1);
            }
        }

        this.root = root;
    }


    /**
     * Meant for use before the Spring application context is initialized.
     * Caching and tracking of files is _NOT_ enabled and all resources created depending on this root need to
     * perform their own cleanup
     *
     * @return java.io.File of the system storage root
     */
    public static File getStaticRoot() {
        var root = new File(System.getProperty("user.home") + File.separator + ".silvercache");
        if (!root.isDirectory()) {
            if (!root.mkdir()) {
                logger.error("Failed to create " + root.getPath() + " check permissions or disk corruptions");
                System.exit(-1);
            }
        }

        return root;
    }

    public File getRoot() {
        return root;
    }

    private static class INSTANCE_HOLDER {
        static LocalDiskStorage INSTANCE = new LocalDiskStorage();
    }

}
