import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PredictiveAI extends NaiveAI {

    public PredictiveAI(GamePanel g, int id) {
        super(g, id);
    }

    public PredictiveAI(SimGame g, int id) {
        super(g, id);
    }

    @Override
    void calcMeld() {

        //Consider holding sets to prevent other players from adding to them

        ArrayList<ArrayList<Card>> sets = getHand().detectSets();

        /* if a set longer than 5 is available, the middle cards can be melded 'safely',
         i.e. nobody else would be able to gain points by adding to them */

        int panicFactor = 0;
        for (int i : handSizes) panicFactor += Math.max(0, (7-i));
        //System.out.println(panicFactor);

        for (ArrayList<Card> al : sets) {
            if (penaltyCheck || Math.random()*20 < panicFactor) { // another player is close to ending the round -> meld all sets
                if (meld(al)) {
                    if (game != null) game.logMeld(al);
                    else sim.meld(al);
                }
            } else if (al.size() >= 5) {
                List<Card> a = al.subList(1, al.size() - 1);
                if (meld(a)) {
                    if (game != null) game.logMeld(a);
                    else sim.meld(a);
                }
            }
        }

        ArrayList<Card> singles = getHand().scanSingles();
        if (Math.random()*22 - 2 < panicFactor) {
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

        } else {

            //If melding a single while holding the card that follows/precedes it, melding that single is safe
            ArrayList<Card> toMeld = new ArrayList<>();
            for (Card c : singles) {
                if (getHand().contains(c.getPrev()) || getHand().contains(c.getNext())) {
                    toMeld.add(c);
                }
            }
            if (toMeld.size() > 0 && meld(toMeld)) {
                if (game != null) game.logMeld(toMeld);
                else sim.meld(toMeld);
            }
        }

    }

    @Override
    double[] calcDiscardVals() {

        double[] vals = new double[getHand().size()];

        ArrayList<Card> singles = getHand().scanSingles();

        HashSet<Card> good = new HashSet<>();

        //Consider cards in pile (avoiding adding cards which start to form a set)

        for (Card c : pile) {
            good.add(c.getPrev());
            good.add(c.getNext());
        }


        //Consider other players' hand (are they waiting for a specific discard)

        for (ArrayList<Card> al : playerHands) {
            for (Card c : al) {
                good.add(c.getPrev());
                good.add(c.getNext());
            }
        }

        int panicFactor = 0;
        for(int i : handSizes) {
            panicFactor += (7 - i);
        }

        Hand h = getHand();
        int index = 0;
        for (Card c : h) {
            //if other players are close to winning, prefer discarding more expensive cards
            int val = Math.abs(panicFactor - c.getScore());
            if (h.contains(c.getNext())) val *= 3;
            if (h.contains(c.getPrev())) val *= 3;
            if (good.contains(c))
                val *= 1.5;//Another player is waiting for this card - avoid putting it in pile
            if(singles.contains(c)) val=1000; //can't discard a card which is a valid singleton
            vals[index++] = val;
        }


        return vals;
    }

    @Override
    public String getAI(){
        return "Predictive AI";
    }


}
