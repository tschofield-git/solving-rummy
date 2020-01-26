import java.util.ArrayList;

public class AI1 extends AI {

    public AI1(GamePanel g) {
        super(g);
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
            if (meld(a))
                game.logMeld(a);
        }
        ArrayList<Card> singles = scanSingles();
        while (singles.size() > 0) {
            for (Card c : singles) {
                ArrayList<Card> single = new ArrayList<>(0);
                single.add(c);
                if (meld(single)) game.logMeld(single);
            }
            singles = scanSingles();
        }
    }

    @Override
    boolean calcDiscard() {
        int ran = (int) (Math.floor(Math.random() * getHand().size()));
        while (!simDiscard(getHand().get(ran))) calcMeld();
        return true;
    }
}
