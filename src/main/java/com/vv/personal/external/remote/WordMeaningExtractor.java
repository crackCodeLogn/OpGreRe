package com.vv.personal.external.remote;

import com.vv.personal.config.FetchConfig;
import com.vv.personal.util.FileHelper;
import com.vv.personal.util.LoggingHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Vivek
 * @since 2024-01-15
 * <p>
 * Only to be used for initial construction and setup
 */
public class WordMeaningExtractor extends AbstractExtractor {

    private static final int WORD_MEANING_DOWNLOAD_UPPER_LIMIT = 2500;
    private static final AtomicBoolean allowProcessing = new AtomicBoolean(true);
    private static final Set<String> blackListWords = new HashSet<>();

    public WordMeaningExtractor(FetchConfig fetchConfig, String wordsTxtLocation) {
        super(fetchConfig, wordsTxtLocation);

        populateBlackListWords();
    }

    private void populateBlackListWords() {

    }

    public void extractAbsentWordMeaningsOnly() {
        Path outputFolderPath = Paths.get(FileHelper.PATH_RESOURCES, getFetchConfig().getOutFolder());
        File[] files = outputFolderPath.toFile().listFiles();
        int totalFoldersPresent = files.length;

        List<String> allWordsToFetch = new ArrayList<>();
        Arrays.stream(files).forEach(file -> {
            if (file.isDirectory()) {
                String name = file.getName();
                boolean isWordMeaningPresent = false;
                for (File internalFile : file.listFiles()) {
                    if (internalFile.isFile() && internalFile.getName().endsWith(".txt") && internalFile.length() > 0) {
                        isWordMeaningPresent = true;
                        break;
                    }
                }
                if (!isWordMeaningPresent) allWordsToFetch.add(name);
            }
        });
        int totalWordsToFetch = allWordsToFetch.size();
        LoggingHelper.info(String.format("Obtained request to extract %d word meanings", totalWordsToFetch));

        LoggingHelper.info(String.format("%.2f%% COMPLETED until now. Left with request to extract %d meanings after removing already present meanings", ((totalFoldersPresent - allWordsToFetch.size()) / (totalFoldersPresent * 1.0) * 100), allWordsToFetch.size()));
        int cnt = 0;
        for (String wordMeaningToFetch : allWordsToFetch) {
            if (cnt > WORD_MEANING_DOWNLOAD_UPPER_LIMIT || !allowProcessing.get()) break; // to deal with api throttling

            if (!blackListWords.contains(wordMeaningToFetch)) {
                LoggingHelper.info(String.format("%d. %s", cnt, wordMeaningToFetch));
                extractWordMeaning(wordMeaningToFetch);
            }
            cnt++;

            try {
                Thread.sleep(101);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void extractWordMeaning(String word) {
        try {
            Future<List<String>> futureMeanings = getExecutorService().submit(getWordMeaning(word));
            List<String> wordMeanings = futureMeanings.get(getFetchConfig().getFetchUrlTimeout(), TimeUnit.SECONDS);
            FileHelper.writeToFile(String.format("datastore/%s/%s.txt", word, word), wordMeanings);
            LoggingHelper.info(String.format("Completed task for %s", word));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            System.out.println(e);
        }
    }

    private Callable<List<String>> getWordMeaning(String word) {
        return () -> {
            try {
                String remoteUrl = String.format(getFetchConfig().getFetchUrl(), word); //alternate site is "https://wordinfo.info/results/%s"
                LoggingHelper.info("Remote url to contact for the meaning => " + remoteUrl);
                URL url = new URL(remoteUrl);
                Document jsoup = Jsoup.parse(url, getFetchConfig().getFetchUrlTimeout() * 1000);

                return jsoup.getElementsByClass("q7ELwPUtygkuxUXXOE9t LVt92HnYuY17Vv04474m").get(0)
                        //return jsoup.getElementsByClass("word").get(0)
                        .getElementsByClass("ESah86zaufmd2_YPdZtq")
                        //.getElementsByClass("definition")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                return new ArrayList<>();
            }
        };
    }
}