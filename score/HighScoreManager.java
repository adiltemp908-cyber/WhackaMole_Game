package score;

import exceptions.HighScoreException;
import java.io.*;
import java.util.*;

public class HighScoreManager {

    private static final String SCORE_FILE = "scores.dat";

    public void saveScores(List<PlayerScore> scores) throws HighScoreException {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(new ArrayList<>(scores));
        } catch(Exception e) {
            throw new HighScoreException("Failed to save scores", e);
        }
    }

  @SuppressWarnings("unchecked")
public List<PlayerScore> loadScores() throws HighScoreException {
    File file = new File(SCORE_FILE);
    if(!file.exists()) return new ArrayList<>();
    try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        return (List<PlayerScore>) ois.readObject();
    } catch(Exception e) {
        throw new HighScoreException("Failed to load scores", e);
    }
}
        
}
