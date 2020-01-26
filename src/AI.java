public abstract class AI extends Player{

    //No human input
    GamePanel game;

    public AI(GamePanel g){
        super();
        game = g;
    }

    public void simTurn(){
        //Thread.sleep(2000);
        calcDraw();
        //Thread.sleep(2000);
        calcMeld();
        //Thread.sleep(2000);
        if (calcDiscard()) game.checkRoundEnd();
    }

    public void simDraw(int option){
        game.reqDraw(this, option);
    }

    public boolean simDiscard(Card c){
        if(getHand().isEmpty()) return true;
        return game.reqDiscard(this, c);
    }

    abstract void calcDraw();

    abstract void calcMeld();

    abstract boolean calcDiscard();

}
