package com.example.mynewapp.NavigationMenu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PaymentHistoryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PaymentHistoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is payment history fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}