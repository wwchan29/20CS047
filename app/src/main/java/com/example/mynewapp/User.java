package com.example.mynewapp;

import java.util.ArrayList;

public class User {
    private String name;
    private int balance;
    private ArrayList<TopUp> listOfTopUp;

    public User(){
        // A constructor with no argument is needed so that the firebase fireStore can deserialize the User object correctly
    }

    public User(String name, int balance){
        this.name = name;
        this.balance = balance;
    }

    public User(String name, int balance, ArrayList<TopUp> listOfTopUp){
        this.name = name;
        this.balance = balance;
        this.listOfTopUp = listOfTopUp;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getName(){
        return name;
    }

    public int getBalance(){
        return balance;
    }

    public void setListOfTopUp(ArrayList<TopUp> listOfTopUp){
        this.listOfTopUp = listOfTopUp;
    }

    public ArrayList<TopUp> getListOfTopUp(){
        return listOfTopUp;
    }
}
