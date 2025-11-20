import java.util.*;
import java.io.*;

public class Main {
    private static Scanner sc = new Scanner(System.in);
    private static JournalManager manager = new JournalManager();

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Mood Journal & Sentiment Analyzer (Console)"); 
        showHelp();
        while (true) {
            System.out.print("\n> ");
            String cmd = sc.nextLine().trim();
            if (cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!"); break;
            }
            switch (cmd.toLowerCase()) {
                case "help": showHelp(); break;
                case "add": doAdd(); break;
                case "list": doList(); break;
                case "stats": doStats(); break;
                case "search": doSearch(); break;
                case "delete": doDelete(); break;
                case "export": doExport(); break;
                default:
                    System.out.println("Unknown command. Type 'help' to see commands."); break;
            }
        }
    }

    private static void showHelp() {
        System.out.println("Commands:");
        System.out.println("  add     - Add a mood entry (type your text)"); 
        System.out.println("  list    - List all entries"); 
        System.out.println("  search  - Search entries by mood (Positive/Neutral/Negative)"); 
        System.out.println("  stats   - Show journal statistics and emoji summary"); 
        System.out.println("  delete  - Delete an entry by id"); 
        System.out.println("  export  - Export entries to export.csv (for sharing)"); 
        System.out.println("  help    - Show this help"); 
        System.out.println("  exit    - Exit application"); 
    }

    private static void doAdd() {
        System.out.println("Enter your mood text (single line). Press Enter when done:"); 
        String text = sc.nextLine().trim();
        if (text.isEmpty()) { System.out.println("Empty entry canceled."); return; }
        manager.addEntry(text);
        System.out.println("Saved. Use 'list' to view entries."); 
    }

    private static void doList() {
        List<MoodEntry> all = manager.getAll();
        if (all.isEmpty()) { System.out.println("No entries yet."); return; }
        for (MoodEntry e : all) {
            System.out.println(e.toString() + " " + MoodAnalyzer.emojiForMood(e.getMood()));
        }
    }

    private static void doSearch() {
        System.out.print("Enter mood to search (Positive/Neutral/Negative): "); 
        String m = sc.nextLine().trim();
        List<MoodEntry> res = manager.searchByMood(m);
        if (res.isEmpty()) System.out.println("No matching entries."); 
        else for (MoodEntry e : res) System.out.println(e.toString() + " " + MoodAnalyzer.emojiForMood(e.getMood()));
    }

    private static void doStats() {
        Map<String, Object> s = manager.getStats();
        System.out.println("--- Journal Statistics ---");
        System.out.println("Total entries: " + s.get("total"));
        System.out.println("Positive: " + s.get("positive") + "  Negative: " + s.get("negative") + "  Neutral: " + s.get("neutral"));
        System.out.printf("Average score: %.3f\n", (double) s.get("averageScore"));
        System.out.printf("Average confidence: %.3f\n", (double) s.get("averageConfidence"));
        // emoji summary
        int pos = (int) s.get("positive");
        int neg = (int) s.get("negative");
        int neu = (int) s.get("neutral");
        String moodEmoji = "";
        if (pos >= neg && pos >= neu) moodEmoji = MoodAnalyzer.emojiForMood("Positive");
        else if (neg > pos && neg >= neu) moodEmoji = MoodAnalyzer.emojiForMood("Negative");
        else moodEmoji = MoodAnalyzer.emojiForMood("Neutral");
        System.out.println("Overall mood: " + moodEmoji);
    }

    private static void doDelete() {
        System.out.print("Enter id to delete: "); 
        String id = sc.nextLine().trim();
        manager.deleteById(id);
        System.out.println("If the id existed, it has been removed."); 
    }

    private static void doExport() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("export.csv"));
            for (MoodEntry e : manager.getAll()) pw.println(e.toCSV());
            pw.close();
            System.out.println("Exported to export.csv"); 
        } catch (Exception ex) {
            System.out.println("Export failed: " + ex.getMessage());
        }
    }
}
