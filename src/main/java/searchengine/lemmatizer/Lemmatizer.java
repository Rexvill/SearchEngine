package searchengine.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class Lemmatizer {

    private static final LuceneMorphology luceneMorph;

    private static final String[] partsOfSpeech = new String[]{"ЧАСТ", "МЕЖД", "СОЮЗ", "ПРЕДЛ"};

    static {
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Lemmatizer() {
    }

    private String cleanupContent(String content) {
        return Jsoup.clean(content, Safelist.none());
    }

    private String[] splitTextIntoWords(String text) {
        String[] words;

        if (text.isEmpty()) {
            words = new String[1];
        } else {
            words = text.replaceAll("([^А-яёЁ])", " ").split("[\\s-!.?;,/:©]+");   //(\d)[^А-яёЁ\w]
        }
        return words;
    }

    public HashMap<String, Integer> getLemmas(String text) {
        HashMap<String, Integer> lemmas = new HashMap<>();
        String lemma = "";
        Integer count = 1;
        String[] words = splitTextIntoWords(cleanupContent(text).toLowerCase());
        for (String word : words) {
            if (!anyNormalFormsIsParticle(luceneMorph.getMorphInfo(word))) {
                lemma = luceneMorph.getNormalForms(word).get(0);
                lemmas.merge(lemma, count, (a, b) -> b += 1);
            }
        }
        return lemmas;
    }

    private boolean anyNormalFormsIsParticle(List<String> wordsMorphInfo) {
        return wordsMorphInfo.stream().anyMatch(s -> isParticle(s));
    }

    private boolean isParticle(String morphInfo) {
        for (String property : partsOfSpeech) {
            if (morphInfo.contains(property)) {
                return true;
            }
        }
        return false;
    }
}
