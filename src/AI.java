abstract class AI extends Player {

    //No human input
    GamePanel game;
    SimGame sim;

    AI(GamePanel g, int id) {
        super(id);
        game = g;
    }

    AI(SimGame g, int id) {
        super(id);
        sim = g;
    }

    void simTurn() {
        calcDraw();
        calcMeld();
        if (calcDiscard()) {
            if (game != null) game.setCont(false);
            else sim.setCont(false);
        } else {
            if (game != null) game.setCont(true);
            else sim.setCont(true);
        }
    }

    void simDraw(int option) {
        if (game != null) game.reqDraw(this, option);
        else sim.reqDraw(this, option);
    }

    boolean simDiscard(Card c) {
        if (game != null) return game.reqDiscard(this, c);
        else return sim.reqDiscard(this, c);
    }

    abstract void calcDraw();

    abstract void calcMeld();

    abstract boolean calcDiscard();

}
