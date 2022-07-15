package chessai.game;

import chessai.player.Evaluation;

public class Move implements Comparable<Move> {
    public int from;
    public int to;
    public int special = 0;
    public float score = 0;

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public Move(int from, int to, int special) {
        this.from = from;
        this.to = to;
        this.special = special;
    }

    public boolean involves(int pos) {
        return from == pos || to == pos;
    }

    public float evaluate(Board board) {
        float eval = 0;
        if (board.getPiece(to) == null) {
            eval += Evaluation.typeValue(board.getPiece(from).type);
        } else {
            eval += 10 * Evaluation.typeValue(board.getPiece(to).type) -
                    3 * Evaluation.typeValue(board.getPiece(from).type);
        }
        if (special >= 11) {
            eval += Evaluation.typeValue(special - 8) * 20;
        }
        Color pieceColor = board.getPiece(from).color;
        int opponentKingPos = (pieceColor == Color.WHITE) ? board.blackKing : board.whiteKing;
        if (board.getPiece(from).attacksTarget(board, opponentKingPos)) {
            eval += 5;
        }
        return eval;
    }

    public Move clone() {
        Move clone = new Move(from, to, special);
        clone.score = score;
        return clone;
    }

    public boolean equals(Move move) {
        return from == move.from && to == move.to && special == move.special;
    }

    @Override
    public int compareTo(Move move) {
        return (int) Math.signum(move.score - score);
    }
}
