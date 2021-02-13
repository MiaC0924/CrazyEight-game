package com.Mia;

public class Card {
    //data member
    public String suit, rank;

    //constructor
    public Card(String s, String r){
        suit = s;
        rank = r;
    }

    //show the card as string
    public String showCard(){
        return rank+suit;
    }
}
