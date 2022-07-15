package chessai.pieces;

import chessai.game.Board;
import chessai.game.Color;
import chessai.game.Move;
import chessai.game.Position;

import java.util.ArrayList;

public class Knight extends Piece {

    public static int[][] destination = new int[64][10];
    public static boolean[][] attacks;

    public Knight(Color color, int position) {
        super(color, Pieces.Knight, position);
    }

    public static void calculateDestination() {
        Position[] headings = new Position[]{
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
            int ind = 0;
            for (Position direction : headings) {
                Position to = Position.add(pos, direction);
                if (to.onBoard()) {
                    destination[pos][ind] = to.toNumber();
                    ind++;
                }
            }
            destination[pos][ind] = -1;
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

    @Override
    public boolean attacksTarget(Board board, int target) {
        return attacks[position][target];
    }

    @Override
    public ArrayList<Move> generateMoves(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; destination[position][i] != -1; i++) {
            int to = destination[position][i];
            Piece piece = board.getPiece(to);
            if (piece != null) {
                if (piece.color != color) {
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
    public Knight clone() {
        return new Knight(color, position);
    }
}
