import java.util.ArrayList;

class RandomAI extends AI {

    RandomAI(GamePanel g, int id) {
        super(g, id);
    }

    RandomAI(SimGame g, int id) {
        super(g,id);
    }

    @Override
    void calcDraw() {
        //Placeholder while developing
        int ran = (int) (Math.floor(Math.random() * 3));
        simDraw(ran);
    }

    @Override
    void calcMeld() {
        ArrayList<ArrayList<Card>> sets = getHand().detectSets();
        for (ArrayList<Card> a : sets) {
            if (meld(a)) {
                if (game != null) game.logMeld(a);
                else sim.meld(a);
            }
        }
        ArrayList<Card> singles = getHand().scanSingles();
        while (singles.size() > 0) {
            for (Card c : singles) {
                ArrayList<Card> single = new ArrayList<>(0);
                single.add(c);
                if (meld(single)) {
                    if (game != null) game.logMeld(single);
                    else sim.meld(single);
                }
            }
            singles = getHand().scanSingles();
        }
    }

    @Override
    boolean calcDiscard() {
        while (getHand().size() > 0 && !simDiscard(getHand().get((int) (Math.floor(Math.random() * getHand().size()))))) calcMeld();
        return true;
    }

    public String getAI() {
        return "Random AI";
    }
}
