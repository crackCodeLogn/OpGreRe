package com.vv.personal;

import com.vv.personal.config.Config;
import com.vv.personal.data.ApplicationPropertyReader;
import com.vv.personal.external.local.LocalData;
import com.vv.personal.external.local.LocalDataImpl;
import com.vv.personal.external.remote.ImageExtractor;
import com.vv.personal.external.remote.WordMeaningExtractor;
import com.vv.personal.model.WordModel;
import com.vv.personal.ui.Gui;
import com.vv.personal.util.FileHelper;
import com.vv.personal.util.LoggingHelper;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
    private static final String ROUTE_PRACTICE_RANDOM = "practice-random";
    private static final String ROUTE_PRACTICE_ACCESSED = "practice-accessed";
    private static final String ROUTE_PRACTICE_MARKED = "practice-marked";
    private static final int UI_WIDTH = 1150;
    private static final int UI_HEIGHT = 600;

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-04:00")); //force setting
        ApplicationPropertyReader applicationProperties = new ApplicationPropertyReader(APPLICATION_PROPERTIES);
        OperationGreReServer operationGreReServer = new OperationGreReServer();
        if (args.length == 0) operationGreReServer.shutdown("Cannot start app as argument missing!");

        operationGreReServer.routeAndFire(args, applicationProperties);
    }

    private void routeAndFire(String[] args, ApplicationPropertyReader applicationProperties) {
        LocalData localData = new LocalDataImpl();
        switch (args[0]) {
            case ROUTE_WORD_MEANING:
                extractWordMeaning(applicationProperties);
                break;
            case ROUTE_IMAGE_EXTRACTION:
                extractImages(applicationProperties);
                break;
            case ROUTE_PRACTICE_ACCESSED:
                validate(args.length);
                performAccessedPractice(Integer.parseInt(args[1]), localData);
                break;
            case ROUTE_PRACTICE_MARKED:
                validate(args.length);
                performMarkedPractice(Integer.parseInt(args[1]), localData);
                break;
            case ROUTE_PRACTICE_RANDOM:
            default:
                validate(args.length);
                performRandomPractice(applicationProperties, Integer.parseInt(args[1]), localData);
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
        launch("RANDOM", allWordsToFetch, wordsForPractice, localData, true);
    }

    private void performAccessedPractice(int wordsForPractice, LocalData localData) {
        List<String> accessedWords = localData.readAccessLogData();
        if (accessedWords.isEmpty()) shutdown("Cannot launch as no accessed words yet!");

        launch("ACCESSED", new ArrayList<>(accessedWords), wordsForPractice, localData, false);
    }

    private void performMarkedPractice(int wordsForPractice, LocalData localData) {
        List<String> markedWords = localData.readMarkedWords();
        if (markedWords.isEmpty()) shutdown("Cannot launch as no marked words yet!");

        launch("MARKED", new ArrayList<>(markedWords), wordsForPractice, localData, false);
    }

    private void launch(String title, List<String> allWordsToFetch, int wordsForPractice, LocalData localData, boolean updateAccessLog) {
        Collections.shuffle(allWordsToFetch);
        List<String> allWords = allWordsToFetch.subList(0, wordsForPractice);
        List<WordModel> wordModels = allWords.stream()
                .map(word -> {
                    List<String> wordMeaning = localData.readWordMeaning(word);
                    ImageIcon imageIcon = localData.readImageForWord(word);
                    return new WordModel(word, wordMeaning, imageIcon);
                }).collect(Collectors.toList());

        //transfer to UI this list
        wordModels.forEach(wordModel -> LoggingHelper.info(wordModel.toString()));
        manageUi(wordModels, String.format("GRE PRACTISE WORDS :: %s - %d", title, wordsForPractice), localData, updateAccessLog);
    }

    private void manageUi(List<WordModel> wordModelList, String title, LocalData localData, boolean updateAccessLog) {
        TreeSet<String> markedWordsForLaterPractise = new TreeSet<>();

        JFrame jFrame = new Gui(wordModelList, 0, markedWordsForLaterPractise);
        jFrame.setVisible(true);
        jFrame.setTitle(title);
        jFrame.setLocation(300, 300);
        jFrame.setSize(UI_WIDTH, UI_HEIGHT);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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
