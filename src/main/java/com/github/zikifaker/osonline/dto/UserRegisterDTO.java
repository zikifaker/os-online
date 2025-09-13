package com.github.zikifaker.osonline.dto;

import java.io.Serializable;

public class UserRegisterDTO implements Serializable {

    private String username;

    private String password;

    private String email;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
