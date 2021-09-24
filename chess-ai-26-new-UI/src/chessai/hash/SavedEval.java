package chessai.hash;

import chessai.game.Move;

public class SavedEval {
    public long hash; //hash code
    public float eval; //eval result
    public int type; //0-exact value, 1-minimum value (alpha), 2-maximum value (beta)
    public int depth;
    public Move bestMove; //best move if exists

    public SavedEval(long hash, float eval, int type, int depth, Move bestMove) {
        this.hash = hash;
        this.eval = eval;
        this.type = type;
        this.depth = depth;
        this.bestMove = bestMove.clone();
    }
}
