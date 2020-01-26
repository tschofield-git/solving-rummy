import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

class GamePanel extends JPanel {

    private Deck deck;
    private Stack<Card> pile;
    private Player[] players;
    private JLabel pileLabel, deckCount;
    private JPanel overlay;
    private JPanel[] tableDisplays, handDisplays;
    private int activePlayer;
    private JTextArea gameLog, discardArea;
    private boolean draw, cont, firstRound;
    private JScrollPane scrollPane;
    private JButton[] drawBtns, playBtns;
    Thread t;

    GamePanel(int[] selected, int numPlayers) {
        players = new Player[numPlayers];
        tableDisplays = new JPanel[numPlayers];
        handDisplays = new JPanel[numPlayers];
        int index = 0;
        for (int i = 0; i < selected.length; i++) {
            switch (selected[i]) {
                case 0:
                    players[index] = new RandomAI(this, i);
                    index++;
                    break;
                case 1:
                    players[index] = new NaiveAI(this, i);
                    index++;
                    break;
                case 2:
                    players[index] = new PredictiveAI(this, i);
                    index++;
                    break;
            }
        }

        int num = 1;
        for (Player p : players) {
            p.setName("Player " + num);
            num++;
        }

        setSize(1200, 800);
        setLayout(null);
        setBackground(Color.white);
        setOpaque(true);

        initGraphics();

        initGame();

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    firstRound = true;
                    while (!checkRoundEnd()) {
                        nextPlayer();
                    }
                    roundEnd();
                } while (!checkGameEnd());
            }
        });

        t.start();
    }

    private void initGame() {

        //Deck needs to be generated and all draws are generated

        Pack p1 = new Pack();
        p1.setGame(this);
        deck = new Deck(p1.getCards());

        //Generate players' hands
        for (Player p : players) {
            Hand h = new Hand(players);
            for (int i = 0; i < 7; i++) {
                Card c = drawFromDeck();
                h.add(c);
                p.knowCard(c);
            }

            p.initHand(h);

        }

        //Take one card from the deck for the start of the pile
        pile = new Stack<>();
        pile.push(drawFromDeck());

        for (Player p : players) {
            p.setPile(pile);
        }

        Icon img = pile.peek().getIcon().getIcon();
        pileLabel.setIcon(img);

        deckCount.setText("Remaining: " + deck.size());

        tableDisplays[0] = displayTable(0);
        add(tableDisplays[0]);

        handDisplays[0] = displayHand(0);
        add(handDisplays[0]);

        repaintHand(0);

        //Setup complete - start gameplay:

        activePlayer = 0;
        draw = false;
        updateBtnStates();
    }

    void updateBtnStates() {
        int sumSelected = players[0].countSelected();
        players[0].getHand().scanSingles();
        players[0].getHand().detectSets();
        if (activePlayer != 0) {
            enableDrawBtns(false);
            disablePlayBtns();
        } else if (!draw) {
            enableDrawBtns(true);
            disablePlayBtns();
        } else {
            switch (sumSelected) {
                case 0:
                    disablePlayBtns();
                    break;
                case 1:
                    if (players[0].isSingle()) {
                        playBtns[1].setEnabled(true);
                    } else {
                        playBtns[2].setEnabled(true);
                    }
                    break;
                case 2:
                    disablePlayBtns();
                    break;
                default:
                    playBtns[0].setEnabled(players[0].getHand().isSelectedSet());
            }
        }
        if (players[0].getHand().isEmpty()) {
            synchronized (t){
                t.notify();
            }
            setCont(true);
        }
        updateDiscardArea();
    }

    private void enableDrawBtns(boolean e) {
        for (JButton b : drawBtns) {
            b.setEnabled(e);
        }
        if (e && pile.isEmpty()) {
            drawBtns[1].setEnabled(false);
            drawBtns[2].setEnabled(false);
        } else if (e && pile.size() == 1) {
            drawBtns[2].setEnabled(false);
        }

    }

    private void disablePlayBtns() {
        for (JButton b : playBtns) {
            b.setEnabled(false);
        }
    }

    private void initGraphics() {
        JPanel center = new JPanel(null);
        center.setSize(350, 250);
        center.setLocation(420, 10);
        center.setBorder(new LineBorder(Color.black));

        pileLabel = new JLabel();
        pileLabel.setSize(77, 100);
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
                genOverlay(new ArrayList<>(pile), pileLabel.getLocation());
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
        pileBtn.addActionListener(e -> {
            logDrawFromPile();
            players[activePlayer].drawFromPile(pile.pop());
            if (activePlayer == 0) {
                for (int i = 0; i < players.length; i++) repaintHand(i);
            }
            draw = true;
            pileBtn.setEnabled(false);
            wholePileBtn.setEnabled(false);
            deckDraw.setEnabled(false);
            updatePile();
            updateBtnStates();
        });
        center.add(pileBtn);

        wholePileBtn.setSize(180, 40);
        wholePileBtn.setLocation(10, 190);
        wholePileBtn.addActionListener(e -> {
            logDrawPile();
            players[activePlayer].drawPile(pile);
            pile.removeAllElements();
            if (activePlayer == 0) {
                for (int i = 0; i < players.length; i++) repaintHand(i);
            }
            draw = true;
            pileBtn.setEnabled(false);
            wholePileBtn.setEnabled(false);
            deckDraw.setEnabled(false);
            updatePile();
            updateBtnStates();
        });
        center.add(wholePileBtn);

        deckDraw.setSize(160, 40);
        deckDraw.setLocation(180, 140);
        deckDraw.addActionListener(e -> {
            players[activePlayer].drawFromDeck(drawFromDeck());
            logDrawFromDeck();
            if (activePlayer == 0) {
                for (int i = 0; i < players.length; i++) repaintHand(i);
            }
            draw = true;
            pileBtn.setEnabled(false);
            wholePileBtn.setEnabled(false);
            deckDraw.setEnabled(false);
            updateBtnStates();
        });
        center.add(deckDraw);

        deckCount.setSize(140, 40);
        deckCount.setLocation(200, 190);
        deckCount.setFont(deckCount.getFont().deriveFont(20f));
        deckCount.setAlignmentX(SwingConstants.CENTER);
        center.add(deckCount);

        add(center);

        JButton discardBtn = new JButton("Discard");
        discardBtn.setSize(100, 40);
        discardBtn.setLocation(430, 710);
        discardBtn.setEnabled(false);
        discardBtn.addActionListener(e -> {
            Card c = players[0].getHand().getSelected().get(0);
            players[0].discard(c);
            c.deselect();
            pile.push(c);
            updateBtnStates();
            logDiscard(c);
            setCont(false);
            synchronized (t){
                t.notify();
            }
        });
        add(discardBtn);

        JButton meldSingle = new JButton("Meld single");
        meldSingle.setSize(100, 40);
        meldSingle.setLocation(550, 710);
        meldSingle.setEnabled(false);
        meldSingle.addActionListener(e -> {
            ArrayList<Card> cards = players[0].getHand().getSelected();
            players[0].meld(cards);
            logMeld(cards);
            for (int i = 0; i < players.length; i++) repaintHand(i);
            repaintTable(0);
            updateBtnStates();
            players[0].getHand().scanSingles();
        });
        add(meldSingle);

        JButton meldSet = new JButton("Meld set");
        meldSet.setSize(100, 40);
        meldSet.setLocation(670, 710);
        meldSet.setEnabled(false);
        meldSet.addActionListener(e -> {
            ArrayList<Card> cards = players[0].getHand().getSelected();
            players[0].meld(cards);
            logMeld(cards);
            for (int i = 0; i < players.length; i++) repaintHand(i);
            repaintTable(0);
            updateBtnStates();
            players[0].getHand().scanSingles();
        });
        add(meldSet);

        for (int i = 1; i < players.length; i++) {
            JPanel playerPanel = new JPanel(null);
            playerPanel.setSize(400, 250);
            playerPanel.setBorder(new LineBorder(Color.black));
            playerPanel.setLocation(10, 10 + ((i - 1) * 270));
            if (i == 3) {
                playerPanel.setLocation(780, 10);
            }

            JLabel playerName = new JLabel("Player " + (i + 1) + " - " + players[i].getAI());
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
            tableDisplays[i].setLocation(playerPanel.getX() + playerTable.getX() + playerTable.getWidth() + 10, playerPanel.getY() + 143);
            tableDisplays[i].setSize(playerPanel.getWidth() - playerTable.getWidth() - 40, 100);
            tableDisplays[i].setBorder(new LineBorder(Color.red));

            add(tableDisplays[i]);

            handDisplays[i] = new JPanel(null);
            handDisplays[i].setLocation(playerPanel.getX() + playerHand.getX() + playerHand.getWidth() + 12, playerPanel.getY() + 40);
            handDisplays[i].setSize(playerPanel.getWidth() - playerHand.getWidth() - 42, 100);
            handDisplays[i].setBorder(new LineBorder(Color.blue));

            add(handDisplays[i]);

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

        discardArea = new JTextArea();
        discardArea.setSize(230, 200);
        discardArea.setLocation(10, 550);
        discardArea.setLineWrap(true);
        discardArea.setWrapStyleWord(true);
        discardArea.setEditable(false);

        add(discardArea);

        drawBtns = new JButton[3];
        drawBtns[0] = deckDraw;
        drawBtns[1] = pileBtn;
        drawBtns[2] = wholePileBtn;

        playBtns = new JButton[3];
        playBtns[0] = meldSet;
        playBtns[1] = meldSingle;
        playBtns[2] = discardBtn;
    }

    private void genOverlay(ArrayList<Card> toDisplay, Point loc) {
        overlay = new JPanel(new FlowLayout());
        overlay.setBorder(new LineBorder(Color.black));
        int w = 0;
        int x = 0;
        int h = 0;
        for (int i = 0; i < toDisplay.size(); i++) {
            Card c = toDisplay.get(i);
            JLabel j = new JLabel(c.getImage());
            j.setSize(j.getPreferredSize());
            if (i % 7 == 0) {
                h += 106;
                x = 0;
            }
            x += j.getWidth();
            w = Math.max(w, x);
            j.setLocation(x - j.getWidth(), h - 106);
            j.setBorder(new LineBorder(Color.black, 2));
            overlay.add(j);
        }
        overlay.setSize(w, h);
        loc.setLocation(Math.min(loc.getX(), getWidth()-overlay.getWidth()), Math.min(loc.getY(), getHeight()-overlay.getHeight()));
        overlay.setLocation(loc);
        add(overlay, 0);
        repaint();
    }

    private void remOverlay() {
        remove(overlay);
        repaint();
    }

    private void log(String s) {
        gameLog.append(s + "\n");
        validate();
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void logTurn() {
        log("\n---Player " + (activePlayer + 1) + "'s turn---");
    }

    private void logDrawFromDeck() {
        deckCount.setText("Remaining: " + deck.size());
        log("Card drawn from deck");
    }

    private void logDrawFromPile() {
        log(pile.peek().getCardName() + " drawn from pile");
        for (Player p : players) p.updatePlayerHand(activePlayer, pile.peek(), true);
    }

    private void logDrawPile() {
        log("Whole pile picked up");
        for (Player p : players) for (Card c : pile) p.updatePlayerHand(activePlayer, c, true);
    }

    void logMeld(List<Card> cards) {
        StringBuilder str = new StringBuilder("[");
        for (Card c : cards) str.append(c.getCardName()).append(", ");
        str = new StringBuilder(str.substring(0, str.length() - 2));
        str.append("]");
        log("Set melded: " + str);
        for (Player p : players) {
            for (Card c : cards) {
                p.knowCard(c);
                p.updatePlayerHand(activePlayer, c, false);
            }
        }
    }

    private void logDiscard(Card c) {
        log(c.getCardName() + " discarded");
        for (Player p : players) {
            p.knowCard(c);
            p.updatePlayerHand(activePlayer, c, false);
        }
        updatePile();
        for (int i = 0; i < players.length; i++) repaintHand(i);
    }

    boolean checkRoundEnd() {
        int cardSum = 0;
        for (Player p : players) cardSum += p.getHand().size();
        return cardSum + deck.size() + pile.size() == 0 || (!cont && players[activePlayer].getHand().isEmpty());
    }

    private void nextPlayer() {
        if(firstRound) {
            firstRound = false;
        }else{
            for (Player p : players) {
                p.setPile(pile);
                p.updateHandSizes(activePlayer, players[activePlayer].getHand().size());
            }

            repaintTable(activePlayer);

            activePlayer++;

            if (activePlayer == players.length) {
                activePlayer = 0;
            }
        }
        if (activePlayer != 0) {
            logTurn();
            ((AI) (players[activePlayer])).simTurn();
        } else {
            double[] drawVals = ((NaiveAI)players[0]).calcValues();
            DecimalFormat df = new DecimalFormat("#.00");
            drawBtns[0].setText("<html>Deck | Value = <b>" + df.format(drawVals[0]));
            drawBtns[1].setText("<html>Pile | Value = <b>" + df.format(drawVals[1]));
            drawBtns[2].setText("<html>Whole pile | Value = <b>" + df.format(drawVals[2]));
            draw = false;
            logTurn();
            updateBtnStates();
            players[0].getHand().detectSets();
            players[0].getHand().scanSingles();
            for (int i = 0; i < players.length; i++) repaintHand(i);
            synchronized (t){
                try {
                    t.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    boolean checkGameEnd() {

        // if any players with score >500 end game otherwise start new round
        Player[] scores = players.clone();
        Arrays.sort(scores);
        StringBuilder sb = new StringBuilder();
        sb.append("Round ended, scores: | ");
        for (Player p : scores) {
            sb.append(p.getName()).append(": ").append(p.getScore()).append(" | ");
        }
        JOptionPane.showMessageDialog(this, sb.toString());

        if (scores[0].getScore() > 500) {
            System.out.println(scores[0].getName() + " wins!");
            return true;
        } else {
            System.out.println(scores[0].getName() + " is currently winning. New round starting...");
            initGame();
            return false;
        }

    }

    private void roundEnd() {
        for (Player p : players) {
            p.calcScore();
            p.reset();
        }

        for (int i = 0; i < players.length; i++) {
            repaintHand(i);
            repaintTable(i);
        }
    }

    private void updatePile() {
        if (pile.isEmpty()) {
            pileLabel.setIcon(null);
            pileLabel.setBackground(Color.white);
            pileLabel.setSize(77, 100);
            pileLabel.setOpaque(true);
        } else {
            pileLabel.setIcon(pile.peek().getIcon().getIcon());
        }
    }

    private void repaintHand(int index) {
        remove(handDisplays[index]);
        handDisplays[index] = displayHand(index);
        add(handDisplays[index], 0);
        repaint();
    }

    private void repaintTable(int index) {
        remove(tableDisplays[index]);
        tableDisplays[index] = displayTable(index);
        add(tableDisplays[index], 0);
        repaint();

    }

    private JPanel displayHand(int n) {
        JPanel display = new JPanel(null);
        if (n == 0) {
            Hand h = players[n].getHand();

            h.sortHand();

            JLabel[] boxes = new JLabel[h.size()];
            int index = 0;
            for (Card c : h) {
                boxes[index] = c.getIcon();
                index++;
            }
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
        } else {

            display.setLocation(handDisplays[n].getLocation());
            display.setSize(handDisplays[n].getSize());
            display.setBorder(new LineBorder(Color.blue));

            ArrayList<Card> table = players[0].playerHands[n];
            int x = table.size() * 30 - 30;
            Collections.sort(table);
            for (int i = table.size() - 1; i >= 0; i--) {
                Card c1 = table.get(i);
                JLabel box = c1.getIcon();
                c1.deselect();
                box.setLocation(x, 0);
                display.add(box);
                x -= 30;
            }
            int finalN = n;
            display.addMouseListener(new MouseListener() {
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
                    genOverlay(new ArrayList<>(players[0].playerHands[finalN]), handDisplays[finalN].getLocation());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    remOverlay();
                }
            });
        }

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

            int n = 0;
            ArrayList<Card> table = players[0].getTable();
            while (n < table.size()) {
                int initN = n;

                Card c = table.get(n);

                while (n < table.size() - 1 && table.get(n + 1) == c.getNext()) {
                    c = c.getNext();
                    n += 1;
                    x += 30;
                }

                if (x > 415) {
                    x = 15 + 30 * (n - initN);
                    y += 115;
                }

                for (int i = n; i >= initN; i--) {
                    Card c1 = table.get(i);
                    JLabel box = c1.getIcon();
                    c1.deselect();
                    box.setLocation(x, y);
                    display.add(box);
                    x -= 30;
                }

                x += 30 * (n - initN) + 120;

                n++;
            }
        } else {

            display.setLocation(tableDisplays[index].getLocation());
            display.setSize(tableDisplays[index].getSize());
            display.setBorder(new LineBorder(Color.red));

            int x = 0;
            int n = 0;
            ArrayList<Card> table = players[index].getTable();
            while (n < table.size()) {
                int initN = n;

                Card c = table.get(n);

                while (n < table.size() - 1 && table.get(n + 1) == c.getNext()) {
                    c = c.getNext();
                    n += 1;
                    x += 30;
                }

                for (int i = n; i >= initN; i--) {
                    Card c1 = table.get(i);
                    JLabel box = c1.getIcon();
                    c1.deselect();
                    box.setLocation(x, 0);
                    display.add(box);
                    x -= 30;
                }

                x += 30 * (n - initN) + 120;

                n++;
            }
            display.addMouseListener(new MouseListener() {
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
                    genOverlay(new ArrayList<>(players[index].getTable()), tableDisplays[index].getLocation());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    remOverlay();
                }
            });
        }


        return display;
    }

    void reqDraw(Player p, int option) {

        if (pile.isEmpty() && deck.isEmpty()) {
            return;
        }

        if (pile.isEmpty()) option = 0;
        else if (option == 2 && pile.size() == 1) option = 1;

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

    boolean reqDiscard(Player p, Card c) {
        if (p.getHand().isEmpty()) {
            return false;
        }
        if (!p.discard(c)) return false;
        pile.push(c);
        for (Player p1 : players) {
            p1.knowCard(c);
            p1.updatePlayerHand(activePlayer, c, false);
        }
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

    boolean isDeckFlipped() {
        return deck.orderKnown;
    }

    Card topDeck() {
        if (deck.orderKnown) return deck.get(0);
        else return null;
    }

    void setCont(boolean c) {
        cont = c;
    }

    void updateDiscardArea(){
        double[] discardVals = ((NaiveAI) players[0]).calcDiscardVals();
        double[][] toSort = new double[discardVals.length][2];
        StringBuilder sb = new StringBuilder("Discard Values: ");
        for(int i = 0; i < discardVals.length; i++) {
            toSort[i][0] = discardVals[i];
            toSort[i][1] = i;
        }
        Arrays.sort(toSort, Comparator.comparingDouble(a -> a[0]));
        for(int i = 0; i < toSort.length; i++){
            String cardName = players[0].getHand().get((int)toSort[i][1]).getCardName();
            sb.append("\n" + toSort[i][0] + " | " + cardName);
        }
        discardArea.setText(sb.toString());
    }

}
