package com.ank;

import com.ank.model.PhraseLocation;
import com.ank.model.WordLocation;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@Getter
public class InvertedIndex {

    private final Map<UUID, String> sourceMap;
    private final Map<String, Set<WordLocation>> index;

    public InvertedIndex() {
        sourceMap = new ConcurrentHashMap<>();
        index = new HashMap<>();
    }

    public void buildIndex(List<Path> filePaths) {
        for (Path filePath : filePaths) {
            this.buildIndex(filePath);
        }
    }

    public void clear() {
        this.sourceMap.clear();
        this.index.clear();
    }

    public void buildIndex(Path filePath) {
        log.debug("Building indexes for file path : " + filePath);
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            UUID id = UUID.randomUUID();
            sourceMap.put(id, filePath.toString());
            String line;
            int pos = 0;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\W+");
                for (String word : words) {
                    this.indexWord(word, id, pos++);
                }
            }
        } catch (IOException e) {
            log.error("File " + filePath + " not found. Skipping it");
        }
        log.debug("Completed building indexes for file path : " + filePath);
    }

    @Synchronized
    private void indexWord(String word, UUID fileId, int pos) {
        word = sanitize(word);
        index.computeIfAbsent(word, v -> new HashSet<>());
        index.get(word).add(new WordLocation(fileId, pos));
    }

    private String sanitize(String word) {
        return word.replaceAll("[^a-zA-Z0-9]", "");
    }

    public List<PhraseLocation> find(String phrase) {
        String[] words = phrase.split("\\W+");
        int wordsLength = words.length;
        Map<String, Set<WordLocation>> wordLocationMap = new HashMap<>();

        Set<WordLocation> baseWordLocationSet = index.get(words[wordsLength - 1].toLowerCase());
        if (baseWordLocationSet == null) {
            log.info("No matching phrases found..");
            return emptyList();
        } else {
            wordLocationMap.put(words[wordsLength - 1], baseWordLocationSet);
        }

        for (int i = wordsLength - 2; i >= 0; i--) {
            Set<WordLocation> wordLocationSet = index.get(words[i]);

            if (wordLocationSet == null) {
                log.info("No matching phrases found..");
                return emptyList();
            }

            Set<WordLocation> validWordLocationSet = new HashSet<>();
            for (WordLocation wordLocation : wordLocationSet) {
                if (wordLocationMap
                        .get(words[i + 1])
                        .contains(new WordLocation(wordLocation.getFileId(), wordLocation.getPosition() + 1))) {
                    validWordLocationSet.add(wordLocation);
                }
            }
            wordLocationMap.put(words[i], validWordLocationSet);
        }

        if (wordLocationMap.get(words[0]).isEmpty()) {
            log.info("No matching phrases found..");
        }

        return wordLocationMap.get(words[0]).stream()
                              .map(this::mapLocation)
                              .sorted(Comparator.comparing(PhraseLocation::getFileName)
                                                .thenComparing(PhraseLocation::getPosition))
                              .collect(Collectors.toList());
    }

    private PhraseLocation mapLocation(WordLocation wordLocation) {
        var tuple = new PhraseLocation();
        tuple.setFileName(sourceMap.get(wordLocation.getFileId()));
        tuple.setPosition(wordLocation.getPosition());

        return tuple;
    }
}
