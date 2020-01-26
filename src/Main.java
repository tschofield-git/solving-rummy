import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tscho on 11/10/2017.
 */
public class Main {

    static JFrame frame;

    public Main() {
        frame = new Frame();
        frame.setResizable(false);

        Menu menu = new Menu(this);
        frame.setContentPane(menu);

    }

    public static void main(String[] args) {
        new Main();
    }

    public void startGame(int[] selected, int numPlayers) {

        frame.setContentPane(new GamePanel(selected, numPlayers));

    }

    public void simGame(int[] selected, int numPlayers, int numGames){

        int[] numWins = new int[numPlayers];

        boolean verbose = true;

        for(int i = 0; i < numGames; i++){
            SimGame s = new SimGame(selected, numPlayers, verbose);
            int w = s.simulate();
            numWins[w-1]++;
        }

        System.out.println("Final score after " + numGames + " games:");
        for(int i = 0; i < numWins.length; i++){
            System.out.println("Player " + (i+1) + ": " + numWins[i]);
        }

    }
}
