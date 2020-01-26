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

        Menu menu = new Menu(this);
        frame.setContentPane(menu);

    }

    public static void main(String[] args) {
        new Main();
    }

    public void startGame(int[] selected, int numPlayers) {

        //A real game (i.e. with inputs required) has value 1.
        //Value 0 means the deck and all draws are randomly generated
        frame.setContentPane(new GamePanel(selected, numPlayers));

    }
}
