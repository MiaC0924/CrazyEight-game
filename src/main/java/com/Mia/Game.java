package com.Mia;

import java.io.Serializable;

/**
 * HERE IS A GAME LOGIC CLASS
 */

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private CardDeck tableCards;

    //ctor
    public Game(){
        tableCards = new CardDeck();
        tableCards.sureBegin();
    }

    //setter - for test case
    public void setCardDeck(CardDeck cd){tableCards = cd;}

    //从牌堆发牌
    public int getNumOfCard() {return tableCards.getSize();}

    public Card takeCard(){
        return tableCards.takeCardOnTop();
    }

    public int whosNext(int current, int direction, int numPlayer, Card faceCard){
        int temp;
        if (faceCard.getRank() == 12) {
            temp = (current + direction*2) % numPlayer;
        } else {
            temp = (current + direction) % numPlayer;
        }

        if(temp < 0) {
            return numPlayer + temp;
        }else {
            return temp;
        }
    }

    public boolean ifRoundEnd(Player[] pl){
        if(tableCards.getSize() > 0) return false;

        int sum = 0;
        for (int i = 0; i < pl.length; i++) {
            if(pl[i].canPlay == false) ++sum;
        }
        if(tableCards.getSize() == 0 && sum == 4) return true;
        return false;
    }

    public boolean ifGameEnd(Player[] pl){
        for (int i = 1; i < pl.length; i++) {
            if(pl[i].getPlayerScore() >= 100)
                return true;
        }
        return false;
    }

    //get Winner with lowest score - 找到最终赢家
    public Player getWinner(Player[] pl) {
        Player temp = pl[0];

        for (int i = 1; i < pl.length; i++) {
            if(pl[i].getPlayerScore() < temp.getPlayerScore())
                temp = pl[i];
        }
        return temp;
    }

    //prints the final score sheet - 打印最终成绩单
    public void printFinalResult(Player[] pl) {
        for (int i = 0; i < pl.length; i++) {
            System.out.println("The final score of " + pl[i].getName() + " is " + pl[i].getPlayerScore() + " .\n");
        }
        System.out.println("==== PLAYER " + getWinner(pl).getName() + " WIN THE GAME ====\n");
    }

}
