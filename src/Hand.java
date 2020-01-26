import java.awt.image.AreaAveragingScaleFilter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Hand extends ArrayList<Card> {


    public Hand() {
    }

    public String getHand() {
        sortHand();
        String h = get(0).getCardName();
        for (int i = 1; i < size(); i++) {
            h += ", " + get(i).getCardName();
        }
        return h;
    }

    public ArrayList<ArrayList<Card>> detectSets() {
        sortHand();
        ArrayList<ArrayList<Card>> sets = new ArrayList<>(0);
        ArrayList<Card> temp = new ArrayList<>(0);
        int len = 1;
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
            temp = new ArrayList<Card>(0);
            len = 1;
        }

        sets.sort(Comparator.comparingInt(ArrayList::size));

        return sets;
    }

    private void checkMelds(ArrayList<Card> temp){
        for(Card c:temp) c.setMeldable(true);
    }

    public void sortHand() {
        Collections.sort(this);
    }

    public int countSelected() {
        int count = 0;
        try{
            for (Card c : this) if (c.isSelected()) count++;
        }catch (Exception e){
            return 0;
        }
        return count;
    }

    public boolean isSelectedSet() {
        if(countSelected() < 3) return false;
        ArrayList<Card> cards = getSelected();
        if (cards.size() < 3) return false;
        return detectSets().contains(cards);
    }

    public ArrayList<Card> getSelected(){
        ArrayList<Card> cards = new ArrayList<Card>(countSelected());
        try{
            for (Card c : this) if (c.isSelected()) cards.add(c);
        }catch (Exception e){
            return cards;
        }
        return cards;
    }
}