package chessai.hash;

public class RepetitionHistory {
    public int size;

    public long[] table;

    public RepetitionHistory(int size) {
        this.size = size;
        this.table = new long[size];
    }

    public void clear() {
        this.table = new long[size];
    }

    public int getIndex(long hash) {
        return (int) ((hash % size) + size) % size;
    }

    public void saveHash(long hash) {
        table[getIndex(hash)] = hash;
    }

    public void deleteHash(long hash) {
        table[getIndex(hash)] = 0;
    }

    public boolean isRepeating(long hash) {
        return table[getIndex(hash)] == hash;
    }
}

