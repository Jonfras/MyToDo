package net.htlgkr.krejo.toDoList;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.htlgkr.krejo.toDoList.login.exceptions.InvalidUserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

public class HTTPSHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Object sendRequest(String siteURL, String method, Optional<Object> dto, Object responseType)
            throws IOException {

        HttpsURLConnection httpsURLConnection = getHttpsURLConnection(siteURL, method);
        String response = "";

        if ((method.equals(ConstantsMyToDo.POST) || method.equals(ConstantsMyToDo.PUT))
                    && dto.isPresent()) {
                    String body = objectMapper.writeValueAsString(dto.get());
                    Log.i(TAG, "sendRequest: body: " + body);
                    writeOutput(body, httpsURLConnection);
                }


                if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_CREATED
                    || httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    response = getResponse(httpsURLConnection);
                    Log.i(TAG, "sendRequest: response from server: " + response);
                } else {
                    Log.e(TAG, "Request failed: "
                            + httpsURLConnection.getResponseCode()
                            + " " +
                            httpsURLConnection.getResponseMessage());
                }
                httpsURLConnection.disconnect();
                //i woas ned ob des mit listn a geht
            return objectMapper.readValue(response, responseType.getClass());
    }

    private static HttpsURLConnection getHttpsURLConnection(String siteURL, String method) throws IOException {
        HttpsURLConnection httpsURLConnection;

        URL url = new URL(ConstantsMyToDo.BASE_URL + siteURL);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();

        httpsURLConnection.setRequestMethod(method);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        httpsURLConnection.setRequestProperty("Accept", "application/json");

        if (method.equals(ConstantsMyToDo.POST) || method.equals(ConstantsMyToDo.PUT)) {
            httpsURLConnection.setDoOutput(true);
        }

        return httpsURLConnection;
    }

    private static void writeOutput(String body, HttpsURLConnection httpsURLConnection)
            throws IOException {
        try (OutputStream os = httpsURLConnection.getOutputStream()) {
            byte[] output = body.getBytes(StandardCharsets.UTF_8);
            os.write(output, 0, output.length);
        }
    }

    private static String getResponse(HttpURLConnection httpURLConnection) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }
}
