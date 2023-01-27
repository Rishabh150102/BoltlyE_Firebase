package com.example.boltlye;

public class ReadWriteUserDetails {
    public  String fullName, emailId, Mobile, password;

    public ReadWriteUserDetails(String txtFullName, String txtEmailId, String txtMobile, String txtPassword){
        this.fullName = txtFullName;
        this.emailId  = txtEmailId;
        this.Mobile = txtMobile;
        this.password = txtPassword;
    }
}
