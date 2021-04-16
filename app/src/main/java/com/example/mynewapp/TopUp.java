package com.example.mynewapp;

import java.util.Date;

public class TopUp {

    private double topUpAmount;
    private Date topUpDate;

    public TopUp(){
        // A constructor with no argument is needed so that the firebase fireStore can deserialize the TopUp object correctly
    }

    public TopUp(double topUpAmount, Date topUpDate){
        this.topUpAmount = topUpAmount;
        this.topUpDate = topUpDate;
    }

    public double getTopUpAmount(){
        return topUpAmount;
    }

    public Date getTopUpDate(){
        return topUpDate;
    }
}
