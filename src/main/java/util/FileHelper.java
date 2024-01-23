package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek
 * @since 2024-01-15
 */
public class FileHelper {

    private static final String AVOID_LINE_STR1 = "---";
    private static final String AVOID_LINE_STR2 = "---";
    private static final String PATH_RESOURCES = "src/main/resources";

    public static List<String> readWordNamesFromFile(String filePath) {
        File wordsTxt = Paths.get(PATH_RESOURCES, filePath).toFile();
        List<String> words = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(wordsTxt))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith(AVOID_LINE_STR1) && !line.contains(AVOID_LINE_STR2)) words.add(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return words;
    }


}
