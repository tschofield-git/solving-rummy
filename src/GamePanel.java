import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GamePanel extends JPanel {

    Card[] cards;
    Deck deck;
    Stack<Card> pile;
    Player[] players;
    JLabel pileLabel, deckCount;
    JPanel handDisplay, pileOverlay;
    JPanel[] tableDisplays;
    int activePlayer;
    JTextArea gameLog;
    boolean draw;
    boolean discard;
    JScrollPane scrollPane;
    JButton[] drawBtns, playBtns;

    public GamePanel(int[] selected, int numPlayers) {
        players = new Player[numPlayers];
        tableDisplays = new JPanel[numPlayers];
        int index = 0;
        for (int i = 0; i < selected.length; i++) {
            switch (selected[i]) {
                case 0:
                    players[index] = new AI1(this);
                    index++;
                    break;
                case 1:
                    players[index] = new AI2(this);
                    index++;
                    break;
                case 2:
                    players[index] = new AI3(this);
                    index++;
                    break;
            }
        }

        players[0] = new Player();

        int num = 1;
        for (Player p : players) {
            p.initPlayers(players);
            p.setName("Player " + num);
            num++;
        }

        setSize(1200, 800);
        setLayout(null);
        setBackground(Color.white);
        setOpaque(true);

        initCards();

        initGraphics();

        initGame();
    }

    private void initGame() {

        //Deck needs to be generated and all draws are generated
        deck = new Deck(cards);

        //Generate players' hands
        for (Player p : players) {
            Hand h = new Hand();
            for (int i = 0; i < 7; i++) {
                h.add(drawFromDeck());
            }

            p.initHand(h);

        }

        //Take one card from the deck for the start of the pile
        pile = new Stack<Card>();
        pile.push(drawFromDeck());

        Icon img = pile.peek().getIcon().getIcon();
        pileLabel.setIcon(img);

        deckCount.setText("Remaining: " + deck.size());

        handDisplay = displayHand();
        add(handDisplay);

        tableDisplays[0] = displayTable(0);
        add(tableDisplays[0]);

        repaintHand();

        //Setup complete - start gameplay:

        activePlayer = 0;
        draw = discard = false;
        updateBtnStates();

        logTurn();

    }

    public void updateBtnStates() {
        int sumSelected = players[0].getHand().countSelected();
        players[0].highlightSingles(players[0].scanSingles());
        players[0].getHand().detectSets();
        if (activePlayer != 0) {
            enableDrawBtns(false);
            disablePlayBtns();
        } else if (!draw) {
            enableDrawBtns(true);
            disablePlayBtns();
        } else switch (sumSelected) {
            case 0:
                disablePlayBtns();
                break;
            case 1:
                if (players[0].isSingle()) playBtns[1].setEnabled(true);
                else playBtns[2].setEnabled(true);
                break;
            case 2:
                disablePlayBtns();
                break;
            default:
                if (players[0].getHand().isSelectedSet()) playBtns[0].setEnabled(true);
        }
    }

    private void enableDrawBtns(boolean e) {
        for(JButton b : drawBtns) b.setEnabled(e);
        if(e && pile.isEmpty()) {
            drawBtns[1].setEnabled(false);
            drawBtns[2].setEnabled(false);
        }else if(e && pile.size() == 1) drawBtns[2].setEnabled(false);

    }

    private void disablePlayBtns() {
        for (JButton b : playBtns) b.setEnabled(false);
    }

    private void initGraphics() {
        JPanel center = new JPanel(null);
        center.setSize(350, 250);
        center.setLocation(420, 10);
        center.setBorder(new LineBorder(Color.black));

        pileLabel = new JLabel();
        pileLabel.setSize(70, 100);
        pileLabel.setLocation(490, 30);
        pileLabel.setBorder(new LineBorder(Color.black));
        pileLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                genOverlay();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                remOverlay();
            }
        });
        add(pileLabel);

        BufferedImage cardback = null;
        try {
            cardback = ImageIO.read(new File("src/images/Cardback.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel deckLabel = new JLabel(new ImageIcon(cardback));
        deckLabel.setSize(70, 100);
        deckLabel.setLocation(210, 20);
        deckLabel.setBorder(new LineBorder(Color.black));
        center.add(deckLabel);

        JButton pileBtn = new JButton("Take from pile");
        JButton wholePileBtn = new JButton("Take whole pile");
        JButton deckDraw = new JButton("Draw from deck");
        deckCount = new JLabel("Remaining: 52");

        pileBtn.setSize(160, 40);
        pileBtn.setLocation(10, 140);
        pileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logDrawFromPile();
                players[activePlayer].drawFromPile(pile.pop());
                if (activePlayer == 0) repaintHand();
                draw = true;
                pileBtn.setEnabled(false);
                wholePileBtn.setEnabled(false);
                deckDraw.setEnabled(false);
                updatePile();
                updateBtnStates();
            }
        });
        center.add(pileBtn);

        wholePileBtn.setSize(160, 40);
        wholePileBtn.setLocation(10, 190);
        wholePileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logDrawPile();
                players[activePlayer].drawPile(pile);
                pile.removeAllElements();
                if (activePlayer == 0) repaintHand();
                draw = true;
                pileBtn.setEnabled(false);
                wholePileBtn.setEnabled(false);
                deckDraw.setEnabled(false);
                updatePile();
                updateBtnStates();
            }
        });
        center.add(wholePileBtn);

        deckDraw.setSize(160, 40);
        deckDraw.setLocation(180, 140);
        deckDraw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                players[activePlayer].drawFromDeck(drawFromDeck());
                logDrawFromDeck();
                if (activePlayer == 0) repaintHand();
                draw = true;
                pileBtn.setEnabled(false);
                wholePileBtn.setEnabled(false);
                deckDraw.setEnabled(false);
                updateBtnStates();
            }
        });
        center.add(deckDraw);

        deckCount.setSize(160, 40);
        deckCount.setLocation(180, 190);
        deckCount.setFont(deckCount.getFont().deriveFont(24f));
        deckCount.setAlignmentX(SwingConstants.CENTER);
        center.add(deckCount);

        add(center);

        JButton discardBtn = new JButton("Discard");
        discardBtn.setSize(100, 40);
        discardBtn.setLocation(430, 710);
        discardBtn.setEnabled(false);
        discardBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Card c = players[0].getHand().getSelected().get(0);
                players[0].discard(c);
                c.deselect();
                pile.push(c);
                discard = true;
                updateBtnStates();
                logDiscard(c);
                checkRoundEnd();
            }
        });
        add(discardBtn);

        JButton meldSingle = new JButton("Meld single");
        meldSingle.setSize(100, 40);
        meldSingle.setLocation(550, 710);
        meldSingle.setEnabled(false);
        meldSingle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Card> cards = players[0].getHand().getSelected();
                players[0].meld(cards);
                if (players[0].getHand().isEmpty()) discard = true;
                logMeld(cards);
                repaintHand();
                repaintTable(0);
                updateBtnStates();
                players[0].highlightSingles(players[0].scanSingles());
            }
        });
        add(meldSingle);

        JButton meldSet = new JButton("Meld set");
        meldSet.setSize(100, 40);
        meldSet.setLocation(670, 710);
        meldSet.setEnabled(false);
        meldSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Card> cards = players[0].getHand().getSelected();
                players[0].meld(cards);
                if (players[0].getHand().isEmpty()) discard = true;
                logMeld(cards);
                repaintHand();
                repaintTable(0);
                updateBtnStates();
                players[0].highlightSingles(players[0].scanSingles());
            }
        });
        add(meldSet);

        for (int i = 1; i < players.length; i++) {
            JPanel playerPanel = new JPanel(null);
            playerPanel.setSize(400, 250);
            playerPanel.setBorder(new LineBorder(Color.black));
            playerPanel.setLocation(10, 10 + ((i - 1) * 270));
            if (i == 3) playerPanel.setLocation(780, 10);

            JLabel playerName = new JLabel("Player " + (i + 1));
            playerName.setFont(playerName.getFont().deriveFont(24f));
            playerName.setSize(playerName.getPreferredSize());
            playerName.setLocation(10, 10);
            playerPanel.add(playerName);

            JLabel playerHand = new JLabel("Hand: ");
            playerHand.setFont(playerHand.getFont().deriveFont(18f));
            playerHand.setSize(playerHand.getPreferredSize());
            playerHand.setLocation(10, 80);
            playerPanel.add(playerHand);

            JLabel playerTable = new JLabel("Table: ");
            playerTable.setFont(playerHand.getFont());
            playerTable.setSize(playerTable.getPreferredSize());
            playerTable.setLocation(10, 180);
            playerPanel.add(playerTable);

            tableDisplays[i] = new JPanel(null);
            tableDisplays[i].setLocation(playerPanel.getX() + playerTable.getX() + playerTable.getWidth() + 10, playerPanel.getY() + 140);
            tableDisplays[i].setSize(playerPanel.getWidth() - playerTable.getWidth() - 40, 100);
            tableDisplays[i].setBorder(new LineBorder(Color.red));
            add(tableDisplays[i]);

            add(playerPanel);
        }

        gameLog = new JTextArea();
        gameLog.setSize(200, 400);
        gameLog.setLineWrap(true);
        gameLog.setWrapStyleWord(true);
        gameLog.setEditable(false);

        scrollPane = new JScrollPane(gameLog);
        scrollPane.setSize(200, 450);
        scrollPane.setLocation(970, 300);

        JLabel logLabel = new JLabel("Game Log:");
        logLabel.setSize(logLabel.getPreferredSize());
        logLabel.setLocation(970, scrollPane.getY() - logLabel.getHeight());
        add(logLabel);

        add(scrollPane);

        drawBtns = new JButton[3];
        drawBtns[0] = pileBtn;
        drawBtns[1] = deckDraw;
        drawBtns[2] = wholePileBtn;

        playBtns = new JButton[3];
        playBtns[0] = meldSet;
        playBtns[1] = meldSingle;
        playBtns[2] = discardBtn;
    }

    private void genOverlay(){
        pileOverlay = new JPanel(new FlowLayout());
        pileOverlay.setBorder(new LineBorder(Color.black));
        pileOverlay.setLocation(pileLabel.getLocation());
        int w = 0;
        int x = 0;
        int h = 0;
        for(int i = 0; i < pile.size(); i++) {
            Card c = pile.get(i);
            JLabel j = new JLabel(c.getImage());
            j.setSize(j.getPreferredSize());
            x += j.getWidth();
            w = Math.max(w, x);
            if(i%7 == 0) {
                h += 106;
                x = j.getWidth();
            }
            j.setLocation(x-j.getWidth(), h-106);
            j.setBorder(new LineBorder(Color.black, 2));
            pileOverlay.add(j);
        }
        pileOverlay.setSize(w, h);
        add(pileOverlay, 0);
        repaint();
    }

    private void remOverlay(){
        remove(pileOverlay);
        repaint();
    }

    private void log(String s) {
        gameLog.append(s + "\n");
        validate();
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public void logTurn() {
        log("\n---Player " + (activePlayer + 1) + "'s turn---");
    }

    public void logDrawFromDeck() {
        deckCount.setText("Remaining: " + deck.size());
        log("Card drawn from deck");
    }

    public void logDrawFromPile() {
        log(pile.peek().getCardName() + " drawn from pile");
    }

    public void logDrawPile() {
        log("Whole pile picked up");
    }

    public void logMeld(ArrayList<Card> cards) {
        String str = "[";
        for (Card c : cards) str += c.getCardName() + ", ";
        str = str.substring(0, str.length() - 2);
        str += "]";
        log("Set melded: " + str);
    }

    public void logDiscard(Card c) {
        log(c.getCardName() + " discarded");
        updatePile();
        repaintHand();
    }

    public void checkRoundEnd() {
        if (players[activePlayer].getHand().isEmpty()) {
            roundEnd();
        } else {

            for (Player p : players)
                p.updatePlayer(players[activePlayer], activePlayer);

            repaintTable(activePlayer);

            activePlayer++;
            if (activePlayer == players.length) activePlayer = 0;
            if (activePlayer != 0) {
                logTurn();
                ((AI) (players[activePlayer])).simTurn();
            } else {
                draw = discard = false;
                logTurn();
                updateBtnStates();
                players[0].getHand().detectSets();
                players[0].highlightSingles(players[0].scanSingles());
                repaintHand();
            }
        }
    }

    private void roundEnd() {
        for (Player p : players) {
            p.calcScore();
            for (ArrayList<Card> al : p.getTable()) {
                Iterator<Card> iter = al.iterator();
                while(iter.hasNext()){
                    Card c = iter.next();
                    iter.remove();
                    deck.add(c);
                }
            }
            Iterator<ArrayList<Card>> iter = p.getTable().iterator();
            while(iter.hasNext()){
                iter.remove();
            }

            deck.addAll(p.getHand());
            p.getHand().removeAll(p.getHand());
        }
        deck.addAll(pile);
        pile.removeAllElements();

        // if any players with score >500 end game otherwise start new round

        Player[] scores = players.clone();
        Arrays.sort(scores);
        System.out.print("Round ended, scores: | ");
        for(Player p: scores){
            System.out.print(p.getName() + ": " + p.getScore() + " | ");
        }
        System.out.println();

        if(scores[0].getScore() > 500){
            System.out.println(scores[0].getName() + " wins!");
        }else{
            System.out.println(scores[0].getName() + " is currently winning. New round starting...");
            initGame();
        }

    }

    private void updatePile() {
        if (pile.isEmpty()) {
            pileLabel.setIcon(null);
            pileLabel.setBackground(Color.white);
            pileLabel.setSize(70, 100);
            pileLabel.setOpaque(true);
        } else {
            pileLabel.setIcon(pile.peek().getIcon().getIcon());
        }
    }

    private void repaintHand() {
        remove(handDisplay);
        handDisplay = displayHand();
        add(handDisplay);
        repaint();

    }

    private void repaintTable(int index) {
        remove(tableDisplays[index]);
        tableDisplays[index] = displayTable(index);
        add(tableDisplays[index], 0);
        repaint();

    }

    private void initCards() {
        cards = new Card[52];
        String[] suits = {"Spades", "Hearts", "Clubs", "Diamonds"};
        for (int i = 0; i < 4; i++) {
            cards[i * 13] = new Card(suits[i], 1, i * 13, this);
            cards[i * 13].setScore(15);
            for (int j = 1; j < 13; j++) {
                cards[i * 13 + j] = new Card(suits[i], (j + 1), i * 13 + j, this);
                if (j < 9) cards[i * 13 + j].setScore(5);
                else cards[i * 13 + j].setScore(10);
                cards[i * 13 + j].setPrev(cards[i * 13 + j - 1]);
                cards[i * 13 + j - 1].setNext(cards[i * 13 + j]);
            }
            cards[i * 13].setPrev(cards[i * 13 + 12]);//Ace's previous card is the King of the same suit
            cards[i * 13 + 12].setNext(cards[i * 13]);//King's next card is the Ace of the same suit
        }
    }

    private JPanel displayHand() {

        Hand h = players[0].getHand();

        h.sortHand();

        JLabel[] boxes = new JLabel[h.size()];
        int index = 0;
        for (Card c : h) {
            boxes[index] = c.getIcon();
            index++;
        }

        JPanel display = new JPanel(null);
        double dist = 70;
        if (boxes.length > 10) {
            dist = 700 / boxes.length;
        }
        for (int i = boxes.length - 1; i >= 0; i--) {
            boxes[i].setLocation((int) (i * dist), 0);
            display.add(boxes[i]);
        }

        display.setSize((int) ((boxes.length - 1) * dist) + 70, 100);
        display.setLocation(600 - display.getWidth() / 2, 600);

        return display;
    }

    private JPanel displayTable(int index) {
        JPanel display = new JPanel(null);
        if (index == 0) {
            display.setLocation(420, 280);
            display.setSize(500, 250);
            display.setBorder(new LineBorder(Color.black));

            int x = 15;
            int y = 20;

            //System.out.println(Arrays.toString(players[0].getTable().toArray()));
            for (ArrayList<Card> ac : players[0].getTable()) {
                //System.out.println(Arrays.toString(ac.toArray()));
                x += 30 * (ac.size() - 1);
                //Second row of cards if needed
                if (x > 415) {
                    x = 15 + 30 * (ac.size() - 1);
                    y += 115;
                }
                for (int i = ac.size() - 1; i >= 0; i--) {
                    Card c = ac.get(i);
                    JLabel box = c.getIcon();
                    c.deselect();
                    box.setLocation(x, y);
                    display.add(box);
                    x -= 30;
                }
                x += 30 * (ac.size() - 1) + 120;
            }
        } else {

            display.setLocation(tableDisplays[index].getLocation());
            display.setSize(tableDisplays[index].getSize());
            display.setBorder(new LineBorder(Color.red));

            int x = 0;
            for (ArrayList<Card> ac : players[index].getTable()) {
                x += 30 * (ac.size() - 1);
                for (int i = ac.size() - 1; i >= 0; i--) {
                    Card c = ac.get(i);
                    JLabel box = c.getIcon();
                    c.deselect();
                    box.setLocation(x, 0);
                    display.add(box);
                    x -= 30;
                }
                x += 30 * (ac.size() - 1) + 120;
            }
        }
        return display;
    }

    public void reqDraw(Player p, int option) {

        if(pile.isEmpty()) option = 0;
        else if(option == 2 && pile.size() == 1) option = 1;

        option = 0;

        switch (option) {
            case 0:
                Card c = drawFromDeck();
                p.drawFromDeck(c);
                logDrawFromDeck();
                break;
            case 1:
                logDrawFromPile();
                p.drawFromPile(pile.pop());
                break;
            case 2:
                logDrawPile();
                p.drawPile(pile);
                pile.removeAllElements();
                break;
        }

        updatePile();
    }

    public boolean reqDiscard(Player p, Card c) {
        if (!p.discard(c)) return false;
        pile.push(c);
        updatePile();
        logDiscard(c);
        return true;
    }

    private Card drawFromDeck() {
        if (deck.isEmpty()) {
            deck.addAll(pile);
            pile.removeAllElements();
            deck.setOrderKnown();
            log("Deck flipped");
            pile.push(deck.draw());
        }
        return deck.draw();
    }

}
