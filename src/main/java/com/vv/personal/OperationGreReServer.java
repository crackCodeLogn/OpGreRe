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
import java.time.ZoneId;
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
    private static final int UI_WIDTH = 1150;
    private static final int UI_HEIGHT = 600;

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("EST", ZoneId.SHORT_IDS))); //force setting
        ApplicationPropertyReader applicationProperties = new ApplicationPropertyReader(APPLICATION_PROPERTIES);
        if (args.length == 0) {
            System.out.println("Cannot start app as argument missing!");
            System.exit(-1);
        }
        routeAndFire(args, applicationProperties);
    }

    private static void routeAndFire(String[] args, ApplicationPropertyReader applicationProperties) {
        switch (args[0]) {
            case ROUTE_WORD_MEANING:
                extractWordMeaning(applicationProperties);
                break;
            case ROUTE_IMAGE_EXTRACTION:
                extractImages(applicationProperties);
                break;
            case ROUTE_PRACTICE_RANDOM:
                if (args.length != 2) {
                    System.out.println("Supply the number of words you want to do for practice as the second input parameter.");
                    System.exit(0);
                }
                performRandomPractice(applicationProperties, Integer.parseInt(args[1]));
                break;
        }
    }

    private static void performRandomPractice(ApplicationPropertyReader applicationProperties, int wordsForPractice) {
        LocalData localData = new LocalDataImpl();
        List<String> accessedWords = localData.readAccessLogData();
        final List<String> allWordsToFetch = new ArrayList<>();

        Path outputFolderPath = Paths.get(FileHelper.PATH_RESOURCES, applicationProperties.readConfigProperties().getWord().getOutFolder());
        File[] files = outputFolderPath.toFile().listFiles();

        Arrays.stream(files).forEach(file -> allWordsToFetch.add(file.getName()));

        allWordsToFetch.removeAll(accessedWords);
        Collections.shuffle(allWordsToFetch);
        List<String> allWords = allWordsToFetch.subList(0, wordsForPractice);
        //List<Integer> randomIndices = RandomizerHelper.getRandomIndexList(wordsForPractice, 0, allWords.size());

        List<WordModel> wordModels = allWords.stream()
                .map(word -> {
                    List<String> wordMeaning = localData.readWordMeaning(word);
                    ImageIcon imageIcon = localData.readImageForWord(word);

                    return new WordModel(word, wordMeaning, imageIcon);
                })
                .collect(Collectors.toList());

        //transfer to UI this list
        wordModels.forEach(wordModel -> LoggingHelper.info(wordModel.toString()));
        manageUi(wordModels, "GRE PRACTISE WORDS -- RANDOM VOCAB: " + wordsForPractice, localData);
    }

    private static void manageUi(List<WordModel> wordModelList, String title, LocalData localData) {
        TreeSet<String> markedWordsForLaterPractise = new TreeSet<>();

        JFrame jFrame = new Gui(wordModelList, 0, markedWordsForLaterPractise);
        jFrame.setVisible(true);
        jFrame.setTitle(title);
        jFrame.setSize(UI_WIDTH, UI_HEIGHT);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LoggingHelper.info(String.format("Final marked List: %s", markedWordsForLaterPractise));
                //this is called when the window i.e. jframe is given the command to close...
                //this is the implementation for the writing of the marked words onto the markedWords txt file.. these words are those words which have been difficult to remember at first, so as to store them for later purpose, and revise 'em specifically..

                List<String> wordsAccessed = wordModelList.stream().map(WordModel::getWord).collect(Collectors.toList());
                localData.saveAccessLogData(wordsAccessed);
                LoggingHelper.info(String.format("Saved access log data of %d records", wordsAccessed.size()));

                if (!markedWordsForLaterPractise.isEmpty()) {
                    List<String> markedWords = wordModelList.stream()
                            .map(WordModel::getWord)
                            .filter(markedWordsForLaterPractise::contains)
                            .collect(Collectors.toList());
                    localData.saveMarkedWords(markedWords);
                    LoggingHelper.info(String.format("Saved marked words of %d records", markedWords.size()));
                }
                System.exit(0);
            }
        });
    }

    private static void extractImages(ApplicationPropertyReader applicationProperties) {
        Config configuration = applicationProperties.readConfigProperties();
        ImageExtractor imageExtractor = new ImageExtractor(configuration.getImage(), configuration.getWords());
        imageExtractor.extractAbsentImagesOnly();
        imageExtractor.shutdown();
    }

    private static void extractWordMeaning(ApplicationPropertyReader applicationProperties) {
        Config configuration = applicationProperties.readConfigProperties();
        WordMeaningExtractor wordMeaningExtractor = new WordMeaningExtractor(configuration.getWord(), configuration.getWords());
        wordMeaningExtractor.extractAbsentWordMeaningsOnly();
        wordMeaningExtractor.shutdown();
    }
}
