import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Player implements Comparable{

    private String name;
    private int points;
    private Hand hand;
    private ArrayList<ArrayList<Card>> table;
    private Player[] players;
    private boolean penaltyCheck;

    public Player() {
        table = new ArrayList<>(0);

    }

    public void drawFromDeck(Card c) {
        hand.add(c);
    }

    public void drawFromPile(Card c) {
        hand.add(c);
    }

    public void drawPile(Stack<Card> pile) {
        penaltyCheck = true;
        hand.addAll(pile);
    }

    public boolean meld(ArrayList<Card> cards) {
        if (!hand.containsAll(cards))
            return false;
        for (Card c : cards) {
            c.setMeldable(false);
            c.setSingle(false);
            c.removeListener();
        }
        table.add(cards);
        hand.removeAll(cards);
        tidyTable();
        penaltyCheck = false;
        return true;
    }

    private void tidyTable() {
        //ArrayList<Card> all = new ArrayList<>(0);
        for (ArrayList<Card> c : table) {
            Collections.sort(c);
        }
        /*
        table = new ArrayList<>(0);
        Collections.sort(all);
        for(int i = 0; i < all.size(); i++){
            Card target = all.get(i);
            ArrayList<Card> temp = new ArrayList<>();
            temp.add(target);
            while(all.contains(target.getNext())){
                target = target.getNext();
                i++;
                temp.add(target);
            }
            Collections.sort(temp);
            table.add(temp);
        }
        */
    }

    public ArrayList<ArrayList<Card>> getTable() {
        tidyTable();
        return table;
    }

    public Hand getHand() {
        return hand;
    }

    public boolean discard(Card c) {
        if(c.isSingle()) return false;
        hand.remove(c);
        if(penaltyCheck) {
            points -= 50;
            System.out.println("No meld, score -50!");
        }
        return true;
    }

    public void initHand(Hand h) {
        hand = h;
        //System.out.println(hand.getHand());
        hand.detectSets();
    }

    public void initPlayers(Player[] p) {
        players = p;
    }

    public void updatePlayer(Player p, int id) {
        players[id] = p;
    }

    public int countSelected() {
        return hand.countSelected();
    }


    public ArrayList<Card> scanSingles() {
        ArrayList<Card> singles = new ArrayList<Card>();
        for (Card c : hand) {
            boolean add = false;
            outer:
            for (Player p : players) {
                for (ArrayList<Card> al : p.table) {
                    //System.out.println(Arrays.toString(al.toArray()));
                    //System.out.println("Comparing " + c + " to " + al.get(0).getPrev() + " and " + al.get(al.size() - 1).getNext());
                    if (c == al.get(0).getPrev() || c == al.get(al.size() - 1).getNext()) {
                        add = true;
                        break outer;
                    }
                }
            }
            if (add) singles.add(c);
        }

        return singles;
    }

    public boolean isSingle() {
        if (getHand().countSelected() != 1) return false;

        ArrayList<Card> singles = scanSingles();
        highlightSingles(singles);
        return singles.contains(getHand().getSelected().get(0));
    }

    public void highlightSingles(ArrayList<Card> al) {
        for (Card c : al)
            c.setSingle(true);
    }

    public void calcScore(){
        for(ArrayList<Card> al: table){
            for(Card c: al){
                points += c.getScore();
            }
        }
        for(Card c: hand){
            points -= c.getScore();
        }
    }

    public int getScore(){
        return points;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(getScore(), ((Player) o).getScore());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPenaltyCheck() {
        return penaltyCheck;
    }
}
