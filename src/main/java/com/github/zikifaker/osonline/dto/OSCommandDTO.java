package com.github.zikifaker.osonline.dto;

import java.io.Serializable;

public class OSCommandDTO implements Serializable {

    private String username;

    private String command;

    public String getUsername() {
        return username;
    }

    public String getCommand() {
        return command;
    }
}
