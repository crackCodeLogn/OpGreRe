package config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vivek
 * @since 2024-01-15
 */
@Getter
@Setter
public class FetchConfig {

    private String fetchUrl;
    private int fetchUrlTimeout;
    private String outFolder;

    @Override
    public String toString() {
        return String.format("%s: %d", fetchUrl, fetchUrlTimeout);
    }
}
