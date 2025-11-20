import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JournalManager {
    private final String FILE_NAME = "journal.csv";
    private ArrayList<MoodEntry> entries = new ArrayList<>();

    public JournalManager() {
        load();
    }

    public void addEntry(String text) {
        double score = MoodAnalyzer.scoreText(text);
        double confidence = MoodAnalyzer.confidenceFor(text, score);
        String mood = MoodAnalyzer.classify(score);
        String id = MoodEntry.generateId();
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        MoodEntry e = new MoodEntry(id, text, mood, score, confidence, dateTime);
        entries.add(e);
        save();
    }

    public List<MoodEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public List<MoodEntry> searchByMood(String moodQuery) {
        ArrayList<MoodEntry> r = new ArrayList<>();
        for (MoodEntry e : entries) if (e.getMood().equalsIgnoreCase(moodQuery)) r.add(e);
        return r;
    }

    public void deleteById(String id) {
        entries.removeIf(e -> e.getId().equals(id));
        save();
    }

    public Map<String, Object> getStats() {
        int total = entries.size();
        int pos = 0, neg = 0, neu = 0;
        double sum = 0;
        double sumConf = 0;
        for (MoodEntry e : entries) {
            sum += e.getScore();
            sumConf += e.getConfidence();
            switch (e.getMood()) {
                case "Positive": pos++; break;
                case "Negative": neg++; break;
                default: neu++; break;
            }
        }
        double avg = total == 0 ? 0.0 : sum / total;
        double avgConf = total == 0 ? 0.0 : sumConf / total;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("positive", pos);
        m.put("negative", neg);
        m.put("neutral", neu);
        m.put("averageScore", avg);
        m.put("averageConfidence", avgConf);
        return m;
    }

    private void load() {
        try {
            File f = new File(FILE_NAME);
            if (!f.exists()) return;
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                MoodEntry e = MoodEntry.fromCSV(line);
                if (e != null) entries.add(e);
            }
            br.close();
        } catch (Exception ex) {
            // ignore but continue with empty list
        }
    }

    private void save() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME));
            for (MoodEntry e : entries) pw.println(e.toCSV());
            pw.close();
        } catch (Exception ex) {
            System.out.println("Error saving journal: " + ex.getMessage());
        }
    }
}
