package com.ank;


import com.ank.model.PhraseLocation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;

@Slf4j
@Getter
public class TextSearchEngine {

    private static final int THREAD_POOL_SIZE = 10;
    InvertedIndex invertedIndex;

    public TextSearchEngine() {
        this.invertedIndex = new InvertedIndex();
    }

    public void buildIndex(String rootFilePath) throws IOException {
        log.debug("Building indexes for root file path : " + rootFilePath);
        List<Path> filePaths = this.getFilesPaths(rootFilePath);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CompletableFuture<Void> cfFuture =
                CompletableFuture.allOf(filePaths.stream()
                                                 .map(path -> runAsync(buildIndexForPath(path), executor))
                                                 .toArray(CompletableFuture[]::new));

        cfFuture.join();
        log.debug("Index build completed for root file path : " + rootFilePath);
    }

    public List<PhraseLocation> findPhraseLocation(String phrase) {
        if (this.invertedIndex.getSourceMap().isEmpty()) {
            log.warn("Indexes have not yet been build. Build the index first");
            return Collections.emptyList();
        } else {
            return this.invertedIndex.find(phrase);
        }
    }

    public void clear() {
        this.invertedIndex.clear();
    }

    private List<Path> getFilesPaths(String rootFilePath) throws IOException {
        List<Path> paths;

        try {
            paths = Files.walk(Paths.get(rootFilePath))
                         .filter(Files::isRegularFile)
                         .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception occurred while tracing files.");
            throw e;
        }
        return paths;
    }

    private Runnable buildIndexForPath(Path path) {
        return () -> this.invertedIndex.buildIndex(path);
    }

}
