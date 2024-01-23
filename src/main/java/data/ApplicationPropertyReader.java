package data;

import config.Config;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * @author Vivek
 * @since 2024-01-15
 */
public class ApplicationPropertyReader {

    private final String propertyLocation;

    public ApplicationPropertyReader(String propertyLocation) {
        this.propertyLocation = propertyLocation;
    }

    public Config readConfigProperties() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(propertyLocation);
        return yaml.loadAs(inputStream, Config.class);
    }
}
