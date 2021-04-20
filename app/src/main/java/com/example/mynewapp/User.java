package com.example.mynewapp;

import java.util.ArrayList;

public class User {
    private String name;
    private double balance;
    private ArrayList<TopUp> listOfTopUp;
    private ArrayList<Payment> listOfPayment;

    public User(){
        // A constructor with no argument is needed so that the firebase fireStore can deserialize the User object correctly
    }

    public User(String name, double balance, ArrayList<TopUp> listOfTopUp, ArrayList<Payment> listOfPayment){
        this.name = name;
        this.balance = balance;
        this.listOfTopUp = listOfTopUp;
        this.listOfPayment = listOfPayment;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getName(){
        return name;
    }

    public double getBalance(){
        return balance;
    }

    public void setListOfTopUp(ArrayList<TopUp> listOfTopUp){
        this.listOfTopUp = listOfTopUp;
    }

    public ArrayList<TopUp> getListOfTopUp(){
        return listOfTopUp;
    }

    public void setListOfPayment(ArrayList<Payment> listOfPayment){
        this.listOfPayment = listOfPayment;
    }

    public ArrayList<Payment> getListOfPayment(){
        return listOfPayment;
    }
}
