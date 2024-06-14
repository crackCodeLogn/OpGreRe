package com.vv.personal.external.local;

import javax.swing.*;
import java.util.List;

/**
 * @author Vivek
 * @since 2024-01-23
 */
public interface LocalData {

    List<String> readAllWords();

    List<String> readAccessLogData();

    List<String> readMarkedWords();

    List<String> readWordMeaning(String word);

    void saveAccessLogData(List<String> words);

    void saveMarkedWords(List<String> words);

    ImageIcon readImageForWord(String word);

    String getMarkedWordsFilePath();
}
