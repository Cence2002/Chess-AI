package chessai.game;

public class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(int pos) {
        this.x = pos % 8;
        this.y = pos / 8;
    }

    public Position add(int pos) {
        x += pos % 8;
        y += pos / 8;
        return this;
    }

    public static Position add(Position p1, Position p2) {
        return new Position(p1.x + p2.x, p1.y + p2.y);
    }

    public static Position add(int p1, Position p2) {
        return new Position(p1 % 8 + p2.x, p1 / 8 + p2.y);
    }

    public static Position mult(Position p, int c) {
        return new Position(p.x * c, p.y * c);
    }

    public boolean onBoard() {
        return x >= 0 && y >= 0 & x < 8 && y < 8;
    }

    public int toNumber() {
        return 8 * y + x;
    }

    public static String toString(int pos) {
        int x = pos % 8;
        int y = pos / 8;
        String s = "";
        s += (char) ('a' + x);
        s += (char) ('1' + y);
        return s;
    }

    public Position clone() {
        return new Position(x, y);
    }
}
