package net.htlgkr.krejo.toDoList.login.user;

import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.Data;

@Data
public class UserRessource {
    private int userId;

    @JsonSetter
    public void setUserId(String userId) {
        this.userId = Integer.parseInt(userId);
    }
}
