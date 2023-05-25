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

import javax.net.ssl.HttpsURLConnection;

public class HTTPSHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Object sendRequest(String url, String method, Object dto, Object responseType) throws InvalidUserException {
        try {
            HttpsURLConnection httpsURLConnection = null;
            String response = "";

            try {
                httpsURLConnection = getHttpsURLConnection(url, method);

                // TODO: 25.05.2023 Moch des so dynamisch dass get und delete a geht mit der methode!
                if (method.equals(ConstantsMyToDo.POST) || method.equals(ConstantsMyToDo.PUT))
                String body = objectMapper.writeValueAsString(dto);

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

            return objectMapper.readValue(response, responseType.getClass());

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpsURLConnection getHttpsURLConnection(String urlString, String method) throws IOException {
        HttpsURLConnection httpsURLConnection;

        URL url = new URL(ConstantsMyToDo.BASE_URL + urlString);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();

        httpsURLConnection.setRequestMethod(method);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        httpsURLConnection.setDoOutput(true);

        return httpsURLConnection;
    }

    private static void writeOutput(String body, HttpsURLConnection httpsURLConnection)
            throws IOException {
        try (OutputStream os = httpsURLConnection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
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
