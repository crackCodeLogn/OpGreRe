package remote;

import config.FetchConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import util.LoggingHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Vivek
 * @since 2024-01-15
 * <p>
 * Only to be used for initial construction and setup
 */
public class WordMeaningExtractor extends AbstractExtractor {

    public WordMeaningExtractor(FetchConfig fetchConfig, String wordsTxtLocation) {
        super(fetchConfig, wordsTxtLocation);
    }

    public List<String> extractWordMeaning(String word) {
        try {
            Future<List<String>> futureMeanings = getExecutorService().submit(getWordMeaning(word));
            return futureMeanings.get(getFetchConfig().getFetchUrlTimeout(), TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return new ArrayList<>();
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