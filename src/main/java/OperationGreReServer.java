import config.Config;
import data.ApplicationPropertyReader;
import org.apache.commons.lang3.NotImplementedException;
import remote.ImageExtractor;

/**
 * @author Vivek
 * @since 2024-01-15
 */
public class OperationGreReServer {

    private static final String APPLICATION_PROPERTIES = "app.yml";
    private static final String ROUTE_WORD_MEANING = "word-meaning";
    private static final String ROUTE_IMAGE_EXTRACTION = "image-extraction";

    public static void main(String[] args) {
        ApplicationPropertyReader applicationProperties = new ApplicationPropertyReader(APPLICATION_PROPERTIES);
        if (args.length == 0) {
            System.out.println("Cannot start app as argument missing!");
            System.exit(-1);
        }
        routeAndFire(args[0], applicationProperties);
    }

    private static void routeAndFire(String arg, ApplicationPropertyReader applicationProperties) {
        switch (arg) {
            case ROUTE_WORD_MEANING:
                extractWordMeaning(applicationProperties);
                break;
            case ROUTE_IMAGE_EXTRACTION:
                extractImages(applicationProperties);
                break;
        }
    }

    private static void extractImages(ApplicationPropertyReader applicationProperties) {
        Config configuration = applicationProperties.readConfigProperties();
        ImageExtractor imageExtractor = new ImageExtractor(configuration.getImage(), configuration.getWords());
        imageExtractor.extractAbsentImagesOnly();
        imageExtractor.shutdown();
    }

    private static void extractWordMeaning(ApplicationPropertyReader applicationProperties) {
        throw new NotImplementedException();

        /*
        Config configuration = applicationProperties.readConfigProperties();
        WordMeaningExtractor wordMeaningExtractor = new WordMeaningExtractor(configuration.getWord(), configuration.getWords());

        List<String> meanings = wordMeaningExtractor.extractWordMeaning("haste");
        meanings.forEach(System.out::println);
        meanings = wordMeaningExtractor.extractWordMeaning("delta");
        meanings.forEach(System.out::println);
        wordMeaningExtractor.shutdown(); */
    }
}
