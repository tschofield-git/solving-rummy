import java.util.ArrayList;

public class Pack  {

    private Card[] cards;

    public Pack() {
        initCards();
    }

    private void initCards() {
        cards = new Card[52];
        String[] suits = {"Spades", "Hearts", "Clubs", "Diamonds"};
        for (int i = 0; i < 4; i++) {
            cards[i * 13] = new Card(suits[i], 1, i * 13);
            cards[i * 13].setScore(15);
            for (int j = 1; j < 13; j++) {
                cards[i * 13 + j] = new Card(suits[i], (j + 1), i * 13 + j);
                if (j < 9) cards[i * 13 + j].setScore(5);
                else cards[i * 13 + j].setScore(10);
                cards[i * 13 + j].setPrev(cards[i * 13 + j - 1]);
                cards[i * 13 + j - 1].setNext(cards[i * 13 + j]);
            }
            cards[i * 13].setPrev(cards[i * 13 + 12]);//Ace's previous card is the King of the same suit
            cards[i * 13 + 12].setNext(cards[i * 13]);//King's next card is the Ace of the same suit
        }
    }

    public Card[] getCards(){
        return cards;
    }

    public void setGame(GamePanel g){
        for(Card c : cards) c.setGame(g);
    }


}
