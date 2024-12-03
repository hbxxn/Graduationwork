package com.example.setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<String> address = new MutableLiveData<>();

    public LiveData<String> getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        address.setValue(newAddress);
    }
}
