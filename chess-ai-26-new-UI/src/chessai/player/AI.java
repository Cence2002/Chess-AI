package chessai.player;

import chessai.Main;
import chessai.game.Board;
import chessai.game.Move;
import chessai.game.Position;
import chessai.hash.OpeningDatabase;
import chessai.pieces.Piece;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Collections;

public class AI extends PApplet {
    public final float infinity = 1000000;
    public final float mateScore = 1000;

    public Board board;

    public Move bestMove;
    public float bestEval;

    public Move bestMoveInIteration;
    public float bestEvalInIteration;

    public boolean useIterativeDeepening = true;
    public boolean stopAlphaBeta;
    public long startTime;
    public long maxTime;

    public boolean useMoveOrdering = true;

    public boolean useCaptures = true;
    public int quiescenceDepth = 8;

    public boolean useOpeningDatabase = true;
    public boolean useRepetitionHistory = true;
    public boolean useTranspositions = true;


    public AI(String fen, int length) {
        board = new Board(length, useOpeningDatabase, useRepetitionHistory, useTranspositions);
        board.applyFen(fen);
    }

    public int perfTest(int depth) {
        if (depth == 0) {
            return 1;
        }
        int sum = 0;
        for (int pos = 0; pos < 64; pos++) {
            Piece piece = board.getPiece(pos);
            if (piece != null && piece.color == board.active) {
                ArrayList<Move> moves = piece.generateMoves(board);
                if (depth == 1) {
                    sum += moves.size();
                    continue;
                }
                for (Move move : moves) {
                    board.makeMove(move, 0);
                    sum += perfTest(depth - 1);
                    board.unmakeMove(0);
                }
            }
        }
        return sum;
    }

    public void AIMove(long millis) {
        long start = System.currentTimeMillis();
        Main.moveCount = 0;
        Main.evalCount = 0;
        findBestMove(Main.maxDepth, millis);
        System.out.format("%d ply -> moves: %d evals: %d time: %dms\n\n", board.step + 1, Main.moveCount, Main.evalCount, System.currentTimeMillis() - start);
        applyBestMove();
    }

    public void applyBestMove() {
        if (bestMove == null) {
            Main.println("GAME OVER");
            Main.gameEnded = true;
        } else {
            board.makeMove(bestMove, 2);
        }
    }

    public void findBestMove(int maxDepth, long millis) {
        Main.println();
        if (useOpeningDatabase) {
            Move move = OpeningDatabase.getStoredMove(board.hashes[board.step]);
            if (move != null) {
                Main.println("bookMove: " + Position.toString(move.from), Position.toString(move.to));
                bestMove = move.clone();
                return;
            }
        }
        if (useIterativeDeepening) {
            maxTime = millis;
            startTime = System.currentTimeMillis();
            bestMove = null;
            bestMoveInIteration = null;
            stopAlphaBeta = false;
            for (int depth = 2; depth <= maxDepth && !stopAlphaBeta; depth++) {
                Main.println(depth + ":");
                minimax(0, depth, -infinity, infinity);
                if (!stopAlphaBeta && bestMoveInIteration != null) {
                    bestMove = bestMoveInIteration.clone();
                    bestEval = bestEvalInIteration;
                }
                if (bestEval > mateScore / 2) {
                    break;
                }
            }
        } else {
            bestMove = null;
            bestMoveInIteration = null;
            minimax(0, maxDepth, -infinity, infinity);
            bestMove = bestMoveInIteration.clone();
            bestEval = bestEvalInIteration;
        }
    }

    //alpha: best that current player can achieve so far
    //beta: best that opponent can achieve so far
    public float minimax(int depth, int maxDepth, float alpha, float beta) {
        int searchDepth = maxDepth - depth;
        if (stopAlphaBeta) {
            return 0;
        }

        if (depth <= maxDepth / 2) {
            if (System.currentTimeMillis() - startTime > maxTime) {
                stopAlphaBeta = true;
                return 0;
            }
        }

        if (depth > 0) {
            //Skip if mate found earlier
            alpha = Math.max(alpha, -mateScore + depth);
            beta = Math.min(beta, mateScore - depth);
            if (alpha >= beta) {
                return alpha;
            }

            //the exact same position was before -> draw by repetition
            if (useRepetitionHistory && depth <= maxDepth / 2) {
                if (board.repetitionHistory.isRepeating(board.hash)) {
                    return 0;
                }
            }
        }

        if (useTranspositions) {
            float transPositionEval = board.transpositions.lookupEval(board.hash, searchDepth, alpha, beta);
            if (transPositionEval != Integer.MIN_VALUE) {
                if (depth == 0) {
                    bestMoveInIteration = board.transpositions.getBestMove(board.hash);
                    bestEvalInIteration = board.transpositions.getEval(board.hash);
                }
                return transPositionEval;
            }
        }

        //End of search
        if (depth >= maxDepth) {
            if (useCaptures) {
                return quiescence(0, quiescenceDepth, alpha, beta);
            } else {
                return Evaluation.evaluate(board);
            }
        }

        ArrayList<Move> moves = board.generateMoves();
        if (moves.size() == 0) {
            //current player is in mate -> bad
            //mate later -> better but still bad
            if (board.isInCheck(board.active)) {
                return -mateScore + depth;
            }
            //opponent is in mate -> good
            //mate later -> worse but still good
            if (board.isInCheck(1 - board.active)) {
                return mateScore - depth;
            }
            //draw -> neutral
            return 0;
        }

        //order moves
        if (useMoveOrdering) {
            for (Move move : moves) {
                move.score = move.evaluate(board);
            }
            /*if (searchDepth >= 4) {
                for (Move move : moves) {
                    board.makeMove(move, 0);
                    float eval = -miniAlphaBeta(0, 2, -beta, -alpha);
                    board.unmakeMove(0);
                    move.score = eval;
                }
            }*/
            if (depth == 0 && bestMove != null) {
                for (Move move : moves) {
                    if (move.equals(bestMove)) {
                        move.score = infinity;
                    }
                }
            }
            //Collections.shuffle(moves);
            Collections.sort(moves);
        }


        int transpositionType = 1;
        Move bestMoveInPosition = null;

        //test each move
        for (Move move : moves) {
            board.makeMove(move, 1);
            float eval = -minimax(depth + 1, maxDepth, -beta, -alpha);
            board.unmakeMove(1);

            //if new best move in this position
            if (eval > alpha) {
                alpha = eval;
                //new best move in the position
                bestMoveInPosition = move.clone();
                transpositionType = 0;
                //new best move in the root
                if (depth == 0 && !stopAlphaBeta) {
                    Main.println(Position.toString(move.from), Position.toString(move.to), eval);
                    bestMoveInIteration = move.clone();
                    bestEvalInIteration = alpha;
                }
            }

            //worse for the opponent, than the best he can insure, so he wouldn't allow it...
            if (alpha >= beta) {
                if (useTranspositions) {
                    board.transpositions.setEval(board.hash, beta, 2, searchDepth, move.clone());
                }
                return beta;
            }
        }

        if (bestMoveInPosition != null && useTranspositions) {
            board.transpositions.setEval(board.hash, alpha, transpositionType, searchDepth, bestMoveInPosition);
        }
        return alpha;
    }

    public float quiescence(int depth, int maxDepth, float alpha, float beta) {
        float eval = Evaluation.evaluate(board);
        if (eval > alpha) {
            alpha = eval;
        }
        if (alpha >= beta) {
            return beta;
        }

        if (depth >= maxDepth) {
            return eval;
        }

        ArrayList<Move> moves = board.generateCaptures();
        if (useMoveOrdering) {
            for (Move move : moves) {
                move.score = move.evaluate(board);
            }
            Collections.sort(moves);
        }

        //test each move
        for (Move move : moves) {
            board.makeMove(move, 0);
            eval = -quiescence(depth + 1, maxDepth, -beta, -alpha);
            board.unmakeMove(0);
            //if new best move in this position
            if (eval > alpha) {
                alpha = eval;
            }
            //worse for the opponent, than the best he can insure, so he wouldn't allow it...
            if (alpha >= beta) {
                return alpha;
            }
        }
        return alpha;
    }
}
