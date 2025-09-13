package com.github.zikifaker.osonline.dto;

import java.io.Serializable;

public class UserLoginDTO implements Serializable {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
