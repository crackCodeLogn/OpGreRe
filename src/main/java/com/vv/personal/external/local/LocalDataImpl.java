package com.vv.personal.external.local;

import com.vv.personal.util.FileHelper;

import javax.swing.*;
import java.util.List;

/**
 * @author Vivek
 * @since 2024-01-23
 */
public class LocalDataImpl implements LocalData {

    private static final String WORDS = "words.txt";
    private static final String ACCESS_LOG = "accessLog.txt";
    private static final String MARKED_WORDS_LOG = "markedWords.txt";

    @Override
    public List<String> readAllWords() {
        return FileHelper.readWordsFromFile(WORDS, "--", ":");
    }

    @Override
    public List<String> readAccessLogData() {
        return FileHelper.readWordsFromFile(ACCESS_LOG, "--");
    }

    @Override
    public List<String> readMarkedWords() {
        return FileHelper.readWordsFromFile(MARKED_WORDS_LOG, "--");
    }

    @Override
    public void saveAccessLogData(List<String> words) {
        FileHelper.writeToFile(ACCESS_LOG, words);
    }

    @Override
    public void saveMarkedWords(List<String> words) {
        FileHelper.writeToFile(MARKED_WORDS_LOG, words);
    }

    @Override
    public ImageIcon readImageForWord(String word) {
        return FileHelper.readImageForWord(word);
    }

    @Override
    public List<String> readWordMeaning(String word) {
        return FileHelper.readWordsFromFile(String.format("datastore/%s/%s.txt", word, word), "--");
    }
}
