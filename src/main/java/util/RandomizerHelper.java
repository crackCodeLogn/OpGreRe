package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Vivek
 * @since 2024-01-23
 */
public class RandomizerHelper {

    private RandomizerHelper() {
    }

    public static int getRandomNumber(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high);
    }

    public static List<Integer> getRandomIndexList(int target, int low, int high) {
        if (target > high - low) return new ArrayList<>();

        Set<Integer> randoms = new HashSet<>();
        while (randoms.size() != target) {
            int random = getRandomNumber(low, high);
            while (randoms.contains(random)) random = getRandomNumber(low, high);
            randoms.add(random);
        }
        return new ArrayList<>(randoms);
    }

}
