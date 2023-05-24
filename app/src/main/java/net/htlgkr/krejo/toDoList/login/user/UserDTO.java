package net.htlgkr.krejo.toDoList.login.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"username", "password", "name"})
public class UserDTO {
    private String username;
    private String password;
    private String name;

    public UserDTO(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public UserDTO() {
    }
}
