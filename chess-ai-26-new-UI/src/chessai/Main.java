package chessai;

import chessai.game.*;
import chessai.hash.*;
import chessai.pieces.*;
import chessai.player.*;
import processing.core.PApplet;
import processing.core.PImage;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;

public class Main extends PApplet {

    Color color = Color.WHITE;
    boolean playAgainst = true;
    boolean moveAutomatically = true;
    public static boolean useOpeningBias = true;

    //visual settings
    int gridSize = 100;
    int lightSquareColor = color(240, 220, 180);
    int darkSquareColor = color(180, 140, 100);
    int frameRate = 20;

    //main agent and board
    AI mainAi;
    Board mainBoard;

    //maximal depth to search to
    public static int maxDepth = 10;
    //maximal response time in milliseconds
    long maxThinkingTime = 400;

    public static boolean gameEnded = false;
    final HashMap<Integer, PImage> images = new HashMap<>();

    public static int moveCount = 0;
    public static int evalCount = 0;

    int pressedSquare = -1;
    int frames = -1;


    public void settings() {
        size(gridSize * 8, gridSize * 8);
    }

    void loadImages(String root) {
        images.put(10 + Pieces.King, loadImage(root + "white-king.png"));
        images.put(10 + Pieces.Pawn, loadImage(root + "white-pawn.png"));
        images.put(10 + Pieces.Knight, loadImage(root + "white-knight.png"));
        images.put(10 + Pieces.Bishop, loadImage(root + "white-bishop.png"));
        images.put(10 + Pieces.Rook, loadImage(root + "white-rook.png"));
        images.put(10 + Pieces.Queen, loadImage(root + "white-queen.png"));

        images.put(Pieces.King, loadImage(root + "black-king.png"));
        images.put(Pieces.Pawn, loadImage(root + "black-pawn.png"));
        images.put(Pieces.Knight, loadImage(root + "black-knight.png"));
        images.put(Pieces.Bishop, loadImage(root + "black-bishop.png"));
        images.put(Pieces.Rook, loadImage(root + "black-rook.png"));
        images.put(Pieces.Queen, loadImage(root + "black-queen.png"));
    }

    void resizeImages() {
        for (PImage image : images.values()) {
            image.resize(gridSize, gridSize);
        }
    }

    public void setup() {
        background(0);
        noStroke();
        noFill();
        drawSquares();

        precomputeData();
        initGame();

        frameRate(frameRate);
    }

    public void draw() {
        background(0);
        drawSquares();
        drawSquareNames();
        showLastMove();
        if (pressedSquare == -1) {
            showLegalMoves(mousePos());
        } else {
            showLegalMoves(pressedSquare);
        }
        drawPieces();
        drawSelectedPiece();
        if (frameCount > 1 && !gameEnded && moveAutomatically) {
            autoMove();
        }
    }

    void precomputeData() {
        String root = "assets/";
        loadImages(root);
        resizeImages();

        King.calculateDestination();
        Pawn.calculateDestination();
        Knight.calculateDestination();
        Bishop.calculateDestination();
        Rook.calculateDestination();
        Queen.calculateDestination();

        King.calculateAttacks();
        Pawn.calculateAttacks();
        Knight.calculateAttacks();
        Bishop.calculateAttacks();
        Rook.calculateAttacks();
        Queen.calculateAttacks();

        King.calculateChecks();
        Evaluation.calculateDistances();
        Hash.calculateHashValues();

        try {
            OpeningDatabase.loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initGame() {
        test(2e5);
        //println();

        String fen;
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";
        //fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        //fen = "r1b1kb1r/p1pp2pp/2n5/3q2P1/1p3Pn1/2PBPN2/PP5P/RNBQ1RK1 w kq -";
        //fen = "rn3k2/pppqp2r/4bnQp/3P2p1/4p3/2N5/PPP2PPP/R1B1KB1R b KQ -";
        //fen = "2r2rk1/5ppp/p2p4/1b2pPb1/1P2P1Pq/1B1P1R2/1B4QP/6RK w - -";
        ///fen = "2k5/8/8/2p5/3rK3/8/8/2R5 w - -";
        //fen = "r2qkbnr/pp2pppp/2p5/n7/3P2P1/3BP3/PP3P1P/1RBQK1NR w - -";
        //fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";

        //fen = "qk5/8/8/8/8/4K3/8/8 w - -";
        //fen = "rrk5/8/8/8/8/4K3/8/8 w - -";
        //fen = "rk5/8/8/8/8/4K3/8/8 w - -";
        //fen = "bbk5/8/8/8/8/4K3/8/8 w - -";
        //fen = "bnk5/8/8/8/8/4K3/8/8 w - -";

        //fen = "8/3K4/4P3/8/8/8/6k1/7q w - -";
        //fen = "8/8/8/1k6/8/8/K1r5/8 b - -";
        //fen = "8/k7/8/8/6r1/7r/K7/8 b - -";
        //fen = "8/1k4p1/R7/2p5/2B5/p2Pr3/Kb6/8 b - -";

        //fen = "1r2r3/pp1R3p/4kp2/2p5/4P3/5P2/PK3P1P/3R4 w - -";

        //fen = "8/k7/3p4/p2P1p2/P2P1P2/8/8/K7 w - -";

        //fen = "7K/8/k1P5/7p/8/8/8/8 w - -";

        //fen = "2B2b2/rpK1kPR1/1P5P/4N1p1/3N2Pn/4P3/5Q2/1b2n3 w - -";
        //fen = "8/kPR5/5Q2/3B4/8/8/6K1/8 w - -";
        //fen = "b2qkQ1r/3p1ppp/p1n1pn2/1p6/2N1b3/BP6/P1P2PPP/R3KB1R b KQkq -";
        //fen = "5k2/8/5pK1/5PbP/2Bn4/8/8/8 b - -";

        //fen = "8/4q3/2p5/8/pk6/1n2N3/1Q6/1K6 w - -";

        //fen = "1K1RQ3/1BPP2qP/P3P3/1P2n3/3Np3/1b6/ppp3pp/1k3r2 b - -";
        //fen = "3QR1K1/Pq2PPB1/3P3P/3n2P1/3pN3/6b1/pp3ppp/2r3k1 b - -";
        //fen = "2r3k1/pp3ppp/6b1/3pN3/3n2P1/3P3P/Pq2PPB1/3QR1K1 b - -";

        mainAi = new AI(fen, 1000);
        mainBoard = mainAi.board;
    }

    void drawSquares() {
        rectMode(CENTER);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                boolean lightSquare = (x + y) % 2 == 1;
                int pos = 8 * y + x;
                fill(lightSquare ? lightSquareColor : darkSquareColor);
                rect(getX(pos), getY(pos), gridSize, gridSize);
            }
        }
    }

    void drawSquareNames() {
        textAlign(RIGHT, TOP);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                boolean lightSquare = (x + y) % 2 == 1;
                int pos = 8 * y + x;
                fill(lightSquare ? darkSquareColor : lightSquareColor);
                textSize(gridSize * 0.14f);
                text(Position.toString(pos), getX(pos) + gridSize * 0.45f, getY(pos) - gridSize * 0.49f);
            }
        }
    }

    void drawPieces() {
        imageMode(CENTER);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int pos = 8 * y + x;
                if (pos == pressedSquare) {
                    continue;
                }
                Piece piece = mainBoard.getPiece(pos);
                if (piece == null) {
                    continue;
                }
                image(images.get((piece.color == Color.WHITE ? 10 : 0) + piece.type),
                        getX(piece.position), getY(piece.position));
            }
        }
    }

    void drawSelectedPiece() {
        if (pressedSquare != -1) {
            Piece piece = mainBoard.getPiece(pressedSquare);
            if (piece != null) {
                image(images.get((piece.color == Color.WHITE ? 10 : 0) + piece.type),
                        mouseX, mouseY);
            }
        }
    }

    void showLegalMoves(int pos) {
        Piece piece = mainBoard.getPiece(pos);
        if (piece != null && piece.color == mainBoard.active) {
            ArrayList<Move> moves = piece.generateMoves(mainBoard);
            fill(0, 0, 255, 50);
            if (moves != null) {
                for (Move m : moves) {
                    rect(getX(m.to), getY(m.to), gridSize, gridSize);
                }
            }
        }
    }

    void showLastMove() {
        Move lastMove = mainBoard.moves[mainBoard.step];
        if (lastMove != null) {
            fill(0, 255, 0, 50);
            rect(getX(lastMove.from), getY(lastMove.from), gridSize, gridSize);
            rect(getX(lastMove.to), getY(lastMove.to), gridSize, gridSize);
        }
    }

    void autoMove() {
        if ((mainBoard.active != color) == playAgainst) {
            if (frames == -1) {
                frames = 0;
            } else {
                frames++;
            }
            if (frames == 2) {
                mainAi.AIMove(maxThinkingTime);
                frames = -1;
            }
        }
    }

    float getX(int pos) {
        int x = (pos % 8);
        if (color == Color.BLACK) {
            x = 7 - x;
        }
        return x * gridSize + gridSize / 2f;
    }

    float getY(int pos) {
        int y = 7 - pos / 8;
        if (color == Color.BLACK) {
            y = 7 - y;
        }
        return y * gridSize + gridSize / 2f;
    }

    int mousePos() {
        int mx = mouseX / gridSize;
        int my = 7 - mouseY / gridSize;
        if (color == Color.BLACK) {
            mx = 7 - mx;
            my = 7 - my;
        }
        int pos = 8 * my + mx;
        if (validPos(pos)) {
            return pos;
        }
        return -1;
    }

    boolean validPos(int pos) {
        return pos >= 0 && pos < 64;
    }

    public void mousePressed() {
        int pos = mousePos();
        if (pos != -1) {
            Piece piece = mainBoard.getPiece(pos);
            if (piece == null) {
                if (pressedSquare != -1) {
                    ArrayList<Move> moves = mainBoard.getPiece(pressedSquare).generateMoves(mainBoard);
                    for (Move m : moves) {
                        if (m.to == pos) {
                            return;
                        }
                    }
                    pressedSquare = -1;
                }
            } else if (piece.color == mainBoard.active) {
                if (pos == pressedSquare) {
                    pressedSquare = -1;
                } else {
                    pressedSquare = pos;
                }
            } else {
                pressedSquare = -1;
            }
        }
    }

    public void mouseReleased() {
        if (!validPos(pressedSquare)) {
            return;
        }
        int released = mousePos();
        if (!validPos(released)) {
            return;
        }

        Piece piece = mainBoard.getPiece(pressedSquare);
        if (piece == null || piece.color != mainBoard.active) {
            pressedSquare = -1;
            return;
        }

        if (released == pressedSquare) {
            return;
        }

        ArrayList<Move> moves = piece.generateMoves(mainBoard);
        for (Move move : moves) {
            move.score = move.evaluate(mainBoard);
        }
        Collections.sort(moves);

        for (Move m : moves) {
            if (m.to == released) {
                mainBoard.makeMove(m, 2);
                break;
            }
        }
        pressedSquare = -1;
    }

    public void keyPressed() {
        switch (keyCode) {
            case KeyEvent.VK_SPACE -> {
                if (mainBoard.moves[1] != null) {
                    mainAi.board.unmakeMove(2);
                }
                if (moveAutomatically) {
                    if (mainBoard.moves[1] != null) {
                        mainAi.board.unmakeMove(2);
                    }
                }
                gameEnded = false;
                pressedSquare = -1;
            }
            case KeyEvent.VK_ENTER -> {
                mainAi.AIMove(maxThinkingTime);
                pressedSquare = -1;
            }
        }
    }

    void test(double maxSolution) {
        long start = System.currentTimeMillis();
        ArrayList<PerfTest> tests = new ArrayList<>(Arrays.asList(
                new PerfTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", 0, 1),
                new PerfTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", 1, 20),
                new PerfTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", 2, 400),
                new PerfTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", 3, 8902),
                new PerfTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", 4, 197281),
                new PerfTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", 5, 4865609),

                new PerfTest("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 0, 1),
                new PerfTest("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 1, 48),
                new PerfTest("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 2, 2039),
                new PerfTest("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 3, 97862),
                new PerfTest("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 4, 4085603),

                new PerfTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 0, 1),
                new PerfTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 1, 14),
                new PerfTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 2, 191),
                new PerfTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 3, 2812),
                new PerfTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 4, 43238),
                new PerfTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 5, 674624),

                new PerfTest("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq -", 0, 1),
                new PerfTest("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq -", 1, 6),
                new PerfTest("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq -", 2, 264),
                new PerfTest("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq -", 3, 9467),
                new PerfTest("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq -", 4, 422333),

                new PerfTest("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ -", 0, 1),
                new PerfTest("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ -", 1, 6),
                new PerfTest("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ -", 2, 264),
                new PerfTest("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ -", 3, 9467),
                new PerfTest("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ -", 4, 422333),

                new PerfTest("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ -", 0, 1),
                new PerfTest("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ -", 1, 44),
                new PerfTest("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ -", 2, 1486),
                new PerfTest("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ -", 3, 62379),
                new PerfTest("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ -", 4, 2103487),

                new PerfTest("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - -", 0, 1),
                new PerfTest("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - -", 1, 46),
                new PerfTest("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - -", 2, 2079),
                new PerfTest("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - -", 3, 89890),
                new PerfTest("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - -", 4, 3894594)
        ));
        Collections.sort(tests);

        Main.println();
        for (PerfTest test : tests) {
            if (test.solution > maxSolution) {
                break;
            }
            if (test.test() == test.solution) {
                print(test.depth + " ");
            } else {
                System.err.format("!!!!! Failed at depth %d: %s  found: %d/%d\n", test.depth, test.fen, test.test(), test.solution);
                return;
            }
        }
        System.out.format("\nFinished testing in %dms\n", System.currentTimeMillis() - start);
    }

    public static void main(String[] args) {
        PApplet.main("chessai.Main");
    }
}
