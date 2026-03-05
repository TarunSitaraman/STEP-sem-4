
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutocompleteSystem {

    class Node {

        Map<Character, Node> children = new ConcurrentHashMap<>();
        List<String> topSuggestions = new ArrayList<>();
        int frequency = 0;
    }

    private final Node root = new Node();
    private final Map<String, Integer> termFrequencies = new ConcurrentHashMap<>();
    private final int MAX_SUGGESTIONS = 10;

    public void updateFrequency(String query) {
        termFrequencies.put(query, termFrequencies.getOrDefault(query, 0) + 1);
        insertIntoTrie(query);
    }

    private void insertIntoTrie(String query) {
        Node curr = root;
        for (char c : query.toCharArray()) {
            curr.children.putIfAbsent(c, new Node());
            curr = curr.children.get(c);
            updateNodeSuggestions(curr, query);
        }
        curr.frequency = termFrequencies.get(query);
    }

    private void updateNodeSuggestions(Node node, String query) {
        if (!node.topSuggestions.contains(query)) {
            node.topSuggestions.add(query);
        }

        node.topSuggestions.sort((a, b) -> {
            int freqA = termFrequencies.getOrDefault(a, 0);
            int freqB = termFrequencies.getOrDefault(b, 0);
            return freqB != freqA ? Integer.compare(freqB, freqA) : a.compareTo(b);
        });

        if (node.topSuggestions.size() > MAX_SUGGESTIONS) {
            node.topSuggestions.remove(MAX_SUGGESTIONS);
        }
    }

    public List<String> search(String prefix) {
        Node curr = root;
        for (char c : prefix.toCharArray()) {
            if (!curr.children.containsKey(c)) {
                return Collections.emptyList();
            }
            curr = curr.children.get(c);
        }
        return curr.topSuggestions;
    }

    public static void main(String[] args) {
        AutocompleteSystem engine = new AutocompleteSystem();

        engine.updateFrequency("java tutorial");
        engine.updateFrequency("javascript");
        engine.updateFrequency("java download");

        for (int i = 0; i < 10; i++) {
            engine.updateFrequency("java tutorial");
        }

        System.out.println("Suggestions for 'jav': " + engine.search("jav"));
    }
}
