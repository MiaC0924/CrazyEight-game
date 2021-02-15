package com.Mia;

import java.io.Serializable;

/**
 * HERE IS A GAME LOGIC CLASS
 */

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private CardDeck tableCards;
    private int next = 1;

    public Game(){
        tableCards = new CardDeck();
        tableCards.sureBegin();
    }

    //从牌堆发牌
    public int getNumOfCard() {return tableCards.getSize();}

    public Card dealCard(){
        return tableCards.takeCardOnTop();
    }

    public int nextPlayer() { return next; }

    public void reverseDirection() { next = -1; }

    public boolean ifGameEnd(Player[] pl){
        for (int i = 1; i < pl.length; i++) {
            if(pl[i].getPlayerScore() >= 100)
                return true;
        }
        return false;
    }

    public boolean ifRoundEnd(Player[] pl){
        int sum = 0;
        for (int i = 0; i < pl.length; i++) {
            sum += pl[i].cantPlay;
        }
        if(tableCards.getSize() == 0 && sum == 4) return true;
        return false;
    }

    //get Winner with lowest score - 找到赢家
    public Player getWinner(Player[] pl) {
        Player temp = pl[0];

        for (int i = 1; i < pl.length; i++) {
            if(pl[i].getPlayerScore() < temp.getPlayerScore())
                temp = pl[i];
        }
        return temp;
    }

    //prints the score sheet - 打印成绩单
    public void printResult(Player[] pl) {
        for (int i = 0; i < pl.length; i++) {
            System.out.println("The final score of " + pl[i].getName() + " is " + pl[i].getPlayerScore() + " .\n");
        }
        System.out.println("==== PLAYER " + getWinner(pl).getName() + " WIN THE GAME ====\n");
    }

}
