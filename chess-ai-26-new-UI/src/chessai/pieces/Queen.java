package chessai.pieces;

import chessai.game.Board;
import chessai.game.Move;
import chessai.game.Position;

import java.util.ArrayList;

public class Queen extends Piece {

    public static int[][][] destination = new int[64][8][10];
    public static int[][] attacks;

    public Queen(int color, int position) {
        super(color, Pieces.Queen, position);
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

        for (int pos = 0; pos < 64; pos++) {
            for (int d = 0; d < 8; d++) {
                int ind = 0;
                Position direction = headings[d];
                for (int i = 1; i <= 8; i++) {
                    Position to = Position.mult(direction, i).add(pos);
                    if (to.onBoard()) {
                        destination[pos][d][ind] = to.toNumber();
                        ind++;
                    } else {
                        break;
                    }
                }
                destination[pos][d][ind] = -1;
            }
        }
    }

    public static void calculateAttacks() {
        attacks = new int[64][64];
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < 64; to++) {
                attacks[from][to] = -1;
            }
        }
        for (int pos = 0; pos < 64; pos++) {
            for (int d = 0; d < 8; d++) {
                for (int i = 0; destination[pos][d][i] != -1; i++) {
                    int to = destination[pos][d][i];
                    attacks[pos][to] = d;
                }
            }
        }
    }

    @Override
    public boolean attacksTarget(Board board, int target) {
        int d = attacks[position][target];
        if (d == -1) {
            return false;
        } else {
            for (int i = 0; destination[position][d][i] != -1; i++) {
                int to = destination[position][d][i];
                if (to == target) {
                    return true;
                }
                if (board.getPiece(to) != null) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public ArrayList<Move> generateMoves(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int d = 0; d < 8; d++) {
            for (int i = 0; destination[position][d][i] != -1; i++) {
                int to = destination[position][d][i];
                Piece piece = board.getPiece(to);
                if (piece != null) {
                    if (piece.color != color) {
                        Move move = new Move(position, to);
                        if (board.testMove(move, color)) {
                            moves.add(move);
                        }
                    }
                    break;
                }
                Move move = new Move(position, to);
                if (board.testMove(move, color)) {
                    moves.add(move);
                }
            }
        }
        return moves;
    }

    @Override
    public ArrayList<Move> generateCaptures(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int d = 0; d < 8; d++) {
            for (int i = 0; destination[position][d][i] != -1; i++) {
                int to = destination[position][d][i];
                Piece piece = board.getPiece(to);
                if (piece != null) {
                    if (piece.color != color) {
                        Move move = new Move(position, to);
                        if (board.testMove(move, color)) {
                            moves.add(move);
                        }
                    }
                    break;
                }
            }
        }
        return moves;
    }

    @Override
    public Queen clone() {
        return new Queen(color, position);
    }
}
