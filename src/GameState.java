import java.util.ArrayList;

public class GameState implements Comparable{

    Hand hand;
    ArrayList<Card> table;

    private double evalHandValue(){
        double val = 0.0;
        //max value is 1, min value is 0

        return 0.0;
    }

    private double evalTableValue(){

        return 0.0;
    }

    public double evaluate(){
        //Consider cards in hand
        //Consider cards on table

        //for each other player consider their cards in hand + table
        //consider likelihood of opponent ending a round
        return 0.0;
    }

    @Override
    public int compareTo(Object o) {
        return Double.compare(evaluate(), ((GameState)o).evaluate());
    }
}
