package config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vivek
 * @since 2024-01-15
 */
@Getter
@Setter
public class Config {
    private FetchConfig word;
    private FetchConfig image;
    private String words;
}