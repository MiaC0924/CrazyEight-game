package com.Mia;

import java.io.Serializable;

public class Card implements Serializable {
    //data member
    private String suit;
    private int rank;

    //constructor
    public Card(String s, int r){
        setCard(s, r);
    }

    //getter & setter
    public int    getRank(){return rank;}
    public String getSuit(){return suit;}

    public void setCard(Card c){
        suit = c.getSuit();
        rank = c.getRank();
    }

    public void setCard(String s, int r){
        suit = s;
        rank = r;
    }

    //show the card as string
    public String toString(){
        if(rank == 11){
            return "J" + suit;
        }else if(rank == 12){
            return "Q" + suit;
        }else if(rank == 13){
            return "K" + suit;
        }
        return rank + suit;
    }

    //get the card score
    public int getScore(){
        if(rank > 9){
            return 10;
        }else if(rank == 8){
            return 50;
        }
        return rank;
    }

    public boolean match(Card face){
        if (suit.equals(face.getSuit()) || rank == face.getRank() || rank == 8)
            return true;
        return false;
    }
}
