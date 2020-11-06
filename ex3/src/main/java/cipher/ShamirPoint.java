package cipher;

/**
 * Classe 'wrapper' pour un point de l'algorithme de Shamir.
 * */
public class ShamirPoint {

    private int x;
    private int y;

    public ShamirPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
