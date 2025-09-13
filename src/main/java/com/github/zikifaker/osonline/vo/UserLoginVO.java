package com.github.zikifaker.osonline.vo;

import java.io.Serializable;

public class UserLoginVO implements Serializable {

    private Integer id;

    private String userName;

    private String token;

    private UserLoginVO(Builder builder) {
        this.id = builder.id;
        this.userName = builder.userName;
        this.token = builder.token;
    }

    public static class Builder {

        private Integer id;

        private String userName;

        private String token;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public UserLoginVO build() {
            return new UserLoginVO(this);
        }
    }

    public Integer getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }
}

