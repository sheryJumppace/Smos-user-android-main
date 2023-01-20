package com.smox.smoxuser.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WebViewModel extends ViewModel {
    private final MutableLiveData<String> url = new MutableLiveData<String>();

    public void setUrl(String val) {
        url.setValue(val);
    }

    public LiveData<String> getUrl() {
        return url;
    }
}
