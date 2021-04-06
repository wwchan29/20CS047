package com.example.mynewapp;

import java.util.Date;

public class TopUp {

    private int topUpAmount;
    private Date topUpDate;

    public TopUp(){

    }

    public TopUp(int topUpAmount, Date topUpDate){
        this.topUpAmount = topUpAmount;
        this.topUpDate = topUpDate;
    }

    public int getTopUpAmount(){
        return topUpAmount;
    }

    public Date getTopUpDate(){
        return topUpDate;
    }
}
