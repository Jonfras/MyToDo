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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.htlgkr.krejo.toDoList.R;
import net.htlgkr.krejo.toDoList.login.exceptions.InvalidUserException;
import net.htlgkr.krejo.toDoList.login.user.User;
import net.htlgkr.krejo.toDoList.login.user.UserDTO;
import net.htlgkr.krejo.toDoList.login.user.UserRessource;
import net.htlgkr.krejo.toDoList.management.ManagerActivity;
import net.htlgkr.krejo.toDoList.management.ToDoList.ToDoListActivity;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
    // TODO: 24.05.2023 gonz spät amoi wos mocha dass ma offline a se irgendwie eiloggn ko
    private User user;
    private UserRessource userRessource;
    private UserDTO userDTO;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLoginOK;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String REGISTER_URL = "https://www.docsced.at/notesserver/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_login);

        if (!isNetworkAvailable()){
            return;
        }

        setUpViews();
        buttonLoginOK.setOnClickListener(v -> sendRegisterPOSTRequestAsync());

    }

    private void sendRegisterPOSTRequestAsync() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, UserRessource> asyncTask
                = new AsyncTask<Void, Void, UserRessource>() {
            @Override
            protected UserRessource doInBackground(Void... voids) {
                try {
                    userRessource = sendRegisterPOSTRequest();
                    return userRessource;
                } catch (InvalidUserException e) {
                    showToasts();
                    return null;
                }
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(UserRessource userRessource) {
                if (userRessource != null) {
                    Log.i(TAG,
                            "onPostExecute: new userRessource with id "
                                    + userRessource.getUserId() + " was found");
                    startNewActivity();
                } else {
                    Log.e(TAG, "onPostExecute: userRessource is null");
                }
            }
        };

        asyncTask.execute();
    }

    private void showToasts() {
        Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Remember no spaces are allowed!", Toast.LENGTH_SHORT).show();
    }

    private UserRessource sendRegisterPOSTRequest() throws InvalidUserException {
        try {
            userDTO = createDTOFromEditTexts();


            HttpsURLConnection httpsURLConnection = null;
            String response = "";

            try {
                httpsURLConnection = getHttpsURLConnection();
                String body = objectMapper.writeValueAsString(userDTO);

                writeOutput(body, httpsURLConnection);

                if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                    response = getResponse(httpsURLConnection);
                    Log.i(TAG, "sendRegisterPOSTRequest: response from server: " + response);
                } else {
                    Log.e(TAG, "POST Request failed: "
                            + httpsURLConnection.getResponseCode()
                            + " " +
                            httpsURLConnection.getResponseMessage());
                }

                httpsURLConnection.disconnect();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return objectMapper.readValue(response, UserRessource.class);

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private UserDTO createDTOFromEditTexts() throws InvalidUserException {
        String usernameContent = editTextUsername.getText().toString();
        String passwordContent = editTextPassword.getText().toString();

        if (usernameContent.length() >= 8 && passwordContent.length() >= 8 && !usernameContent.contains(" ") && !passwordContent.contains(" ")) {

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
        buttonLoginOK = findViewById(R.id.button_login_ok);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkActive = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.i(TAG, "isNetworkAvailable() returned: " + isNetworkActive);

        return isNetworkActive;
    }


    private void startNewActivity() {
        Intent intent = new Intent(this, ManagerActivity.class);
        intent.putExtra("user", createUser());
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

    private static HttpsURLConnection getHttpsURLConnection() throws IOException {
        HttpsURLConnection httpsURLConnection;

        URL url = new URL(REGISTER_URL);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();

        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        httpsURLConnection.setDoOutput(true);

        return httpsURLConnection;
    }

    private static void writeOutput(String body, HttpsURLConnection httpsURLConnection) throws IOException {
        try (OutputStream os = httpsURLConnection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private static String getResponse(HttpURLConnection httpURLConnection) throws IOException {

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }
}
