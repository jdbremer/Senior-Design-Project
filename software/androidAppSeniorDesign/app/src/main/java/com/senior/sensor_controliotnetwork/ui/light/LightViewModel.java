package com.senior.sensor_controliotnetwork.ui.light;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LightViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LightViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is light fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}