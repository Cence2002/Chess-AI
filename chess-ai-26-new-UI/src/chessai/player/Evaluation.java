package chessai.player;

import chessai.Main;
import chessai.game.Board;
import chessai.game.Position;
import chessai.pieces.King;
import chessai.pieces.Pawn;
import chessai.pieces.Piece;
import chessai.pieces.Pieces;

import java.util.HashMap;

public class Evaluation {

    /**
     * TODO
     * tisztek hány helyre léphetnek
     * <p>
     * gyalogszerkezet:
     * visszamaradt vmennyi minusz
     * szomszédai előrébb, hátrébbik előtt ellenséges gyalog
     * végjátékban fontos tolni őket
     * <p>
     * futó:
     * -(hány saját gyalog árnyékolja előre)
     * nem végjátékban ellenfél lyuk jó, végjátékban ellenhél gyalog futó színén
     * <p>
     * <p>
     * bástya:
     * nyílt vonal +0.2
     * félig nyílt vonal +0.1
     * duplázva +0.3
     * <p>
     * vezér:
     * hány szabályos lépése van (hány helyre tud lépni)
     * lépésenként +0.03
     */

    public static float endgameStart = 15;

    public static float evaluate(Board board) {
        Main.evalCount++;

        float whiteEval = 0;
        float blackEval = 0;

        float whitePieceValue = calculatePieceValue(board, 1);
        float blackPieceValue = calculatePieceValue(board, 0);

        //how endgame-y is the position for both sides...
        float whiteEndgameWeight = calculateEndgameWeight(board, 1, whitePieceValue);
        float blackEndgameWeight = calculateEndgameWeight(board, 0, blackPieceValue);
        float totalEndgameWeight = (whiteEndgameWeight + blackEndgameWeight) / 2;

        whiteEval += whitePieceValue;
        blackEval += blackPieceValue;

        whiteEval += calculatePositionValue(board, 1, totalEndgameWeight);
        blackEval += calculatePositionValue(board, 0, totalEndgameWeight);

        whiteEval += calculateEndgame(board, 1, whitePieceValue, blackPieceValue, blackEndgameWeight);
        blackEval += calculateEndgame(board, 0, blackPieceValue, whitePieceValue, whiteEndgameWeight);

        whiteEval += calculateKingSafety(board, 1);
        blackEval += calculateKingSafety(board, 0);

        whiteEval += calculatePawnStructure(board, 1);
        blackEval += calculatePawnStructure(board, 0);

        return (whiteEval - blackEval) * (board.active == 1 ? 1 : -1);
    }

    public static float calculatePieceValue(Board board, int color) {
        int[] pieceCount = (color == 1) ? board.whitePieces : board.blackPieces;
        float sum = 0;
        for (int type = 1; type <= 6; type++) {
            sum += typeValue(type) * pieceCount[type];
        }
        return sum;
    }

    public static float calculatePositionValue(Board board, int color, float endgameWeight) {
        float sum = 0;
        for (int pos = 0; pos < 64; pos++) {
            Piece piece = board.getPiece(pos);
            if (piece != null && piece.color == color) {
                sum += positionValue(piece, endgameWeight);
            }
        }
        return sum;
    }

    public static float calculateEndgameWeight(Board board, int color, float pieceValue) {
        int[] pieceCount = (color == 1) ? board.whitePieces : board.blackPieces;
        return 1 - Math.min(1, (pieceValue - pieceCount[2]) / endgameStart);
    }

    //color is the friendly one
    public static float calculateEndgame(Board board, int color, float pieceValue, float opponentPieceValue, float endgameWeight) {
        if (endgameWeight < 0.0001f || pieceValue <= opponentPieceValue + 2) {
            return 0;
        }
        int kingPos = (color == 1) ? board.whiteKing : board.blackKing;
        int opponentKingPos = (color == 1) ? board.blackKing : board.whiteKing;
        float sum = 0;
        sum += distance[opponentKingPos][36] * 0.12f;
        sum += (14 - distance[opponentKingPos][kingPos]) * 0.08f;

        return sum * endgameWeight;
    }

    public static float calculateKingSafety(Board board, int color) {
        int kingPos = (color == 1) ? board.whiteKing : board.blackKing;
        float sum = 0;
        for (int i = 0; King.destination[kingPos][i] != -1; i++) {
            int to = King.destination[kingPos][i];
            Piece piece = board.getPiece(to);
            if (piece != null && piece.color == color) {
                sum += 0.02 / typeValue(piece.type);
            }
        }
        return sum;
    }

    // minél kevesebb gyalogsziget
    // izolált -0.1
    // szabad 0.2
    // védett szabad 0.3
    // ék 0.2
    // dupla -0.1
    // dupla izolált -0.4

    public static float calculatePawnStructure(Board board, int color) {
        int[][] destinations = (color == 1) ? Pawn.destinationWhite : Pawn.destinationBlack;
        float sum = 0;
        for (int pos = 0; pos < 64; pos++) {
            Piece piece = board.getPiece(pos);
            if (piece != null && piece.type == Pieces.Pawn && piece.color == color) {
                for (int i = 0; i < 2; i++) {
                    int to = destinations[pos][i];
                    if (to == -1) {
                        continue;
                    }
                    Piece protect = board.getPiece(to);
                    if (protect != null && protect.color == color) {
                        if (protect.type == Pieces.Pawn) {
                            sum += 0.02f;
                        } else if (protect.type > 2) {
                            sum += 0.01f;
                        }
                    }
                }
            }
        }
        return sum;
    }


    public static float typeValue(int type) {
        switch (type) {
            case Pieces.Pawn -> {
                return 1;
            }
            case Pieces.Knight -> {
                return 3f;
            }
            case Pieces.Bishop -> {
                return 3.2f;
            }
            case Pieces.Rook -> {
                return 4.8f;
            }
            case Pieces.Queen -> {
                return 9.2f;
            }
        }
        return 0;
    }

    public static float positionValue(Piece piece, float endgameWeight) {
        int x = piece.position % 8;
        int y = piece.position / 8;
        if (piece.color == 1) {
            y = 7 - y;
        }
        int pos = 8 * y + x;
        float pawnWeight = endgameWeight < 0.1f ? 1 : 3.5f;
        float otherWeight = endgameWeight < 0.3f ? 1 : 0.2f;
        switch (piece.type) {
            case Pieces.Pawn -> {
                return pawns[pos] / 100 * pawnWeight;
            }
            case Pieces.Knight -> {
                return knights[pos] / 100 * otherWeight;
            }
            case Pieces.Bishop -> {
                return bishops[pos] / 100 * otherWeight;
            }
            case Pieces.Rook -> {
                return rooks[pos] / 100 * otherWeight;
            }
            case Pieces.Queen -> {
                return queens[pos] / 100 * otherWeight;
            }
            case Pieces.King -> {
                return kingsEnd[pos] / 100 * endgameWeight + kings[pos] / 100 * (1 - endgameWeight);
            }
        }
        return 0;
    }

    public static int[][] distance;

    public static void calculateDistances() {
        distance = new int[64][64];
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < 64; to++) {
                Position p1 = new Position(from);
                Position p2 = new Position(to);
                distance[from][to] = Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
            }
        }
    }

    public static float[] pawns = {
            70, 70, 70, 70, 70, 70, 70, 70,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            0, 5, 10, 25, 25, 10, 5, 0,
            -5, 0, 0, 20, 20, 0, 0, -5,
            0, 5, 5, 0, 0, 5, 5, 0,
            5, 10, 10, 5, 5, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    public static float[] knights = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 5, 20, 25, 25, 20, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 0, 15, 15, 15, 15, 0, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50,
    };

    public static float[] bishops = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 10, 5, 10, 10, 5, 10, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 15, 10, 5, 5, 10, 15, -10,
            -20, -10, -15, -10, -10, -15, -10, -20,
    };

    public static float[] rooks = {
            5, 5, 5, 5, 5, 5, 5, 5,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 5, 5, 0, 0, -5,
            5, 0, 0, 15, 15, 10, 0, 5
    };

    public static float[] queens = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
    };

    public static float[] kings = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            10, 10, -10, -10, -10, -10, 10, 10,
            15, 20, 20, 0, 0, 0, 25, 20
    };

    public static float[] kingsEnd = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };
}
