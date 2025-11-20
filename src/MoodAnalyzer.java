import java.util.*;
import java.util.regex.*;

public class MoodAnalyzer {
    // Weighted lexicon (positive +, negative -). ~60 entries (medium-sized).
    private static final Map<String, Integer> lexicon = new HashMap<>();
    private static final Map<String, Integer> phraseWeights = new HashMap<>();
    private static final Set<String> negationWords = new HashSet<>(Arrays.asList(
            "not","no","never","none","n't","cant","cannot","hardly","rarely","without","never"
    ));
    private static final Set<String> punctuation = new HashSet<>(Arrays.asList(".", "!", "?"));

    static {
        // Positive (weights +1 or +2)
        for (String s : new String[] {
                "happy","joy","good","great","awesome","fantastic","love","excited","pleased",
                "grateful","calm","relaxed","satisfied","productive","hopeful","optimistic","excellent",
                "energized","confident","motivated","cheerful","delighted","content","better","improved"
        }) lexicon.put(s, 1);
        for (String s : new String[] {
                "amazing","incredible","perfect","ecstatic","thrilled","superb","outstanding"
        }) lexicon.put(s, 2);

        // Negative (weights -1 or -2)
        for (String s : new String[] {
                "sad","angry","bad","terrible","hate","depressed","anxious","worried",
                "stressed","tired","upset","frustrated","lonely","guilty","bored","overwhelmed",
                "annoyed","disappointed","hurt","sick","suffering","angst","uneasy","restless"
        }) lexicon.put(s, -1);
        for (String s : new String[] {
                "horrible","miserable","suicidal","despair","devastated","panic","terrible","awful"
        }) lexicon.put(s, -2);

        // Phrase weights (bigrams/trigrams) - more reliable signals
        phraseWeights.put("nothing went right", -3);
        phraseWeights.put("everything went wrong", -3);
        phraseWeights.put("not at all good", -2);
        phraseWeights.put("could not sleep", -2);
        phraseWeights.put("couldn't sleep", -2);
        phraseWeights.put("not good", -2);
        phraseWeights.put("not happy", -2);
        phraseWeights.put("not well", -2);
        phraseWeights.put("very happy", 2);
        phraseWeights.put("felt extremely tired", -2);
        phraseWeights.put("extremely stressed", -2);
        phraseWeights.put("really bad", -2);
        phraseWeights.put("felt productive", 1);
        phraseWeights.put("finished my practice", 1);
    }

    // Basic normalization and contraction handling
    private static String normalize(String text) {
        if (text == null) return "";
        String t = text.toLowerCase();
        t = t.replace("’", "'"); // fancy apostrophe
        // common contractions normalization
        t = t.replaceAll("\bim\b", "i am");
        t = t.replaceAll("\bi'm\b", "i am");
        t = t.replaceAll("\bdont\b", "do not");
        t = t.replaceAll("\bdon't\b", "do not");
        t = t.replaceAll("\bdoesnt\b", "does not");
        t = t.replaceAll("\bdoesn't\b", "does not");
        t = t.replaceAll("\bcant\b", "can not");
        t = t.replaceAll("\bcan't\b", "can not");
        t = t.replaceAll("\bcouldnt\b", "could not");
        t = t.replaceAll("\bcouldn't\b", "could not");
        t = t.replaceAll("\bwon't\b", "will not");
        t = t.replaceAll("\bwasn't\b", "was not");
        t = t.replaceAll("[^a-z0-9\\s\\.?!'’—-]", " ");
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }
    
    private static String stem(String tok) {
        if (tok.length() > 4) {
            if (tok.endsWith("ing")) return tok.substring(0, tok.length()-3);
            if (tok.endsWith("ed")) return tok.substring(0, tok.length()-2);
            if (tok.endsWith("ness")) return tok.substring(0, tok.length()-4);
            if (tok.endsWith("ly")) return tok.substring(0, tok.length()-2);
        }
        if (tok.endsWith("s") && tok.length() > 3) return tok.substring(0, tok.length()-1);
        return tok;
    }

    // Score text and return score (-1..1)
    public static double scoreText(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        String norm = normalize(text);
        if (norm.isEmpty()) return 0.0;

        // check phrase weights first (longest match)
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        String[] tokens = norm.split(" ");
        // create a token list that preserves punctuation tokens .,!? as separate tokens
        List<String> tlist = new ArrayList<>();
        for (String tk : tokens) {
            // split tokens that end with punctuation like word. into word and .
            if (tk.length() > 1 && (tk.endsWith(".") || tk.endsWith("!") || tk.endsWith("?"))) {
                tlist.add(tk.substring(0, tk.length()-1));
                tlist.add(tk.substring(tk.length()-1));
            } else tlist.add(tk);
        }

        // phrase detection (trigrams then bigrams)
        boolean[] consumed = new boolean[tlist.size()];
        for (int i = 0; i < tlist.size(); i++) {
            if (consumed[i]) continue;
            // trigrams
            if (i+2 < tlist.size()) {
                String tri = tlist.get(i) + " " + tlist.get(i+1) + " " + tlist.get(i+2);
                if (phraseWeights.containsKey(tri)) {
                    int w = phraseWeights.get(tri);
                    weightedSum += w;
                    weightTotal += Math.abs(w);
                    consumed[i]=consumed[i+1]=consumed[i+2]=true;
                    continue;
                }
            }
            // bigrams
            if (i+1 < tlist.size()) {
                String bi = tlist.get(i) + " " + tlist.get(i+1);
                if (phraseWeights.containsKey(bi)) {
                    int w = phraseWeights.get(bi);
                    weightedSum += w;
                    weightTotal += Math.abs(w);
                    consumed[i]=consumed[i+1]=true;
                    continue;
                }
            }
        }

        // token-level scoring with negation scope
        int i = 0;
        while (i < tlist.size()) {
            if (consumed[i]) { i++; continue; }
            String tk = tlist.get(i);
            if (punctuation.contains(tk)) { i++; continue; }
            String stemmed = stem(tk.replaceAll("[^a-z0-9']", ""));
            if (stemmed.isEmpty()) { i++; continue; }

            // negation handling: if current token is negation, flip next N tokens until punctuation or max window
            if (negationWords.contains(stemmed)) {
                int window = 5;
                for (int k = 1; k <= window && i + k < tlist.size(); k++) {
                    String next = tlist.get(i+k);
                    if (punctuation.contains(next)) break;
                    String s2 = stem(next.replaceAll("[^a-z0-9']", ""));
                    s2 = stem(s2);
                    if (s2.isEmpty()) continue;
                    int w = lexicon.getOrDefault(s2, 0);
                    if (w != 0) {
                        weightedSum -= w; // flip sign
                        weightTotal += Math.abs(w);
                    }
                }
                i++;
                continue;
            }

            int w = lexicon.getOrDefault(stemmed, 0);
            if (w != 0) {
                weightedSum += w;
                weightTotal += Math.abs(w);
            }
            i++;
        }

        if (weightTotal == 0) {
            return 0.0;
        }
        double raw = weightedSum / weightTotal;
        if (raw > 1) raw = 1;
        if (raw < -1) raw = -1;
        return raw;
    }

    // Confidence: based on total observed absolute weight (more signals -> higher confidence)
    public static double confidenceFor(String text, double score) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        String norm = normalize(text);
        if (norm.isEmpty()) return 0.0;
        String[] tokens = norm.split(" ");
        List<String> tlist = new ArrayList<>();
        for (String tk : tokens) {
            if (tk.length() > 1 && (tk.endsWith(".") || tk.endsWith("!") || tk.endsWith("?"))) {
                tlist.add(tk.substring(0, tk.length()-1));
                tlist.add(tk.substring(tk.length()-1));
            } else tlist.add(tk);
        }
        double weightTotal = 0.0;
    
        for (int i = 0; i < tlist.size(); i++) {
            if (i+2 < tlist.size()) {
                String tri = tlist.get(i) + " " + tlist.get(i+1) + " " + tlist.get(i+2);
                if (phraseWeights.containsKey(tri)) weightTotal += Math.abs(phraseWeights.get(tri));
            }
            if (i+1 < tlist.size()) {
                String bi = tlist.get(i) + " " + tlist.get(i+1);
                if (phraseWeights.containsKey(bi)) weightTotal += Math.abs(phraseWeights.get(bi));
            }
        }
        for (String tk : tlist) {
            String s = stem(tk.replaceAll("[^a-z0-9']", ""));
            if (s.isEmpty()) continue;
            weightTotal += Math.abs(lexicon.getOrDefault(s, 0));
        }
        // scale into 0..1 (tunable). Use a soft scaling: weightTotal / (weightTotal + 5)
        double conf = weightTotal / (weightTotal + 5.0);
        if (conf > 1) conf = 1;
        if (conf < 0) conf = 0;
        return conf;
    }

    // classification thresholds — tuned
    public static String classify(double score) {
        if (score >= 0.12) return "Positive";
        if (score <= -0.12) return "Negative";
        return "Neutral";
    }

    public static String emojiForMood(String mood) {
        switch (mood) {
            case "Positive": return "\uD83D\uDE0A"; // 😊
            case "Negative": return "\uD83D\uDE1E"; // 😞
            default: return "\uD83D\uDE10"; // 😐
        }
    }
}
