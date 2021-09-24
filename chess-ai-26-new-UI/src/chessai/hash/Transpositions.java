package chessai.hash;

import chessai.game.Move;

public class Transpositions {
    public int size;

    public SavedEval[] table;

    public Transpositions(int size) {
        this.size = size;
        this.table = new SavedEval[size];
    }

    public void clear() {
        this.table = new SavedEval[size];
    }

    public int getIndex(long hash) {
        return (int) ((hash % size) + size) % size;
    }

    public void setEval(long hash, float eval, int type, int depth, Move bestMove) {
        SavedEval savedEval = new SavedEval(hash, eval, type, depth, bestMove);
        table[getIndex(hash)] = savedEval;
    }

    public float lookupEval(long hash, int depth, float alpha, float beta) {
        SavedEval savedEval = table[getIndex(hash)];
        if (savedEval == null || savedEval.hash != hash || savedEval.depth < depth) {
            return Integer.MIN_VALUE;
        }
        switch (savedEval.type) {
            //exact
            case 0 -> {
                return savedEval.eval;
            }
            //alpha
            case 1 -> {
                if (savedEval.eval <= alpha) {
                    return alpha;
                }
            }
            //beta
            case 2 -> {
                if (savedEval.eval >= beta) {
                    return beta;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    public Move getBestMove(long hash) {
        return table[getIndex(hash)].bestMove;
    }

    public float getEval(long hash) {
        return table[getIndex(hash)].eval;
    }
}
