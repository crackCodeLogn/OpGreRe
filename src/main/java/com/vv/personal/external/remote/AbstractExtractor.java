package com.vv.personal.external.remote;

import com.vv.personal.config.FetchConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vivek
 * @since 2024-01-15
 */
@Getter
@Setter
public abstract class AbstractExtractor {
    private static final int EXECUTOR_SERVICE_THREAD_COUNT = 8;

    private final ExecutorService executorService;
    private final FetchConfig fetchConfig;
    private final String wordsTxtLocation;

    protected AbstractExtractor(FetchConfig fetchConfig, String wordsTxtLocation) {
        this.fetchConfig = fetchConfig;
        this.wordsTxtLocation = wordsTxtLocation;
        this.executorService = Executors.newFixedThreadPool(EXECUTOR_SERVICE_THREAD_COUNT);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
