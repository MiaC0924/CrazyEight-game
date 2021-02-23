package com.Mia;

import java.io.Serializable;
import java.lang.Math;
import java.util.ArrayList;

public class CardDeck implements Serializable {
    public ArrayList<Card> cards;

    /** Constructor - for server */
    public CardDeck(){
        cards = new ArrayList<Card>();

        String[] suits = new String[]{"S", "H", "D", "C"};
        int[] ranks = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

        //shuffle the cards with an int array
        //please refer to https://stackoverflow.com/questions/15942050/deck-of-cards-java
        int[] deck = new int[52];
        for(int i=0; i<deck.length; i++){
            deck[i] = i;
        }

        for (int i = 0; i < deck.length; i++) {
            int index = (int)(Math.random() * deck.length);
            int temp = deck[i];
            deck[i] = deck[index];
            deck[index] = temp;
        }

        // assign the suit and rank to the cards arrayList
        for (int i = 0; i < 52; i++) {
            cards.add(new Card(suits[deck[i] / 13], ranks[deck[i] % 13]));
        }
    }

    /**constructor with limited number of cards - for player*/
    public CardDeck(int n){
        cards = new ArrayList<Card>();
    }

    /** Getter and Setter */
    public int getSize(){ return cards.size(); }

    /**
     * methods for players' decks
     * */
    //add one card in hand deck
    public void addCard(Card c){
        cards.add(c);
    }

    //remove one card with index
    public Card playSelectedCard(int index){
        if(index >= cards.size() || index < 0) return null;

        Card selectedCard = cards.get(index);
        cards.remove(index);
        return selectedCard;
    }

    /**
     * methods for server's deck
     * */
    //take the top card of the deck
    public Card takeCardOnTop(){
        if(cards.size() == 0 || cards == null)
            return null;

        Card temp = cards.get(cards.size()-1);
        cards.remove(cards.size()-1);
        return temp;
    }

    //make sure the top card of deck is not 8
    public void sureBegin(){
        if(cards.get(cards.size()-1).getRank() == 8){
            int index = (int)(Math.random() * (cards.size()-1));
            Card temp = cards.get(cards.size()-1);
            cards.get(cards.size()-1).setCard(cards.get(index));
            cards.get(index).setCard(temp);
        }
    }

}
