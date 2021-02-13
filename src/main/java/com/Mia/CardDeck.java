package com.Mia;

import java.lang.Math;

public class CardDeck {
    //data member
    public Card[] cards;
    public int    numOfCards;

    /*
    * Constructor - for server
    * - about shuffling cards and the assignment
    * - please refer to https://stackoverflow.com/questions/15942050/deck-of-cards-java
    */
    public CardDeck(){
        cards = new Card[52];
        numOfCards = 52;

        String[] suits = new String[]{"S", "H", "D", "C"};
        String[] ranks = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        //shuffle the cards with an int array
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

        // assign the suit and rank to the cards array
        for (int i = 0; i < 52; i++) {
            cards[i].suit = suits[deck[i] / 13];
            cards[i].rank = ranks[deck[i] % 13];
        }
    }

    //constructor with limited number of cards - for player
    public CardDeck(int n){
        cards = new Card[n];
        numOfCards = n;
    }

    /**
     * methods for players' decks
     * */
    //add one card in hand deck
    public void addCard(Card c){
        Card[] temp = new Card[cards.length+1];

        for (int i = 0; i < cards.length; i++) {
            temp[i] = cards[i];
        }
        temp[cards.length] = c;

        cards = temp;
        numOfCards = cards.length;
    }

    //remove one card with index
    public Card playSelectedCard(int index){
        if(index >= cards.length || index < 0) return null;

        Card selectedCard = cards[index];

        int k = 0;
        Card[] temp = new Card[cards.length-1];
        for (int i = 0; i < cards.length; i++) {
            if(i == index) continue;
            temp[k++] = cards[i];
        }

        cards = temp;
        numOfCards--;
        return selectedCard;
    }

    /**
     * methods for server's deck
     * */
    //take the top card of the deck
    public Card takeCardOnTop(){
        // check if no card in deck
        if(!isEmpty())  return null;

        numOfCards--;
        return cards[numOfCards-1];
    }

    //check if the deck is empty
    public boolean isEmpty(){
        if(numOfCards == 0 || cards == null) return true;
        return false;
    }

    //make sure the top card of deck is not 8
    public void sureBegin(){
        if(cards[cards.length-1].rank.equals("8")){
            int index = (int)(Math.random() * (cards.length-1));
            Card temp = cards[cards.length-1];
            cards[cards.length-1] = cards[index];
            cards[index] = temp;
        }
    }

}
