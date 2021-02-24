package com.Mia;

public class TesterDeck extends CardDeck {
    CardDeck testerDeck;

    public TesterDeck(){
        testerDeck = new CardDeck(0);
    }

    public void test1(){
        testerDeck.addCard(new Card("H",4));
        testerDeck.addCard(new Card("S",4));
        testerDeck.addCard(new Card("S",9));
        testerDeck.addCard(new Card("D",7));

        testerDeck.addCard(new Card("S",7));
        testerDeck.addCard(new Card("S",6));
        testerDeck.addCard(new Card("C",6));
        testerDeck.addCard(new Card("H",11));

        testerDeck.addCard(new Card("D",5));
        testerDeck.addCard(new Card("C",13));
        testerDeck.addCard(new Card("C",9));
        testerDeck.addCard(new Card("H",12));

        testerDeck.addCard(new Card("D",6));
        testerDeck.addCard(new Card("H",8));
        testerDeck.addCard(new Card("D",11));
        testerDeck.addCard(new Card("H",13));

        testerDeck.addCard(new Card("D",9));
        testerDeck.addCard(new Card("D",10));
        testerDeck.addCard(new Card("H",3));
        testerDeck.addCard(new Card("C",5));

        testerDeck.addCard(new Card("C",2));
        testerDeck.addCard(new Card("C",3));
        testerDeck.addCard(new Card("C",4));

        testerDeck.addCard(new Card("C",10));
        testerDeck.addCard(new Card("C",11));

        testerDeck.addCard(new Card("C",7));
        testerDeck.addCard(new Card("H",8));

        testerDeck.addCard(new Card("C",8));
        testerDeck.addCard(new Card("H",9));
    }

    public void test2(){
        testerDeck.addCard(new Card("D",7));
        testerDeck.addCard(new Card("D",9));
        testerDeck.addCard(new Card("D",3));
        testerDeck.addCard(new Card("D",4));

        testerDeck.addCard(new Card("S",4));
        testerDeck.addCard(new Card("S",3));
        testerDeck.addCard(new Card("S",9));
        testerDeck.addCard(new Card("S",7));

        testerDeck.addCard(new Card("C",7));
        testerDeck.addCard(new Card("C",9));
        testerDeck.addCard(new Card("C",3));
        testerDeck.addCard(new Card("C",4));

        testerDeck.addCard(new Card("H",4));
        testerDeck.addCard(new Card("H",3));
        testerDeck.addCard(new Card("H",9));
        testerDeck.addCard(new Card("S",5));

        testerDeck.addCard(new Card("D",5));
        testerDeck.addCard(new Card("C",11));
        testerDeck.addCard(new Card("H",5));
        testerDeck.addCard(new Card("D",8));

        testerDeck.addCard(new Card("S",13));
        testerDeck.addCard(new Card("S",12));
        testerDeck.addCard(new Card("H",13));

        testerDeck.addCard(new Card("D",6));
        testerDeck.addCard(new Card("D",12));
        testerDeck.addCard(new Card("D",11));

        testerDeck.addCard(new Card("S",6));
        testerDeck.addCard(new Card("S",11));
        testerDeck.addCard(new Card("S",10));

        testerDeck.addCard(new Card("C",8));
        testerDeck.addCard(new Card("H",8));
    }
}
