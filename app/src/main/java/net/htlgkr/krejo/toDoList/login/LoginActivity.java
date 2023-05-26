package net.htlgkr.krejo.toDoList.login;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.htlgkr.krejo.toDoList.ConstantsMyToDo;
import net.htlgkr.krejo.toDoList.HTTPSHelper;
import net.htlgkr.krejo.toDoList.R;
import net.htlgkr.krejo.toDoList.login.exceptions.InvalidUserException;
import net.htlgkr.krejo.toDoList.login.user.User;
import net.htlgkr.krejo.toDoList.login.user.UserDTO;
import net.htlgkr.krejo.toDoList.login.user.UserRessource;
import net.htlgkr.krejo.toDoList.management.ManagerActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    // TODO: 24.05.2023 gonz spät amoi wos mocha dass ma offline a se irgendwie eiloggn ko
    private User user;
    private UserRessource userRessource;
    private UserDTO userDTO;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonRegister;
    private Button buttonLogin;
    private ObjectMapper objectMapper = new ObjectMapper();
    boolean networkAvailable;
    private final HTTPSHelper httpsHelper = new HTTPSHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_login);

        if (!(networkAvailable = isNetworkAvailable())) {
            return;
        }
        initialize();

        setUpViews();
        buttonRegister.setOnClickListener(v -> sendRegisterPOSTRequestAsync());
        //buttonLogin.setOnClickListener(v -> sendLoginRequestAsync());

    }

    private void initialize() {
        user = new User();
        userRessource = new UserRessource();
        userDTO = new UserDTO();
    }

    private void sendRegisterPOSTRequestAsync() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, UserRessource> asyncTask
                = new AsyncTask<Void, Void, UserRessource>() {
            @Override
            protected UserRessource doInBackground(Void... voids) {
                try {
                    userDTO = createDTOFromEditTexts();
                    userRessource = (UserRessource) httpsHelper.sendRequest("register.php",
                            ConstantsMyToDo.POST,
                            Optional.of(userDTO),
                            userRessource);
                    return userRessource;
                } catch (IOException | InvalidUserException e) {
                    userRessource = new UserRessource();
                }
                return userRessource;
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(UserRessource userRessource) {
                if (userRessource.getUserId() > 1) {
                    Log.i(TAG,
                            "onPostExecute: new userRessource with id "
                                    + userRessource.getUserId() + " was found");
                    startNewActivity();
                } else {
                    showToasts();
                    Log.e(TAG, "onPostExecute: userRessource is null");
                }
            }
        };

        asyncTask.execute();
    }
        //BRAUCH I SPÄTER NU
//    private void sendLoginRequestAsync() {
//        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, UserRessource> asyncTask
//                = new AsyncTask<Void, Void, UserRessource>() {
//            @Override
//            protected UserRessource doInBackground(Void... voids) {
//                try {
//                    userDTO = createDTOFromEditTexts();
//                } catch (InvalidUserException e) {
//                    throw new RuntimeException(e);
//                }
//                userRessource = (UserRessource) httpsHelper.sendRequest()
//            }
//
//            @SuppressLint("StaticFieldLeak")
//            @Override
//            protected void onPostExecute(UserRessource userRessource) {
//            }
//
//        };
//        asyncTask.execute();
//    }

    private void showToasts() {
        Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Remember no spaces are allowed!", Toast.LENGTH_SHORT).show();
    }


    private UserDTO createDTOFromEditTexts() throws InvalidUserException {
        String usernameContent = editTextUsername.getText().toString();
        String passwordContent = editTextPassword.getText().toString();

        if (usernameContent.length() >= 8
                && passwordContent.length() >= 8
                && !usernameContent.contains(" ")
                && !passwordContent.contains(" ")) {

            String name = usernameContent.charAt(0) + " " + usernameContent.substring(1);
            UserDTO dto = new UserDTO();
            dto.setUsername(usernameContent);
            dto.setPassword(passwordContent);
            dto.setName(name);
            return dto;

        } else {
            throw new InvalidUserException();
        }
    }

    private void setUpViews() {
        editTextUsername = findViewById(R.id.editText_login_name);
        editTextPassword = findViewById(R.id.editText_login_password);
        buttonRegister = findViewById(R.id.button_login_register);
        buttonLogin = findViewById(R.id.button_login_login);
        buttonLogin.setClickable(false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkActive = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.i(TAG, "isNetworkAvailable() returned: " + isNetworkActive);

        return isNetworkActive;
    }


    private void startNewActivity() {
        Intent intent = new Intent(this, ManagerActivity.class);
        intent.putExtra("user", createUser());
        intent.putExtra("networkAvailable", networkAvailable);
        // TODO: 24.05.2023 get do de listen vom server und gibs mid oda so
        startActivity(intent);
    }

    private User createUser() {
        user = new User(userRessource.getUserId(),
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getName());
        return user;
    }
}
