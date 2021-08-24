# Text Search Library

This library allows user to build indexes of words for a given folder in a file system and then search for any phrase within the same.

I have made use of Inverted Index data structure to build the index for words. 

## Assumptions:

- Indexes for the words will be built as a whole word and [Stemming](https://en.wikipedia.org/wiki/Stemming) is not performed while building the indexes.

- Search phrase contains unique words.

## Implementation

`TextSearchEngine` provides the API methods for building the indexes and searching for a phrase.

- `void buildIndex(String rootFilePath)` method accepts the root file path as a string and builds index for words present in all the files present in this root directory and subdirectories. This method makes use of multiple threads while building the index for words.
- `List<PhraseLocation> findPhraseLocation(String phrase)` method accepts a phrase in string which can contains multiple words, and returns a list of `PhraseLocation` which contains the file name and position of the phrase. The `PhraseLocation` will refer to the location of the complete phrase given as input and not the individual words which I believe should be the expected result.
- ` void clear()` method clears built indexes.