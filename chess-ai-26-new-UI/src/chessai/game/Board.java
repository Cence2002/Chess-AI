package chessai.game;

import chessai.Main;
import chessai.hash.Hash;
import chessai.hash.RepetitionHistory;
import chessai.hash.Transpositions;
import chessai.pieces.Piece;
import chessai.pieces.Pieces;
import chessai.pieces.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {

    public Piece[] pieces;

    public int[] whitePieces;
    public int[] blackPieces;

    public int step;
    public Color active;

    public boolean[] castleWK;
    public boolean[] castleWQ;
    public boolean[] castleBK;
    public boolean[] castleBQ;
    public int[] enPassant;

    public Piece[] captured;
    public Piece[] promoted;

    public Move[] moves;

    public int whiteKing;
    public int blackKing;

    public boolean calculateHash;

    public boolean useRepetitionHistory;
    public RepetitionHistory repetitionHistory;
    public long[] hashes;

    public boolean useTranspositions;
    public Transpositions transpositions;

    public boolean useOpeningDatabase;

    public long hash;

    public Board(int length, boolean useOpeningDatabase, boolean useRepetitionHistory, boolean useTranspositions) {
        castleWK = new boolean[length];
        castleWQ = new boolean[length];
        castleBK = new boolean[length];
        castleBQ = new boolean[length];
        enPassant = new int[length];
        Arrays.fill(enPassant, -1);

        captured = new Piece[length];
        promoted = new Piece[length];

        moves = new Move[length];

        hash = -1;
        hashes = new long[length];
        Arrays.fill(hashes, -1);

        this.useOpeningDatabase = useOpeningDatabase;

        this.useRepetitionHistory = useRepetitionHistory;
        repetitionHistory = new RepetitionHistory(200000);

        this.useTranspositions = useTranspositions;
        transpositions = new Transpositions(200000);

        calculateHash = useRepetitionHistory || useTranspositions || useOpeningDatabase;
    }

    public void applyFen(String fen) {
        step = 0;
        pieces = new Piece[64];
        whitePieces = new int[10];
        blackPieces = new int[10];
        int x = 0;
        int y = 7;
        int ind;
        for (ind = 0; fen.charAt(ind) != ' '; ind++) {
            if (Character.isDigit(fen.charAt(ind))) {
                x += (fen.charAt(ind) - '0');
                continue;
            }
            if (fen.charAt(ind) == '/') {
                x = 0;
                y--;
                continue;
            }
            Color color = Character.isUpperCase(fen.charAt(ind)) ? Color.WHITE : Color.BLACK;
            Piece piece = null;
            int pos = 8 * y + x;
            switch (Character.toUpperCase(fen.charAt(ind))) {
                case 'K' -> piece = new King(color, pos);
                case 'P' -> piece = new Pawn(color, pos);
                case 'N' -> piece = new Knight(color, pos);
                case 'B' -> piece = new Bishop(color, pos);
                case 'R' -> piece = new Rook(color, pos);
                case 'Q' -> piece = new Queen(color, pos);
            }
            setPiece(pos, piece, false);
            if (color == Color.WHITE) {
                if (piece.type == Pieces.King) {
                    whiteKing = piece.position;
                }
            } else {
                if (piece.type == Pieces.King) {
                    blackKing = piece.position;
                }
            }
            x++;
        }

        ind++;
        active = (fen.charAt(ind) == 'w' ? Color.WHITE : Color.BLACK);

        for (ind += 2; fen.charAt(ind) != ' '; ind++) {
            switch (fen.charAt(ind)) {
                case 'K' -> castleWK[step] = true;
                case 'Q' -> castleWQ[step] = true;
                case 'k' -> castleBK[step] = true;
                case 'q' -> castleBQ[step] = true;
            }
        }

        ind++;
        if (fen.charAt(ind) != '-') {
            enPassant[0] = 8 * (fen.charAt(ind) - 'a') + (fen.charAt(ind + 1) - '1');
        } else {
            enPassant[0] = -1;
        }

        if (calculateHash)
            hash = Hash.hashOfBoard(this);
        hashes[0] = hash;
    }

    public ArrayList<Move> generateMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int pos = 0; pos < 64; pos++) {
            Piece piece = getPiece(pos);
            if (piece != null && piece.color == active) {
                moves.addAll(piece.generateMoves(this));
            }
        }
        return moves;
    }

    public ArrayList<Move> generateCaptures() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int pos = 0; pos < 64; pos++) {
            Piece piece = getPiece(pos);
            if (piece != null && piece.color == active) {
                moves.addAll(piece.generateCaptures(this));
            }
        }
        return moves;
    }

    public boolean testMove(Move move, Color color) {
        makeMove(move, 0);
        boolean isInCheck = isInCheck(color);
        unmakeMove(0);
        return !isInCheck;
    }

    public boolean isInCheck(Color color) {
        return color == Color.WHITE ? attacked(whiteKing, color) : attacked(blackKing, color);
    }

    public boolean attacked(int target, Color color) {
        for (int d = 0; d < 16; d++) {
            for (int i = 0; King.checks[target][d][i] != -1; i++) {
                int to = King.checks[target][d][i];
                if (getPiece(to) != null && getPiece(to).color != color) {
                    if (getPiece(to).attacksTarget(this, target)) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    //moveType: 0-testMove, 1-examinable move, 2-final move in game
    public void makeMove(Move move, int moveType) {
        Main.moveCount++;
        int pieceType = getPiece(move.from).type;
        if (pieceType == Pieces.King) {
            if (active == Color.WHITE) {
                whiteKing = move.to;
            } else {
                blackKing = move.to;
            }
        }
        moves[step + 1] = move;
        if (getPiece(move.to) != null) {
            captured[step + 1] = getPiece(move.to);
            delPiece(move.to, true);
        }
        if (move.special == 0) {
            movePiece(move.from, move.to, true);
        } else {
            switch (move.special) {
                case 1 -> {
                    movePiece(4, 6, true);
                    movePiece(7, 5, true);
                }
                case 2 -> {
                    movePiece(4, 2, true);
                    movePiece(0, 3, true);
                }
                case 3 -> {
                    movePiece(60, 62, true);
                    movePiece(63, 61, true);
                }
                case 4 -> {
                    movePiece(60, 58, true);
                    movePiece(56, 59, true);
                }
                case 5 -> {
                    if (getPiece(move.from).color == Color.WHITE) {
                        captured[step + 1] = getPiece(move.to - 8);
                        delPiece(move.to - 8, true);
                    } else {
                        captured[step + 1] = getPiece(move.to + 8);
                        delPiece(move.to + 8, true);
                    }
                    movePiece(move.from, move.to, true);
                }
                default -> {
                    Color color = getPiece(move.from).color;
                    promoted[step + 1] = getPiece(move.from);
                    delPiece(move.from, true);
                    switch (move.special - 8) {
                        case 3 -> setPiece(move.to, new Knight(color, move.to), true);
                        case 4 -> setPiece(move.to, new Bishop(color, move.to), true);
                        case 5 -> setPiece(move.to, new Rook(color, move.to), true);
                        case 6 -> setPiece(move.to, new Queen(color, move.to), true);
                    }
                }
            }
        }

        active = active.opposite();


        castleWK[step + 1] = (castleWK[(step)]
                && !(move.involves(4))
                && !(move.involves(7))
        );
        castleWQ[step + 1] = (castleWQ[(step)]
                && !(move.involves(0))
                && !(move.involves(4))
        );
        castleBK[step + 1] = (castleBK[(step)]
                && !(move.involves(60))
                && !(move.involves(63))
        );
        castleBQ[step + 1] = (castleBQ[(step)]
                && !(move.involves(56))
                && !(move.involves(60))
        );

        if (getPiece(move.to).type == Pieces.Pawn) {
            if (move.from / 8 == 1 && move.to / 8 == 3) {
                enPassant[step + 1] = move.from + 8;
            }
            if (move.from / 8 == 6 && move.to / 8 == 4) {
                enPassant[step + 1] = move.from - 8;
            }
        }

        step++;

        if (calculateHash) {
            hash ^= Hash.whiteActiveValue;
            hash ^= Hash.blackActiveValue;

            if (castleWK[step] != castleWK[step - 1]) {
                hash ^= Hash.castleWKValue;
            }
            if (castleWQ[step] != castleWQ[step - 1]) {
                hash ^= Hash.castleWQValue;
            }
            if (castleBK[step] != castleBK[step - 1]) {
                hash ^= Hash.castleBKValue;
            }
            if (castleBQ[step] != castleBQ[step - 1]) {
                hash ^= Hash.castleBQValue;
            }

            if (enPassant[step] != -1) {
                hash ^= Hash.enPassantValues[enPassant[step]];
            }
            if (enPassant[step - 1] != -1) {
                hash ^= Hash.enPassantValues[enPassant[step - 1]];
            }
            hashes[step] = hash;
        }

        if (moveType == 2) {
            if (useTranspositions) {
                transpositions.clear();
            }

            if (useRepetitionHistory) {
                repetitionHistory.saveHash(hash);
            }
        }
    }

    public void unmakeMove(int moveType) {
        Move move = moves[step];
        if (getPiece(move.to).type == Pieces.King) {
            if (active == Color.WHITE) {
                blackKing = move.from;
            } else {
                whiteKing = move.from;
            }
        }
        if (move.special == 0) {
            movePiece(move.to, move.from, false);
        } else {
            switch (move.special) {
                case 1 -> {
                    movePiece(6, 4, false);
                    movePiece(5, 7, false);
                }
                case 2 -> {
                    movePiece(2, 4, false);
                    movePiece(3, 0, false);
                }
                case 3 -> {
                    movePiece(62, 60, false);
                    movePiece(61, 63, false);
                }
                case 4 -> {
                    movePiece(58, 60, false);
                    movePiece(59, 56, false);
                }
                case 5 -> {
                    movePiece(move.to, move.from, false);
                }
                default -> {
                    delPiece(move.to, false);
                }
            }
        }

        if (captured[step] != null) {
            setPiece(captured[step].position, captured[step], false);
        }
        if (promoted[step] != null) {
            setPiece(promoted[step].position, promoted[step], false);
        }

        active = active.opposite();

        enPassant[step] = -1;

        captured[step] = null;
        promoted[step] = null;
        moves[step] = null;

        hashes[step] = -1;

        if (moveType == 2) {
            if (useTranspositions) {
                transpositions.clear();
            }

            if (useRepetitionHistory) {
                repetitionHistory.deleteHash(hash);
            }
        }

        step--;

        hash = hashes[step];
    }

    public void modifyHash(int pos) {
        if (calculateHash) {
            hash ^= Hash.positionValues[pos][(getPiece(pos).color == Color.WHITE ? 10 : 0) + getPiece(pos).type];
        }
    }

    public Piece getPiece(int pos) {
        return pieces[pos];
    }

    public void setPiece(int pos, Piece piece, boolean makeMove) {
        pieces[pos] = piece;
        if (makeMove) {
            modifyHash(pos);
        }
        if (getPiece(pos).color == Color.WHITE) {
            whitePieces[getPiece(pos).type]++;
        } else {
            blackPieces[getPiece(pos).type]++;
        }
    }

    public void movePiece(int from, int to, boolean makeMove) {
        if (makeMove) {
            modifyHash(from);
        }
        pieces[from].position = to;
        pieces[to] = pieces[from];
        if (makeMove) {
            modifyHash(to);
        }
        pieces[from] = null;
    }

    public void delPiece(int pos, boolean makeMove) {
        if (getPiece(pos).color == Color.WHITE) {
            whitePieces[getPiece(pos).type]--;
        } else {
            blackPieces[getPiece(pos).type]--;
        }
        if (makeMove) {
            modifyHash(pos);
        }
        pieces[pos] = null;
    }

    @Override
    public Board clone() {
        Board clone = new Board(1000, useOpeningDatabase, useRepetitionHistory, useTranspositions);
        clone.pieces = new Piece[64];
        for (int pos = 0; pos < 64; pos++) {
            if (getPiece(pos) != null) {
                clone.pieces[pos] = getPiece(pos).clone();
            }
        }
        return clone;
    }
}
