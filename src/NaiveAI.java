import java.util.ArrayList;
import java.util.Set;

class NaiveAI extends AI {

    private double w1 = 5, w2 = 3, w3 = 1, w4 = 20;

    boolean tune = true; //set to true to randomise weights

    NaiveAI(GamePanel g, int id) {
        super(g, id);
    }

    NaiveAI(SimGame g, int id) {
        super(g, id);
        if(tune) {
            w1 -= Math.random() * 3;
            w2 -= Math.random() * 2;
            w3 -= Math.random();
            w4 -= Math.random() * 10;
        }
    }

    @Override
    void calcDraw() {

        int maxAt = 0;
        double[] vals = calcValues();

        for (int i = 0; i < 3; i++) {
            maxAt = vals[i] > vals[maxAt] ? i : maxAt;
        }

        simDraw(maxAt);

    }

    @Override
    void calcMeld() {

        //Melds everything when possible

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

        do {
            calcMeld();
            if (getHand().isEmpty()) return false;
        } while (!simDiscard(getHand().get(minDisc())));

        return true;
    }

    int minDisc() {

        int minAt = 0;
        double[] vals = calcDiscardVals();

        for (int i = 0; i < getHand().size(); i++) {
            minAt = vals[i] < vals[minAt] ? i : minAt;
        }

        return minAt;

    }

    //decide quality of deck and pile
    double[] calcValues() {
        double deckVal = 0.0;


        //If the next card in the deck is known, use the knowledge
        boolean flipped = false;
        if (game != null) {
            if (game.isDeckFlipped()) {
                flipped = true;
                Hand withTopDeck = (Hand) getHand().clone();
                if (game.topDeck() != null)
                    withTopDeck.add(game.topDeck());
                deckVal = withTopDeck.absoluteHandValue() - getHand().absoluteHandValue();
            }
        } else if (sim.isDeckFlipped()) {
            flipped = true;
            Hand withTopDeck = (Hand) getHand().clone();
            if (sim.topDeck() != null)
                withTopDeck.add(sim.topDeck());
            deckVal = withTopDeck.absoluteHandValue() - getHand().absoluteHandValue();
        }
        if (!flipped) {
            ArrayList<Card> vgood = new ArrayList<>();
            ArrayList<Card> good = new ArrayList<>();
            ArrayList<Card> considered = new ArrayList<>();

            //scan hand to find cards which would create a set
            //scan hand to find cards which follow or precede a card in the hand

            Hand h = getHand();

            for (Card c : h) {
                //if card already considered, don't analyse
                if (considered.contains(c)) continue;

                considered.add(c);
                Card prev = c.getPrev();
                int len = 1;
                while (h.contains(prev)) {
                    if (!considered.contains(prev)) considered.add(prev);
                    prev = prev.getPrev();
                    len++;
                }

                Card next = c.getNext();
                while (h.contains(next)) {
                    if (!considered.contains(next)) considered.add(next);
                    next = next.getNext();
                    len++;
                }

                //if a card is known to be in someone's hand, the table or the pile, it can't be in the deck
                if (len > 1) {
                    if (!knownCards.contains(next)) vgood.add(next); //card will form a set
                    if (!knownCards.contains(prev)) vgood.add(prev);
                } else if (len == 1) {
                    if (!knownCards.contains(next)) good.add(next); //card will build towards a set
                    if (!knownCards.contains(prev)) good.add(prev);
                }
            }


            //for each card that is unknown and junk, lower deck value
            Pack p = new Pack();

            Set<Card> singles = getHand().allSingles();

            for (Card c : p.getCards()) {
                if (!knownCards.contains(c)) {
                    if (singles.contains(c)) {
                        good.add(c);
                    }
                } else if (!vgood.contains(c) && !good.contains(c)) {
                    deckVal -= (w4 - c.getScore() * w3);
                }
            }

            for (Card c : vgood) deckVal += c.getScore() * w1;
            for (Card c : good) deckVal += c.getScore() * w2;

            //Average card value
            deckVal /= (52 - knownCards.size());
        }
        //System.out.println("Value of deck calculated to be " + deckVal);

        //CALCULATE PILE TOP

        Hand withTopPile = (Hand) getHand().clone();
        if (!pile.isEmpty()) withTopPile.add(pile.peek());

        //If no set can be melded, player will receive a penalty
        double topPileVal = withTopPile.absoluteHandValue() - getHand().absoluteHandValue();

        //System.out.println("Value of pile top calculated to be " + topPileVal);

        //CALCULATE WHOLE PILE

        Hand withPile = (Hand) getHand().clone();
        if (!pile.isEmpty()) withPile.addAll(pile);

        double pileVal = withPile.absoluteHandValue() - getHand().absoluteHandValue();

        //If no set can be melded, player will receive a penalty
        if (withPile.detectSets().size() == 0) pileVal -= 50;

        //System.out.println("Value of pile calculated to be " + pileVal);

        //System.out.println(deckVal + "/" + topPileVal + "/" + pileVal);
        return new double[]{deckVal, topPileVal, pileVal};
    }

    double[] calcDiscardVals() {
        double[] vals = new double[getHand().size()];

        Hand h = getHand();
        int index = 0;
        for (Card c : h) {
            double val = c.getScore() * w3;
            if (h.contains(c.getNext())) val += c.getScore() * w2;
            if (h.contains(c.getPrev())) val += c.getScore() * w2;
            vals[index++] = val;
        }

        return vals;
    }

    public double[] getFactors() {
        return new double[]{w1, w2, w3, w4};
    }

    @Override
    public String getAI() {
        return "Naive AI";
    }
}
