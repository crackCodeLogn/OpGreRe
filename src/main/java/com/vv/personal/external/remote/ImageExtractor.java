package com.vv.personal.external.remote;

import com.vv.personal.config.FetchConfig;
import com.vv.personal.util.FileHelper;
import com.vv.personal.util.LoggingHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Vivek
 * @since 2024-01-15
 * <p>
 * Only to be used for initial construction and setup
 */
public class ImageExtractor extends AbstractExtractor {

    private static final int IMAGES_DOWNLOAD_UPPER_LIMIT = 300;
    private static final int IMG_WIDTH = 400;
    private static final int IMG_HEIGHT = 300;
    private static final AtomicBoolean allowProcessing = new AtomicBoolean(true);
    private static final Set<String> blackListWords = new HashSet<>();


    public ImageExtractor(FetchConfig fetchConfig, String wordsTxtLocation) {
        super(fetchConfig, wordsTxtLocation);

        populateBlackListWords();
    }

    private void populateBlackListWords() {
        blackListWords.add("epistemologist");
        blackListWords.add("gerontocracy");
        blackListWords.add("unmitigated");
        blackListWords.add("mealymouthed");
        blackListWords.add("euphoriaric");
        blackListWords.add("encomiastic");
    }

    public void extractAbsentImagesOnly() {
        Path outputFolderPath = Paths.get(FileHelper.PATH_RESOURCES, getFetchConfig().getOutFolder());
        File[] files = outputFolderPath.toFile().listFiles();
        Set<String> allImagesToFetch = new HashSet<>(FileHelper.readWordsFromFile(getWordsTxtLocation(), "---", ":"));
        int totalImagesToFetch = allImagesToFetch.size();
        LoggingHelper.info(String.format("Obtained request to extract %d images", totalImagesToFetch));

        for (File file : files) if (file.isDirectory()) allImagesToFetch.remove(file.getName());
        LoggingHelper.info(String.format("%.2f%% COMPLETED until now. Left with request to extract %d images after removing already present images", ((totalImagesToFetch - allImagesToFetch.size()) / (totalImagesToFetch * 1.0) * 100), allImagesToFetch.size()));
        int cnt = 0;
        for (String imageToFetch : allImagesToFetch) {
            if (cnt > IMAGES_DOWNLOAD_UPPER_LIMIT || !allowProcessing.get()) break; // to deal with api throttling

            if (!blackListWords.contains(imageToFetch))
                extractImage(imageToFetch, cnt);
            cnt++;

            try {
                Thread.sleep(251);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void extractImage(String image, int cnt) {
        getExecutorService().submit(getImage(image.strip(), cnt));
    }

    private Runnable getImage(String word, int cnt) {
        return () -> {
            try {
                String remoteUrl = String.format(getFetchConfig().getFetchUrl(), word);
                LoggingHelper.info(String.format("%d. Remote url to contact for the image => %s", cnt, remoteUrl));
                URL url = new URL(remoteUrl);
                Document jsoup = Jsoup.parse(url, getFetchConfig().getFetchUrlTimeout() * 1000);
                if (jsoup.text().contains(" API ")) {
                    System.out.println("Website throttling in progress, seems I exceeded the img/day limit.");
                    allowProcessing.set(false);
                    return;
                }

                String imgThumbnailUrl = jsoup.getElementsByClass("mui-1l7n00y-thumbnail").get(0)
                        .attr("src");
                LoggingHelper.info(String.format("%s ==> %s", word, imgThumbnailUrl));

                BufferedImage bufferedImage = ImageIO.read(new URL(imgThumbnailUrl));
                File tempImage = new File(word + "-tmp.jpg");
                ImageIO.write(bufferedImage, "jpg", tempImage);

                BufferedImage reSizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, bufferedImage.getType());
                Graphics2D g2d = reSizedImage.createGraphics();
                g2d.drawImage(bufferedImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
                g2d.dispose();

                Path outputFolderPath = Paths.get(FileHelper.PATH_RESOURCES, getFetchConfig().getOutFolder(), word);
                Files.createDirectories(outputFolderPath);

                Path outputFile = outputFolderPath.resolve(word + ".jpg");
                ImageIO.write(reSizedImage, "jpg", outputFile.toFile());
                tempImage.deleteOnExit();

            } catch (IOException e) {
                LoggingHelper.info(e.toString());
                LoggingHelper.info(String.format("Error happened for word: %s", word));
                allowProcessing.set(false);
            }
        };
    }
}
