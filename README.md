# MoodTrack: Java Sentiment Analyzer & Mood Journal
**Version:** 1.0.0

MoodTrack is a lightweight Java console application that lets users record daily mood entries and automatically analyzes the emotional tone using a custom-built, rule-based sentiment engine.

---

## 📌 Features

- **Add, list, search, delete journal entries**
- **Automatic sentiment analysis**  
  - Score range: **−1.00 to +1.00**  
  - Confidence range: **0.0 to 1.0**
- **Mood classification:** Positive / Neutral / Negative
- **Emoji indicators** 😊 😐 😞
- **Export journal to CSV**
- **Persistent storage (`journal.csv`)**

---

## 🧠 How the Sentiment Engine Works
- **Normalization**: lowercasing, contraction expansion, smart apostrophe fix, noise removal  
- **Phrase matching**: bigrams & trigrams (e.g., “nothing went right”, “extremely stressed”)  
- **Lexicon scoring**: ~60 positive/negative words with weights  
- **Negation handling**: flips sentiment for next few tokens (`not happy`, `never good`)  
- **Stemming**: small suffix-based stemmer  
- **Score** = weightedSum / weightTotal  
- **Confidence** = weightTotal / (weightTotal + 5)  
- **Classification thresholds**:  
  - ≥ +0.12 → Positive  
  - ≤ −0.12 → Negative  
  - otherwise → Neutral

---
## ▶️ Run Instructions

```bash
javac -version
java -version
mkdir out

javac src/*.java -d out
chcp 65001
set "JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8"
java -cp out Main

```
---

## ✨ About
MoodTrack is maintained by **Deva Srirama Sai Ganesh Bandaru**.
