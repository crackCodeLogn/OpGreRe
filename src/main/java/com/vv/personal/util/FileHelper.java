package com.vv.personal.util;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek
 * @since 2024-01-15
 */
public class FileHelper {
    public static final String PATH_RESOURCES = "src/main/resources";
    private static final String IMG_PATH_RESOURCES = PATH_RESOURCES + "/datastore";

    public static List<String> readWordsFromFile(String filePath, String... skipLineProtocol) {
        File wordsTxt = Paths.get(PATH_RESOURCES, filePath).toFile();
        List<String> words = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(wordsTxt))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.strip();
                boolean addWord = !line.isEmpty();

                for (String skipString : skipLineProtocol)
                    if (line.contains(skipString)) {
                        addWord = false;
                        break;
                    }

                if (addWord) words.add(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return words;
    }

    public static ImageIcon readImageForWord(String word) {
        return new ImageIcon(String.format("%s/%s/%s.jpg", IMG_PATH_RESOURCES, word, word));
    }

    public static void writeToFile(String filePath, List<String> words) {
        File wordsTxt = Paths.get(PATH_RESOURCES, filePath).toFile();
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(wordsTxt, true))) {
            printWriter.write(String.format("TimeStamp --> %s\n", getCurrentTimestamp()));
            printWriter.write(String.format("Number of entries --> %d\n\n", words.size()));
            for (String word : words) printWriter.write(word + "\n");
            printWriter.write("-----------------------------------------\n\n");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().toString();
    }
}