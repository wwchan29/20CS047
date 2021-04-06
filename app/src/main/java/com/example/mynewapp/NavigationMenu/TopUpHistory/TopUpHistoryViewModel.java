package com.example.mynewapp.NavigationMenu.TopUpHistory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TopUpHistoryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TopUpHistoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}