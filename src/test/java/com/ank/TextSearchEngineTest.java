package com.ank;

import com.ank.model.PhraseLocation;
import com.ank.model.WordLocation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TextSearchEngineTest {

    TextSearchEngine textSearchEngine;
    String rootFilePath;

    @BeforeEach
    public void setUp() {
        rootFilePath = "./root-directory";
        textSearchEngine = new TextSearchEngine();
    }

    @Test
    @SneakyThrows
    public void testBuildIndex() {

        textSearchEngine.buildIndex(rootFilePath);

        String wordPhrase = "sample";

        Set<WordLocation> set = textSearchEngine.getInvertedIndex().getIndex().get(wordPhrase);
        assertThat(set).isNotEmpty();

        List<PhraseLocation> list = textSearchEngine.findPhraseLocation(wordPhrase);
        assertThat(list).isNotEmpty();

        var phraseLocation = list.get(0);
        assertThat(phraseLocation.getFileName()).isEqualTo(rootFilePath + "/file1.txt");
        assertThat(phraseLocation.getPosition()).isEqualTo(3);
    }

    @Test
    @SneakyThrows
    public void searchPhraseLocation1() {
        textSearchEngine.buildIndex(rootFilePath);

        String wordPhrase = "inverted index";

        List<PhraseLocation> list = textSearchEngine.findPhraseLocation(wordPhrase);
        assertThat(list).isNotEmpty();
        assertThat(list).hasSize(10);

    }

    @Test
    @SneakyThrows
    @DisplayName("Search a long phrase containing multiple words")
    public void searchPhraseLocation2() {
        textSearchEngine.buildIndex(rootFilePath);

        String wordPhrase = "typical search engine indexing algorithm";

        List<PhraseLocation> list = textSearchEngine.findPhraseLocation(wordPhrase);
        assertThat(list).isNotEmpty();
        assertThat(list).hasSize(1);

        assertThat(list.get(0).getFileName()).isEqualTo(rootFilePath + "/subfolder/file3.txt");
        assertThat(list.get(0).getPosition()).isEqualTo(11);
    }

    @Test
    @SneakyThrows
    public void searchPhraseLocationNegativeScenario() {
        textSearchEngine.buildIndex(rootFilePath);

        String wordPhrase = "random phrase";

        List<PhraseLocation> list = textSearchEngine.findPhraseLocation(wordPhrase);
        assertThat(list).isEmpty();
    }

    @Test
    @SneakyThrows
    public void testClearIndex(){
        textSearchEngine.buildIndex(rootFilePath);

        assertThat(textSearchEngine.getInvertedIndex().getIndex()).isNotEmpty();

        textSearchEngine.clear();

        assertThat(textSearchEngine.getInvertedIndex().getIndex()).isEmpty();
    }

}
