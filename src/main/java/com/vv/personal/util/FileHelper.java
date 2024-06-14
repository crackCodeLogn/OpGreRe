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
    private static final String LINE_BREAK = "\n";

    public static List<String> readWordsFromFile(String filePath, String... skipLineProtocol) {
        List<String> words = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(getResFilePath(filePath)))) {
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
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(getResFilePath(filePath), true))) {
            printWriter.write(String.format("TimeStamp --> %s\n", getCurrentTimestamp()));
            printWriter.write(String.format("Number of entries --> %d\n\n", words.size()));
            for (String word : words) printWriter.write(word + LINE_BREAK);
            printWriter.write("-----------------------------------------\n\n");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void dumpFile(String filePath, List<String> words) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(getResFilePath(filePath)))) {
            words.forEach(word -> printWriter.write(word.strip() + LINE_BREAK));
            printWriter.write(LINE_BREAK);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().toString();
    }

    private static File getResFilePath(String filePath) {
        return Paths.get(PATH_RESOURCES, filePath).toFile();
    }
}