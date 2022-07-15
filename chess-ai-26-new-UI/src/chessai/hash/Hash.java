package chessai.hash;

import chessai.Main;
import chessai.game.Board;
import chessai.game.Color;
import chessai.pieces.Piece;

import java.util.Arrays;


public class Hash {
    public static long[][] positionValues;

    public static long whiteActiveValue;
    public static long blackActiveValue;

    public static long castleWKValue;
    public static long castleWQValue;
    public static long castleBKValue;
    public static long castleBQValue;
    public static long[] enPassantValues;

    private static final long seed = 0;
    private static long pseudoRandom = 0;

    public static long nextPseudoRandom() {
        long multiplier = 2862933555777941757L;
        long addend = 3037000493L;

        for (int i = 0; i < 10; i++) {
            pseudoRandom *= multiplier;
            pseudoRandom += addend;
        }
        return pseudoRandom;
    }

    public static void calculateHashValues() {
        positionValues = new long[64][20];
        for (int pos = 0; pos < 64; pos++) {
            for (int piece = 0; piece < 20; piece++) {
                positionValues[pos][piece] = nextPseudoRandom();
            }
        }

        whiteActiveValue = nextPseudoRandom();
        blackActiveValue = nextPseudoRandom();

        castleWKValue = nextPseudoRandom();
        castleWQValue = nextPseudoRandom();
        castleBKValue = nextPseudoRandom();
        castleBQValue = nextPseudoRandom();

        enPassantValues = new long[64];
        for (int pos = 0; pos < 64; pos++) {
            enPassantValues[pos] = nextPseudoRandom();
        }
    }

    public static long hashOfBoard(Board board) {
        long hash = 0;
        for (int pos = 0; pos < 64; pos++) {
            Piece piece = board.getPiece(pos);
            if (piece != null) {
                hash ^= positionValues[pos][(piece.color == Color.WHITE ? 10 : 0) + piece.type];
            }
        }
        hash ^= ((board.active == Color.WHITE) ? whiteActiveValue : blackActiveValue);
        if (board.enPassant[board.step] >= 0) {
            hash ^= enPassantValues[board.enPassant[board.step]];
        }
        if (board.castleWK[board.step]) {
            hash ^= castleWKValue;
        }
        if (board.castleWQ[board.step]) {
            hash ^= castleWQValue;
        }
        if (board.castleBK[board.step]) {
            hash ^= castleBKValue;
        }
        if (board.castleBQ[board.step]) {
            hash ^= castleBQValue;
        }
        hash ^= seed;
        return hash;
    }
}
