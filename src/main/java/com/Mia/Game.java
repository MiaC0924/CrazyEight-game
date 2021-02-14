package com.Mia;

import java.io.Serializable;

/**
 * HERE IS A GAME LOGIC CLASS
 */

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private CardDeck tableCards;
    private boolean  end;

    public Game(){
        tableCards = new CardDeck();
        tableCards.sureBegin();
    }

    //从牌堆发牌
    public Card dealCard(){
        return tableCards.takeCardOnTop();
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
