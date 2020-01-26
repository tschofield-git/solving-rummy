import jdk.nashorn.internal.runtime.ECMAException;

import java.util.*;

public class Hand extends ArrayList<Card> {

    private Player[] players;

    Hand(Player[] p) {
        this.players = p;
    }

    public String getHand() {
        if (isEmpty()) return "Hand Empty";
        sortHand();
        StringBuilder h = new StringBuilder(get(0).getCardName());
        for (int i = 1; i < size(); i++) {
            h.append(", ").append(get(i).getCardName());
        }
        return h.toString();
    }

    ArrayList<ArrayList<Card>> detectSets() {
        ArrayList<ArrayList<Card>> sets = new ArrayList<>(0);
        if (size() < 3) return sets;
        ArrayList<Card> temp = new ArrayList<>(0);
        sortHand();
        int len = 1;
        //System.out.println("hand size = " + size() + ", content: " + getHand());
        for (int i = 0; i < size(); i++) {
            Card target = get(i);
            temp.add(target);
            while (contains(target.getNext())) {
                target = target.getNext();
                temp.add(target);
                len++;
                if (len >= 3) {
                    //System.out.println("Set found, length " + len + " between " + temp.get(0).getCardName() + " and " + target.getCardName());
                    Collections.sort(temp);
                    checkMelds(temp);
                    sets.add((ArrayList<Card>) temp.clone());
                }
            }
            temp = new ArrayList<>(0);
            len = 1;
        }

        sets.sort(Comparator.comparingInt(ArrayList::size));

        return sets;
    }

    ArrayList<Card> scanSingles() {
        ArrayList<Card> singles = new ArrayList<>();
        if (isEmpty()) return singles;
        Set<Card> all = allSingles();
        for (Card c : this) {
            if (all.contains(c)) {
                singles.add(c);
                c.setSingle(true);
            } else {
                c.setSingle(false);
            }
        }
        return singles;
    }

    Set<Card> allSingles() {
        Set<Card> singles = new HashSet<>();
        for (Player p : players) {
            for (Card c : p.getTable()) {
                singles.add(c.getNext());
                singles.add(c.getPrev());
            }
        }
        return singles;
    }

    private void checkMelds(ArrayList<Card> temp) {
        for (Card c : temp) c.setMeldable(true);
    }

    void sortHand() {
        try {
            Collections.sort(this);
        } catch (Exception e) {
            //System.out.println("Hand empty: cannot sort");
        }
    }

    int countSelected() {
        int count = 0;
        for (Card c : this) if (c.isSelected()) count++;
        return count;
    }

    boolean isSelectedSet() {
        if (countSelected() < 3) return false;
        ArrayList<Card> cards = getSelected();
        return cards.size() >= 3 && detectSets().contains(cards);
    }

    ArrayList<Card> getSelected() {
        ArrayList<Card> cards = new ArrayList<>(countSelected());
        for (Card c : this) if (c.isSelected()) cards.add(c);
        return cards;
    }

    int absoluteHandValue() {
        int val = 0;
        ArrayList<Card> considered = (ArrayList<Card>) this.clone();
        for (ArrayList<Card> al : detectSets())
            for (Card c : al) {
                val += c.getScore();
                considered.remove(c);
            }
        for (Card c : scanSingles()) {
            val += c.getScore();
            considered.remove(c);
        }

        for (Card c : considered) {
            val -= (20 - c.getScore());
        }

        return val;
    }
}