
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlagiarismDetector {

    private final int N = 5;
    private final Map<Integer, Set<String>> ngramIndex = new ConcurrentHashMap<>();
    private final Map<String, Integer> documentTotalNgrams = new ConcurrentHashMap<>();

    public void indexDocument(String docId, String content) {
        List<String> words = preprocess(content);
        if (words.size() < N) {
            return;
        }

        int totalNgrams = 0;
        for (int i = 0; i <= words.size() - N; i++) {
            int hash = generateNgramHash(words.subList(i, i + N));
            ngramIndex.computeIfAbsent(hash, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(docId);
            totalNgrams++;
        }
        documentTotalNgrams.put(docId, totalNgrams);
    }

    public Map<String, Double> analyzeDocument(String content) {
        List<String> words = preprocess(content);
        Map<String, Integer> matchCounts = new HashMap<>();
        int inputNgrams = 0;

        for (int i = 0; i <= words.size() - N; i++) {
            inputNgrams++;
            int hash = generateNgramHash(words.subList(i, i + N));
            Set<String> matches = ngramIndex.get(hash);

            if (matches != null) {
                for (String docId : matches) {
                    matchCounts.put(docId, matchCounts.getOrDefault(docId, 0) + 1);
                }
            }
        }

        Map<String, Double> results = new HashMap<>();
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            double similarity = (entry.getValue() * 100.0) / inputNgrams;
            results.put(entry.getKey(), similarity);
        }
        return results;
    }

    private List<String> preprocess(String text) {
        return Arrays.asList(text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+"));
    }

    private int generateNgramHash(List<String> ngram) {
        return String.join(" ", ngram).hashCode();
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        detector.indexDocument("essay_092.txt", "The quick brown fox jumps over the lazy dog repeatedly");
        detector.indexDocument("essay_089.txt", "A quick brown fox jumps over a very lazy sleeping dog");

        String newSubmission = "The quick brown fox jumps over the lazy dog for fun";
        Map<String, Double> report = detector.analyzeDocument(newSubmission);

        report.forEach((docId, score) -> {
            System.out.printf("Match with %s: %.1f%% similarity%s\n",
                    docId, score, (score > 60 ? " (PLAGIARISM DETECTED)" : ""));
        });
    }
}
