import java.util.ArrayList;

public class Deck extends ArrayList<Card>{

    boolean orderKnown;

    public Deck(Card[] cards){
        for (int i = 0; i < 52; i++) {
            add(cards[i]);
        }
        orderKnown = false;
    }

    //Removes a random card from the deck and returns it
    public Card draw(){
        if(orderKnown){
            Card c = get(0);
            remove(0);
            return c;
        }
        int ran = (int) Math.floor(Math.random() * size());
        Card card = get(ran);
        remove(get(ran));
        return card;
    }

    public void setOrderKnown(){
        orderKnown = true;
    }

}
