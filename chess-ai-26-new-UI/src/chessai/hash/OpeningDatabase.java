package chessai.hash;

import chessai.Main;
import chessai.game.Move;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class OpeningDatabase {
    public static String fileName = "opening.txt";

    public static HashMap<Long, Integer> database;

    public static void loadData() throws IOException {
        long start = System.currentTimeMillis();
        database = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader("src/chessai/hash/" + fileName));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line);
            if (!st.hasMoreTokens()) {
                continue;
            }
            long hash = Long.parseLong(st.nextToken());
            if (!st.hasMoreTokens()) {
                continue;
            }
            int moveNum = Integer.parseInt(st.nextToken());
            database.put(hash, moveNum);
        }
        Main.println("\nLoaded " + database.size() + " openings in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Move getStoredMove(long hash) {
        if (!database.containsKey(hash)) {
            return null;
        }
        int moveNum = database.get(hash);
        int to = moveNum % 100;
        moveNum /= 100;
        int from = moveNum % 100;
        moveNum /= 100;
        int special = moveNum % 100;
        return new Move(from, to, special);
    }
}
