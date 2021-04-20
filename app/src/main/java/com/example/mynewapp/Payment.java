package com.example.mynewapp;

import java.util.Date;

public class Payment {

    private double paymentAmount;
    private Date paymentDate;
    private String paymentAction;
    private String paymentUserName;

    public Payment(){
        // A constructor with no argument is needed so that the firebase fireStore can deserialize the TopUp object correctly
    }

    public Payment(double paymentAmount, Date paymentDate, String paymentAction, String paymentUserName){
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentAction = paymentAction;
        this.paymentUserName = paymentUserName;
    }

    public double getPaymentAmount(){
        return paymentAmount;
    }

    public Date getPaymentDate(){
        return paymentDate;
    }

    public String getPaymentAction(){
        return paymentAction;
    }

    public String getPaymentUserName(){
        return paymentUserName;
    }
}
