package chessai.pieces;

import chessai.game.Board;
import chessai.game.Move;
import chessai.game.Position;

import java.util.ArrayList;

public class Pawn extends Piece {

    public static int[][] destinationWhite;
    public static int[][] destinationBlack;
    public static boolean[][] attacksWhite;
    public static boolean[][] attacksBlack;

    public Pawn(int color, int position) {
        super(color, Pieces.Pawn, position);
    }

    public static void calculateDestination() {
        Position[] whiteHeadings = new Position[]{
                new Position(-1, 1),
                new Position(1, 1),
                new Position(0, 1),
                new Position(0, 2)
        };
        Position[] blackHeadings = new Position[]{
                new Position(-1, -1),
                new Position(1, -1),
                new Position(0, -1),
                new Position(0, -2)
        };

        destinationWhite = new int[64][10];
        destinationBlack = new int[64][10];
        for (int pos = 0; pos < 64; pos++) {
            int ind = 0;
            for (Position direction : whiteHeadings) {
                Position to = Position.add(pos, direction);
                if (to.onBoard()) {
                    destinationWhite[pos][ind] = to.toNumber();
                    ind++;
                } else {
                    destinationWhite[pos][ind] = -1;
                    ind++;
                }
            }
            destinationWhite[pos][ind] = -1;

            ind = 0;
            for (Position direction : blackHeadings) {
                Position to = Position.add(pos, direction);
                if (to.onBoard()) {
                    destinationBlack[pos][ind] = to.toNumber();
                    ind++;
                } else {
                    destinationBlack[pos][ind] = -1;
                    ind++;
                }
            }
            destinationBlack[pos][ind] = -1;
        }
    }

    public static void calculateAttacks() {
        attacksWhite = new boolean[64][64];
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < 64; to++) {
                attacksWhite[from][to] = false;
            }
        }
        for (int pos = 0; pos < 64; pos++) {
            for (int i = 0; i < 2; i++) {
                int to = destinationWhite[pos][i];
                if (to != -1) {
                    attacksWhite[pos][to] = true;
                }
            }
        }

        attacksBlack = new boolean[64][64];
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < 64; to++) {
                attacksBlack[from][to] = false;
            }
        }
        for (int pos = 0; pos < 64; pos++) {
            for (int i = 0; i < 2; i++) {
                int to = destinationBlack[pos][i];
                if (to != -1) {
                    attacksBlack[pos][to] = true;
                }
            }
        }
    }

    @Override
    public boolean attacksTarget(Board board, int target) {
        return color == 1 ? attacksWhite[position][target] : attacksBlack[position][target];
    }

    @Override
    public ArrayList<Move> generateMoves(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        int[][] dir = (color == 1 ? destinationWhite : destinationBlack);
        int y = position / 8;
        for (int i = 0; i < 2; i++) {
            int to = dir[position][i];
            if (to == -1) continue;
            if (board.getPiece(to) == null) {
                if (board.enPassant[board.step] == to) {
                    Move move = new Move(position, to, 5);
                    if (board.testMove(move, color)) {
                        moves.add(move);
                    }
                }
            } else {
                if (board.getPiece(to).color != color) {
                    if (color == 1 ? (y < 6) : (y > 1)) {
                        Move move = new Move(position, to);
                        if (board.testMove(move, color)) {
                            moves.add(move);
                        }
                    } else {
                        for (int promote = 11; promote <= 14; promote++) {
                            Move move = new Move(position, to, promote);
                            if (board.testMove(move, color)) {
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }
        int to1 = dir[position][2];
        if (board.getPiece(to1) != null) {
            return moves;
        } else {
            if (color == 1 ? (y < 6) : (y > 1)) {
                Move move = new Move(position, to1);
                if (board.testMove(move, color)) {
                    moves.add(move);
                }

                if (color == 1 ? (y == 1) : (y == 6)) {
                    int to2 = dir[position][3];
                    if (board.getPiece(to2) != null) {
                        return moves;
                    }
                    Move move2 = new Move(position, to2);
                    if (board.testMove(move2, color)) {
                        moves.add(move2);
                    }
                }
            } else {
                Move move = new Move(position, to1, 11);
                if (board.testMove(move, color)) {
                    moves.add(move);
                    for (int promote = 12; promote <= 14; promote++) {
                        moves.add(new Move(position, to1, promote));
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public ArrayList<Move> generateCaptures(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        int[][] dir = (color == 1 ? destinationWhite : destinationBlack);
        int y = position / 8;
        for (int i = 0; i < 2; i++) {
            int to = dir[position][i];
            if (to == -1) continue;
            if (board.getPiece(to) == null) {
                if (board.enPassant[board.step] == to) {
                    Move move = new Move(position, to, 5);
                    if (board.testMove(move, color)) {
                        moves.add(move);
                    }
                }
            } else {
                if (board.getPiece(to).color != color) {
                    if (color == 1 ? (y < 6) : (y > 1)) {
                        Move move = new Move(position, to);
                        if (board.testMove(move, color)) {
                            moves.add(move);
                        }
                    } else {
                        for (int promote = 11; promote <= 14; promote++) {
                            Move move = new Move(position, to, promote);
                            if (board.testMove(move, color)) {
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public Pawn clone() {
        return new Pawn(color, position);
    }
}
