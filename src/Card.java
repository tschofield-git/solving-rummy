import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Card implements Comparable<Card> {

    private String suit;
    private int value;
    private Card next, prev;
    private int index;
    private JLabel iconLabel;
    private boolean selected, meldable, single;
    private MouseListener ml;
    private int score;
    private Icon image;
    GamePanel game;

    public Card(String s, int v, int i, GamePanel g) {
        game = g;
        suit = s;
        value = v;
        index = i;
        selected = meldable = single = false;
        initIcon();
    }

    public String getRank() {
        switch (value) {
            case 1:
                return "Ace";
            case 11:
                return "Jack";
            case 12:
                return "Queen";
            case 13:
                return "King";
            default:
                return Integer.toString(value);
        }
    }

    public String getCardName() {
        return getRank() + " of " + suit;
    }

    public Card getNext() {
        return next;
    }

    public void setNext(Card c) {
        next = c;
    }

    public Card getPrev() {
        return prev;
    }

    public void setPrev(Card c) {
        prev = c;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Card c2) {
        if (index > c2.getIndex())
            return 1;
        if (index == c2.getIndex())
            return 0;
        return -1;
    }

    public void setMeldable(boolean m) {
        if (m && !selected) iconLabel.setBorder(new LineBorder(Color.green, 2));
        meldable = m;
    }

    public void setSingle(boolean s){
        if (s && !selected) iconLabel.setBorder(new LineBorder(Color.blue, 2));
        single = s;
    }

    public boolean isSingle(){
        return single;
    }

    public boolean isSelected() {
        return selected;
    }

    public void deselect() {
        if (meldable) iconLabel.setBorder(new LineBorder(Color.green, 2));
        else if(single) iconLabel.setBorder(new LineBorder(Color.blue, 2));
        else iconLabel.setBorder(new LineBorder(Color.black, 1));
        selected = false;
    }

    public JLabel getIcon() {
        return iconLabel;
    }

    public void initIcon() {
        String path = suit.substring(0, 1).toLowerCase();
        if (value < 10) path += "0";
        path += value;
        try {
            BufferedImage img = ImageIO.read(new File("src/images/" + path + ".png"));
            image = new ImageIcon(img);
            ImageIcon icon = new ImageIcon(img);
            iconLabel = new JLabel(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        iconLabel.setSize(70, 100);
        iconLabel.setBorder(new LineBorder(Color.black, 1));

        ml = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isSelected()) {
                    iconLabel.setBorder(new LineBorder(Color.red, 2));
                    selected = true;
                } else {
                    deselect();
                }
                game.updateBtnStates();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };

        iconLabel.addMouseListener(ml);
    }

    public void removeListener(){
        iconLabel.removeMouseListener(ml);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Icon getImage() {
        return image;
    }
}
