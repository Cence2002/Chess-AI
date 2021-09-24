package chessai.pieces;

import chessai.game.Board;
import chessai.game.Move;

import java.util.ArrayList;

public abstract class Piece {
    public int position;
    /*
    0...63
    0-a1
    7-h1
    8-a2
    ...
    56-a8
    63-h8
     */
    public int color;
    /*
    0-black
    1-white
     */
    public int type;
    /*
    0-none
    1-king
    2-pawn
    3-knight
    4-bishop
    5-rook
    6-queen
     */

    public Piece(int color, int type, int position) {
        this.color = color;
        this.type = type;
        this.position = position;
    }

    public abstract boolean attacksTarget(Board board, int target);

    public abstract ArrayList<Move> generateMoves(Board board);

    public abstract ArrayList<Move> generateCaptures(Board board);

    public abstract Piece clone();
}
