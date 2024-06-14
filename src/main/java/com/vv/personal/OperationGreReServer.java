package com.vv.personal;

import com.google.common.collect.ImmutableMap;
import com.vv.personal.config.Config;
import com.vv.personal.data.ApplicationPropertyReader;
import com.vv.personal.external.local.LocalData;
import com.vv.personal.external.local.LocalDataImpl;
import com.vv.personal.external.remote.ImageExtractor;
import com.vv.personal.external.remote.WordMeaningExtractor;
import com.vv.personal.model.UiMode;
import com.vv.personal.model.WordModel;
import com.vv.personal.ui.Gui;
import com.vv.personal.util.FileHelper;
import com.vv.personal.util.LoggingHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Vivek
 * @since 2024-01-15
 */
public class OperationGreReServer {

    private static final String APPLICATION_PROPERTIES = "app.yml";
    private static final String ROUTE_WORD_MEANING = "word-meaning";
    private static final String ROUTE_IMAGE_EXTRACTION = "image-extraction";
    private static final String ROUTE_PRACTICE_RANDOM = "random";
    private static final String ROUTE_PRACTICE_ACCESSED = "accessed";
    private static final String ROUTE_PRACTICE_MARKED = "marked";
    private static final ImmutableMap<Character, String> ROUTER_MAP = ImmutableMap.<Character, String>builder()
            .put('w', ROUTE_WORD_MEANING)
            .put('i', ROUTE_IMAGE_EXTRACTION)
            .put('r', ROUTE_WORD_MEANING)
            .put('a', ROUTE_PRACTICE_ACCESSED)
            .put('m', ROUTE_PRACTICE_MARKED)
            .build();

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-04:00")); //force setting
        ApplicationPropertyReader applicationProperties = new ApplicationPropertyReader(APPLICATION_PROPERTIES);
        OperationGreReServer operationGreReServer = new OperationGreReServer();

        Pair<String, Integer> routingParameters = operationGreReServer.readInput();
        operationGreReServer.routeAndFire(routingParameters, applicationProperties);
    }

    private Pair<String, Integer> readInput() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter launch mode: "); // r 11, a 11, m 11, w, i
            String line = bufferedReader.readLine().strip();
            if (line.isEmpty()) shutdown("Cannot launch on empty input!");

            char mode = line.charAt(0);
            if (!ROUTER_MAP.containsKey(mode)) shutdown("Invalid input mode.");

            // show all entries if no param value supplied
            int count = line.length() > 2 ? Integer.parseInt(line.substring(2)) : Integer.MAX_VALUE;
            return Pair.of(ROUTER_MAP.get(mode), count);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            shutdown("Invalid parameters");
        }
        return null;
    }

    private void routeAndFire(Pair<String, Integer> args, ApplicationPropertyReader applicationProperties) {
        LocalData localData = new LocalDataImpl();
        switch (args.getKey()) {
            case ROUTE_WORD_MEANING:
                extractWordMeaning(applicationProperties);
                break;
            case ROUTE_IMAGE_EXTRACTION:
                extractImages(applicationProperties);
                break;
            case ROUTE_PRACTICE_ACCESSED:
                performAccessedPractice(args.getValue(), localData);
                break;
            case ROUTE_PRACTICE_MARKED:
                performMarkedPractice(args.getValue(), localData);
                break;
            case ROUTE_PRACTICE_RANDOM:
            default:
                performRandomPractice(applicationProperties, args.getValue(), localData);
                break;
        }
    }

    private void validate(int argsLength) {
        if (argsLength != 2)
            shutdown("Supply the number of words you want to do for practice as the second input parameter.");
    }

    private void performRandomPractice(ApplicationPropertyReader applicationProperties, int wordsForPractice, LocalData localData) {
        List<String> accessedWords = localData.readAccessLogData();
        final List<String> allWordsToFetch = new ArrayList<>();

        Path outputFolderPath = Paths.get(FileHelper.PATH_RESOURCES, applicationProperties.readConfigProperties().getWord().getOutFolder());
        File[] files = outputFolderPath.toFile().listFiles();

        Arrays.stream(files).forEach(file -> allWordsToFetch.add(file.getName()));

        allWordsToFetch.removeAll(accessedWords);
        launch(UiMode.RANDOM, allWordsToFetch, wordsForPractice, localData, true);
    }

    private void performAccessedPractice(int wordsForPractice, LocalData localData) {
        List<String> accessedWords = localData.readAccessLogData();
        if (accessedWords.isEmpty()) shutdown("Cannot launch as no accessed words yet!");

        launch(UiMode.ACCESSED, new ArrayList<>(accessedWords), wordsForPractice, localData, false);
    }

    private void performMarkedPractice(int wordsForPractice, LocalData localData) {
        List<String> markedWords = new HashSet<>(localData.readMarkedWords()).stream().toList();
        if (markedWords.isEmpty()) shutdown("Cannot launch as no marked words yet!");

        launch(UiMode.MARKED, new ArrayList<>(markedWords), wordsForPractice, localData, false);
    }

    private void launch(UiMode title, List<String> allWordsToFetch, int wordsForPractice, LocalData localData, boolean updateAccessLog) {
        Collections.shuffle(allWordsToFetch);
        int actualWordsForPractise = Math.min(wordsForPractice, allWordsToFetch.size());
        List<String> allWords = allWordsToFetch.subList(0, actualWordsForPractise);
        List<WordModel> wordModels = allWords.stream()
                .map(word -> {
                    List<String> wordMeaning = localData.readWordMeaning(word);
                    ImageIcon imageIcon = localData.readImageForWord(word);
                    return new WordModel(word, wordMeaning, imageIcon);
                }).collect(Collectors.toList());

        //transfer to UI this list
        wordModels.forEach(wordModel -> LoggingHelper.info(wordModel.toString()));
        manageUi(wordModels, title, String.format("GRE PRACTISE WORDS :: %s - %d", title.getValue(), actualWordsForPractise), localData, updateAccessLog);
    }

    private void manageUi(List<WordModel> wordModelList, UiMode mode, String title, LocalData localData, boolean updateAccessLog) {
        TreeSet<String> markedWordsForLaterPractise = new TreeSet<>();

        JFrame jFrame = new Gui(wordModelList, mode, markedWordsForLaterPractise);
        jFrame.setVisible(true);
        jFrame.toFront();
        jFrame.requestFocus();
        jFrame.setAutoRequestFocus(true);
        jFrame.setTitle(title);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (mode != UiMode.MARKED) {
                    LoggingHelper.info(String.format("Final marked List: %s", markedWordsForLaterPractise));
                    //this is called when the window i.e. jframe is given the command to close...
                    //this is the implementation for the writing of the marked words onto the markedWords txt file.. these words are those words which have been difficult to remember at first, so as to store them for later purpose, and revise 'em specifically..

                    List<String> wordsAccessed = wordModelList.stream().map(WordModel::getWord).collect(Collectors.toList());
                    if (updateAccessLog) {
                        localData.saveAccessLogData(wordsAccessed);
                        LoggingHelper.info(String.format("Saved access log data of %d records", wordsAccessed.size()));
                    }

                    if (!markedWordsForLaterPractise.isEmpty()) {
                        List<String> markedWords = wordModelList.stream()
                                .map(WordModel::getWord)
                                .filter(markedWordsForLaterPractise::contains)
                                .collect(Collectors.toList());
                        localData.saveMarkedWords(markedWords);
                        LoggingHelper.info(String.format("Saved marked words of %d records", markedWords.size()));
                    }
                }
                shutdown("Bye");
            }
        });
    }

    private void extractImages(ApplicationPropertyReader applicationProperties) {
        Config configuration = applicationProperties.readConfigProperties();
        ImageExtractor imageExtractor = new ImageExtractor(configuration.getImage(), configuration.getWords());
        imageExtractor.extractAbsentImagesOnly();
        imageExtractor.shutdown();
    }

    private void extractWordMeaning(ApplicationPropertyReader applicationProperties) {
        Config configuration = applicationProperties.readConfigProperties();
        WordMeaningExtractor wordMeaningExtractor = new WordMeaningExtractor(configuration.getWord(), configuration.getWords());
        wordMeaningExtractor.extractAbsentWordMeaningsOnly();
        wordMeaningExtractor.shutdown();
    }

    private void shutdown(String shutdownMessage) {
        LoggingHelper.info(shutdownMessage);
        System.exit(0);
    }
}
