package net.htlgkr.krejo.toDoList.login.user;

import java.io.Serializable;

import lombok.Data;

@Data
public class User implements Serializable {
    private int userId;
    private String username;
    private String password;
    private String name;

    public User(int userId, String username, String password, String name) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
    }
}
