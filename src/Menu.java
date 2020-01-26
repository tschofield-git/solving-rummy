import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.io.File;

/**
 * Created by tscho on 11/10/2017.
 */
public class Menu extends JPanel implements ActionListener{

    private Main main;
    private JToggleButton[][] selections = new JToggleButton[4][3];
    private int[] selected = {0, -1, -1, -1};
    private int numPlayers = 1;
    private JButton gameStart, simGame;

    Menu(Main m){
        main = m;

        setSize(1200, 800);
        setLayout(null);
        setBackground(Color.white);
        setOpaque(true);

        Font titleFont = null;
        try {
            titleFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/fonts/title.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }


        JLabel title = new JLabel("Rummy 500");
        title.setFont(titleFont.deriveFont(60f));
        title.setSize(title.getPreferredSize());
        title.setLocation(600-(title.getWidth())/2, 80);

        JPanel center = new JPanel();
        center.setLayout(null);
        center.setSize(600, 400);
        center.setLocation(300, 200);
        //center.setBorder(new LineBorder(Color.black));
        center.setBackground(Color.white);

        String[] options = {"Random AI", "Naive AI", "Predictive AI"};

        for(int i = 0; i < 4; i++){
            JLabel playerLabel = new JLabel("Player " + (i + 1));
            playerLabel.setLocation(20, i*80);
            playerLabel.setFont(playerLabel.getFont().deriveFont(24f));
            playerLabel.setSize(playerLabel.getPreferredSize());

            center.add(playerLabel);

            for(int j = 0; j < options.length; j++) {
                selections[i][j] = new JToggleButton(options[j]);
                selections[i][j].setSize(120, 30);
                selections[i][j].setLocation(160 + 140*j, i*80);
                selections[i][j].addActionListener(this);
                center.add(selections[i][j]);
            }

        }

        gameStart = new JButton("Start Game");
        gameStart.setEnabled(false);
        gameStart.setSize(120, 30);
        gameStart.setLocation(160, 370);
        gameStart.addActionListener(this);
        center.add(gameStart);

        simGame = new JButton("Sim Game");
        simGame.setEnabled(false);
        simGame.setSize(120, 30);
        simGame.setLocation(320, 370);
        simGame.addActionListener(this);
        center.add(simGame);

        add(title);
        add(center);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==gameStart) {
            main.startGame(selected, numPlayers);
        }else if(e.getSource()==simGame){
            int i = Integer.parseInt(JOptionPane.showInputDialog("Enter number of games to simulate: "));
            main.simGame(selected, numPlayers, i);
        }else {
            for (int j = 0; j < 4; j++) {
                if (Arrays.asList(selections[j]).contains(e.getSource())) {
                    for (int i = 0; i < selections[j].length; i++) {
                        if (e.getSource().equals(selections[j][i])) {
                            if (selected[j] == -1) numPlayers++;
                            selected[j] = i;
                            if (numPlayers > 1) {
                                gameStart.setEnabled(true);
                                simGame.setEnabled(true);
                            }
                        } else {
                            selections[j][i].setSelected(false);
                        }
                    }
                }
            }
        }
    }
}
