package net.htlgkr.krejo.toDoList.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.htlgkr.krejo.toDoList.R;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_login);


    }

    private void startNewActivity(){
        Intent intent = new Intent();
        startActivity(intent);
    }
}
