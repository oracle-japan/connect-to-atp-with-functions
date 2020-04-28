package com.example.fn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class HelloFunction {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public static class User {
        public String id;
        @JsonAlias("username")
        public String username;
        @JsonAlias("first_name")
        public String firstName;
        @JsonAlias("last_name")
        public String lastName;
        @JsonAlias("created_on")
        public Date createdOn;
        @JsonIgnore
        public List links;
    }

    public User handleRequest(String username) {
        User user = null;
        try {
            String ordsBaseUrl = System.getenv().get("ords_base_url");
            HttpRequest request = HttpRequest.newBuilder(new URI(ordsBaseUrl + "/users/user/" + username))
                    .header("Authorization", "Bearer " + getAuthToken())
                    .GET()
                    .build();
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.headers());
            System.out.println(response.body());
            if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                System.out.println("User with username " + username + " not found!");
            } else {
                user = new ObjectMapper().readValue(response.body(), User.class);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

    private String getAuthToken() {
        String authToken = "";
        try {
            String ordsBaseUrl = System.getenv().get("ords_base_url");
            String clientId = System.getenv().get("client_id");
            String clientSecret = System.getenv().get("client_secret");
            System.out.println("ordsBaseUrl:" + ordsBaseUrl);
            System.out.println("clientId:" + clientId);
            System.out.println("clientSecret:" + clientSecret);

            String authString = clientId + ":" + clientSecret;
            String authEncoded = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
            System.out.println("oauth url:" + ordsBaseUrl + "/oauth/token");
            HttpRequest request = HttpRequest.newBuilder(new URI(ordsBaseUrl + "/oauth/token"))
                    .header("Authorization", authEncoded)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                    .build();
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response:" + response);
            String responseBody = response.body();
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
            };
            HashMap<String, String> result = mapper.readValue(responseBody, typeRef);
            authToken = result.get("access_token");
            System.out.println("authToken:" + authToken);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return authToken;
    }
}
