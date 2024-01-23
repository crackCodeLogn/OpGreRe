package util;

/**
 * @author Vivek
 * @since 2024-01-15
 */
public class LoggingHelper {

    private LoggingHelper() {
    }

    public static void info(String data) {
        System.out.printf("%s: %s\n", Thread.currentThread().getName(), data);
    }
}
