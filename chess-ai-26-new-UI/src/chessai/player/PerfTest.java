package chessai.player;

public class PerfTest implements Comparable<PerfTest> {
    public String fen;
    public int depth;
    public int solution;

    public PerfTest(String fen, int depth, int solution) {
        this.fen = fen;
        this.depth = depth;
        this.solution = solution;
    }

    public int test() {
        AI ai = new AI(fen, 1000);
        return ai.perfTest(depth);
    }

    @Override
    public int compareTo(PerfTest test) {
        return this.solution - test.solution;
    }
}
