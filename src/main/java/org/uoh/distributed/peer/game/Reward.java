package org.uoh.distributed.peer.game;

public abstract class Reward extends GameObject {
    public Reward(int x, int y) {
        super(x, y);
    }


    /**
     * Implements the bijective algorithm,
     *
     * @implNote
     * This is only implemented for the {@link Reward} class
     * so that players can overlap.
     *
     * Source <a href="https://www.cs.upc.edu/~alvarez/calculabilitat/enumerabilitat.pdf">enumerability.pdf</a>
     *
     * @return unique hash of x and y
     */
    @Override
    public int hashCode() {
        int tmp = (y + ((x + 1) / 2));
        return x + (tmp * tmp);
    }
}
