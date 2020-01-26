import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Player implements Comparable {

    private String name;
    private int points;
    private Hand hand;
    private ArrayList<Card> table;
    boolean penaltyCheck;
    private int ID;

    ArrayList[] playerHands;
    int[] handSizes;
    ArrayList<Card> knownCards;
    Stack<Card> pile;

    Player(int id) {
        table = new ArrayList<>();
        knownCards = new ArrayList<>();
        playerHands = new ArrayList[4];
        handSizes = new int[]{7, 7, 7, 7};
        for (int i = 0; i < 4; i++) playerHands[i] = new ArrayList<Card>();
        ID = id;
    }

    int getID() {
        return ID;
    }

    void drawFromDeck(Card c) {
        hand.add(c);
    }

    void drawFromPile(Card c) {
        hand.add(c);
    }

    void drawPile(Stack<Card> pile) {
        penaltyCheck = true;
        hand.addAll(pile);
    }

    boolean meld(List<Card> cards) {
        if (!hand.containsAll(cards))
            return false;
        for (Card c : cards) {
            c.setMeldable(false);
            c.setSingle(false);
            c.removeListener();
        }
        table.addAll(cards);
        hand.removeAll(cards);
        tidyTable();
        if (cards.size() >= 3) penaltyCheck = false;
        return true;
    }

    private void tidyTable() {

        Collections.sort(table);

    }

    ArrayList<Card> getTable() {
        tidyTable();
        return table;
    }

    Hand getHand() {
        return hand;
    }

    boolean discard(Card c) {
        hand.scanSingles();
        if (c.isSingle()) return false;
        hand.remove(c);
        if (penaltyCheck) {
            points -= 50;
            //System.out.println("No meld, score -50!");
        }
        return true;
    }

    void initHand(Hand h) {
        hand = h;
        //System.out.println(hand.getHand());
        hand.detectSets();
    }

    int countSelected() {
        return hand.countSelected();
    }


    boolean isSingle() {
        if (getHand().countSelected() != 1) return false;

        ArrayList<Card> singles = getHand().scanSingles();
        return singles.contains(getHand().getSelected().get(0));
    }

    void calcScore() {
        for (Card c : table) points += c.getScore();
        for (Card c : hand) points -= c.getScore();
    }

    int getScore() {
        return points;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(((Player) o).getScore(), getScore());
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void knowCard(Card c) {
        if (!knownCards.contains(c)) knownCards.add(c);
    }

    void setPile(Stack<Card> pile) {
        this.pile = pile;
    }

    void reset() {
        table = new ArrayList<>();
        knownCards = new ArrayList<>();
        playerHands = new ArrayList[4];
        for (int i = 0; i < 4; i++) playerHands[i] = new ArrayList<Card>();
    }

    void updatePlayerHand(int playerID, Card c, boolean adding) {
        if (playerID == getID()) return; //don't need to keep track of cards in own hand
        if (adding) playerHands[playerID].add(c);
        else playerHands[playerID].remove(c);
    }

    void updateHandSizes(int playerID, int size) {
        if (playerID == getID()) return;
        handSizes[playerID] = size;
    }

    public String getAI() {
        return null;
    }
}
