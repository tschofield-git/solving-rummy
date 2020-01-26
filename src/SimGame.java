import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

class SimGame {

    private Card[] cards;
    private Deck deck;
    private Stack<Card> pile;
    private Player[] players;
    private int activePlayer;
    private int winner;
    private boolean v, cont;

    SimGame(int[] selected, int numPlayers, boolean verbose) {
        super();
        v = verbose;
        players = new Player[numPlayers];

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
            //p.initPlayers(players);
            p.setName("Player " + num++);
        }

        cards = new Pack().getCards();

        initGame();

        do {
            while (!checkRoundEnd()) {
                nextPlayer();
            }
            roundEnd();
        } while (!checkGameEnd());
    }

    int simulate() {
        ((AI) (players[activePlayer])).simTurn();
        return winner;
    }

    private void initGame() {
        //Deck needs to be generated and all draws are generated
        deck = new

                Deck(cards);

        //Generate players' hands
        for (
                Player p : players)

        {
            Hand h = new Hand(players);
            for (int i = 0; i < 7; i++) {
                h.add(drawFromDeck());
            }

            p.initHand(h);

        }

        //Take one card from the deck for the start of the pile
        pile = new Stack<>();
        pile.push(drawFromDeck());

        for (Player p : players) p.setPile(pile);

        activePlayer = 0;

    }

    private void roundEnd() {
        for (Player p : players) {
            p.calcScore();
            p.reset();
        }

    }

    private void nextPlayer() {
        for (Player p : players) {
            //p.updatePlayer(players[activePlayer], activePlayer);
            p.setPile(pile);
            p.updateHandSizes(activePlayer, players[activePlayer].getHand().size());
        }

        activePlayer++;
        if (activePlayer == players.length) {
            activePlayer = 0;
        }
        ((AI) (players[activePlayer])).simTurn();
    }

    boolean checkRoundEnd() {
        int cardSum = 0;
        for (Player p : players) cardSum += p.getHand().size();
        return (cardSum + deck.size() + pile.size() == 0 || (!cont && players[activePlayer].getHand().isEmpty()));
    }

    boolean checkGameEnd() {
        // if any players with score >500 end game otherwise start new round

        Player[] scores = players.clone();
        Arrays.sort(scores);
        if (v) {
            System.out.print("Round ended, scores: | ");

            for (Player p : scores) {
                System.out.print(p.getName() + ": " + p.getScore() + " | ");
            }
            System.out.println();
        }
        if (scores[0].getScore() > 500) {
            if (v) System.out.println(scores[0].getName() + " wins!");
            winner = Integer.parseInt(scores[0].getName().substring(scores[0].getName().length() - 1));
            StringBuilder toWrite = new StringBuilder();
            //Format: factor 1 factor 2 factor 3 score
            for (Player p : players) {
                //toWrite.append(p.getAI() + "," + p.getScore() + "\n");

                if (p.getAI().equals("Naive AI")) {
                    for (double d : ((NaiveAI) p).getFactors())
                        toWrite.append(d).append(",");

                } else {
                    toWrite.append(",,,");
                }

                toWrite.append(p.getScore() + "\n");

            }
            try {
                Files.write(Paths.get("data/resultsFinal.csv"), toWrite.toString().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
            return true;
        } else {
            if (v) System.out.println(scores[0].getName() + " is currently winning. New round starting...");
            initGame();
            return false;
        }
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
                break;
            case 1:
                for (Player p1 : players) p1.updatePlayerHand(activePlayer, pile.peek(), true);
                p.drawFromPile(pile.pop());
                break;
            case 2:
                for (Player p1 : players) for (Card c1 : pile) p1.updatePlayerHand(activePlayer, c1, true);
                p.drawPile(pile);
                pile.removeAllElements();
                break;
        }

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
        return true;
    }

    private Card drawFromDeck() {
        if (deck.size() == 0 && pile.size() > 0) {
            deck.addAll(pile);
            pile.removeAllElements();
            deck.setOrderKnown();
        } else if (deck.size() == 1) {
            Card c = deck.draw();
            deck.addAll(pile);
            pile.removeAllElements();
            deck.setOrderKnown();
            if (deck.size() > 1) pile.push(deck.draw());
            return c;
        }
        return deck.draw();
    }

    void meld(List<Card> cards) {
        for (Player p : players)
            for (Card c : cards) {
                p.knowCard(c);
                p.updatePlayerHand(activePlayer, c, false);
            }
    }

    boolean isDeckFlipped() {
        return deck.orderKnown;
    }

    Card topDeck() {
        if (deck.orderKnown && !deck.isEmpty()) return deck.get(0);
        else return null;
    }

    void setCont(boolean c) {
        cont = c;
    }

}
