package chessai.pieces;

import chessai.game.Board;
import chessai.game.Move;
import chessai.game.Position;

import java.util.ArrayList;

public class King extends Piece {

    public static int[][] destination;
    public static boolean[][] attacks;
    public static int[][][] checks;

    public King(int color, int position) {
        super(color, Pieces.King, position);
    }

    public static void calculateDestination() {
        Position[] headings = new Position[]{
                new Position(-1, -1),
                new Position(0, -1),
                new Position(1, -1),
                new Position(-1, 0),
                new Position(1, 0),
                new Position(-1, 1),
                new Position(0, 1),
                new Position(1, 1)
        };

        destination = new int[64][10];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int pos = 8 * y + x;
                int ind = 0;
                for (Position direction : headings) {
                    Position to = Position.add(new Position(x, y), direction);
                    if (to.onBoard()) {
                        destination[pos][ind] = to.toNumber();
                        ind++;
                    }
                }
                destination[pos][ind] = -1;
            }
        }
    }

    public static void calculateAttacks() {
        attacks = new boolean[64][64];
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < 64; to++) {
                attacks[from][to] = false;
            }
        }
        for (int pos = 0; pos < 64; pos++) {
            for (int i = 0; destination[pos][i] != -1; i++) {
                int to = destination[pos][i];
                attacks[pos][to] = true;
            }
        }
    }

    public static void calculateChecks() {
        checks = new int[64][16][10];
        Position[] queenDirs = new Position[]{
                new Position(-1, -1),
                new Position(0, -1),
                new Position(1, -1),
                new Position(-1, 0),
                new Position(1, 0),
                new Position(-1, 1),
                new Position(0, 1),
                new Position(1, 1)
        };

        for (int pos = 0; pos < 64; pos++) {
            for (int d = 0; d < 8; d++) {
                int ind = 0;
                Position direction = queenDirs[d];
                for (int i = 1; i <= 8; i++) {
                    Position to = Position.mult(direction, i).add(pos);
                    if (to.onBoard()) {
                        checks[pos][d][ind] = to.toNumber();
                        ind++;
                    } else {
                        break;
                    }
                }
                checks[pos][d][ind] = -1;
            }
        }


        Position[] knightDirs = new Position[]{
                new Position(-1, -2),
                new Position(1, -2),
                new Position(-2, -1),
                new Position(2, -1),
                new Position(-2, 1),
                new Position(2, 1),
                new Position(-1, 2),
                new Position(1, 2)
        };

        for (int pos = 0; pos < 64; pos++) {
            for (int d = 0; d < 8; d++) {
                Position to = Position.add(pos, knightDirs[d]);
                if (to.onBoard()) {
                    checks[pos][d + 8][0] = to.toNumber();
                    checks[pos][d + 8][1] = -1;
                } else {
                    checks[pos][d + 8][0] = -1;
                }
            }
        }
    }

    @Override
    public boolean attacksTarget(Board board, int target) {
        return attacks[position][target];
    }

    @Override
    public ArrayList<Move> generateMoves(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; destination[position][i] != -1; i++) {
            int to = destination[position][i];
            if (board.getPiece(to) != null) {
                if (board.getPiece(to).color != color) {
                    Move move = new Move(position, to);
                    if (board.testMove(move, color)) {
                        moves.add(move);
                    }
                }
                continue;
            }
            Move move = new Move(position, to);
            if (board.testMove(move, color)) {
                moves.add(move);
            }
        }
        if (color == 1) {
            if (board.castleWK[board.step]
                    && board.getPiece(5) == null
                    && board.getPiece(6) == null
                    && !board.attacked(4, color)
                    && !board.attacked(5, color)
                    && !board.attacked(6, color)) {
                moves.add(new Move(4, 6, 1));
            }
            if (board.castleWQ[board.step]
                    && board.getPiece(1) == null
                    && board.getPiece(2) == null
                    && board.getPiece(3) == null
                    && !board.attacked(2, color)
                    && !board.attacked(3, color)
                    && !board.attacked(4, color)) {
                moves.add(new Move(4, 2, 2));
            }
        } else {
            if (board.castleBK[board.step]
                    && board.getPiece(61) == null
                    && board.getPiece(62) == null
                    && !board.attacked(60, color)
                    && !board.attacked(61, color)
                    && !board.attacked(62, color)) {
                moves.add(new Move(60, 62, 3));
            }
            if (board.castleBQ[board.step]
                    && board.getPiece(57) == null
                    && board.getPiece(58) == null
                    && board.getPiece(59) == null
                    && !board.attacked(58, color)
                    && !board.attacked(59, color)
                    && !board.attacked(60, color)) {
                moves.add(new Move(60, 58, 4));
            }
        }
        return moves;
    }

    @Override
    public ArrayList<Move> generateCaptures(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; destination[position][i] != -1; i++) {
            int to = destination[position][i];
            if (board.getPiece(to) != null) {
                if (board.getPiece(to).color != color) {
                    Move move = new Move(position, to);
                    if (board.testMove(move, color)) {
                        moves.add(move);
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public King clone() {
        return new King(color, position);
    }
}
