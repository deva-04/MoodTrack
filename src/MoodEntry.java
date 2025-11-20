import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

public class MoodEntry {
    private String id;
    private String text;
    private String mood; // Positive / Neutral / Negative
    private String dateTime; // ISO string
    private double score; // sentiment score from -1.0 .. +1.0
    private double confidence; // 0.0 .. 1.0

    public MoodEntry(String id, String text, String mood, double score, double confidence, String dateTime) {
        this.id = id;
        this.text = text.replace(",", " "); // avoid CSV break
        this.mood = mood;
        this.score = score;
        this.confidence = confidence;
        this.dateTime = dateTime;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getMood() { return mood; }
    public double getScore() { return score; }
    public double getConfidence() { return confidence; }
    public String getDateTime() { return dateTime; }

    @Override
    public String toString() {
        return id + " | " + dateTime + " | " + mood + " (" + String.format("%.2f", score) + ", conf:" + String.format("%.2f", confidence) + ") -> " + text;
    }

    // CSV row for storage: id,score,confidence,mood,dateTime,text
    public String toCSV() {
        return id + "," + score + "," + confidence + "," + mood + "," + dateTime + "," + text;
    }

    public static MoodEntry fromCSV(String csvLine) {
        String[] p = csvLine.split(",", 6);
        if (p.length < 6) return null;
        String id = p[0];
        double score = Double.parseDouble(p[1]);
        double confidence = Double.parseDouble(p[2]);
        String mood = p[3];
        String dateTime = p[4];
        String text = p[5];
        return new MoodEntry(id, text, mood, score, confidence, dateTime);
    }

    // generate a timestamp id
    public static String generateId() {
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
    }
}
