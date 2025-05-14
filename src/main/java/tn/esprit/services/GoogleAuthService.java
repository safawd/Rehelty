package tn.esprit.services;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class GoogleAuthService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Informations OAuth (à remplacer par vos propres valeurs)
    String clientId = "689140770366-3gqlqiagvej6gvkbir5gnfv6it94gdlt.apps.googleusercontent.com";
    String clientSecret = "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";

    private String redirectUri = "urn:ietf:wg:oauth:2.0:oob"; // Redirection spéciale pour les applications de bureau

    // URLs OAuth de Google
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    // Portées (scopes) demandées
    private static final String SCOPE = "email profile openid";

    public GoogleAuthService() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/google_oauth.properties"));

            clientId = props.getProperty("client_id");
            clientSecret = props.getProperty("client_secret");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la configuration OAuth: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CompletableFuture<GoogleUser> authenticate() {
        CompletableFuture<GoogleUser> future = new CompletableFuture<>();

        try {
            // Construire l'URL d'authentification
            String authUrl = AUTH_URL +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code" +
                    "&scope=" + SCOPE.replace(" ", "%20");

            // Créer la fenêtre WebView
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            Stage authStage = new Stage();
            authStage.setTitle("Connexion avec Google");
            authStage.setScene(new Scene(webView, 800, 600));

            // Gérer la redirection après l'authentification
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == State.SUCCEEDED) {
                    String location = webEngine.getLocation();
                    System.out.println("URL actuelle: " + location);

                    // Détecter le code d'autorisation dans la page
                    if (location.contains("approval")) {
                        // Google affiche le code sur une page spéciale pour les applications de bureau
                        String pageContent = (String) webEngine.executeScript("document.body.innerText");
                        System.out.println("Contenu de la page: " + pageContent);

                        // Extraire le code d'autorisation
                        String authCode = null;
                        if (pageContent.contains("code=")) {
                            authCode = pageContent.substring(pageContent.indexOf("code=") + 5);
                            authCode = authCode.split("\\s")[0]; // Prendre le premier mot après "code="
                        } else {
                            // Extraction alternative en cherchant dans le texte
                            for (String line : pageContent.split("\n")) {
                                if (line.matches(".*[A-Za-z0-9_\\-]{20,}.*")) {
                                    authCode = line.trim();
                                    break;
                                }
                            }
                        }

                        if (authCode != null) {
                            System.out.println("Code d'autorisation trouvé: " + authCode);
                            authStage.close();

                            // Échanger le code contre un token
                            exchangeCodeForToken(authCode)
                                    .thenAccept(googleUser -> {
                                        future.complete(googleUser);
                                    })
                                    .exceptionally(ex -> {
                                        future.completeExceptionally(ex);
                                        return null;
                                    });
                        }
                    }
                }
            });

            // Charger l'URL d'autorisation
            webEngine.load(authUrl);
            authStage.show();

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<GoogleUser> exchangeCodeForToken(String authCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Construire la requête pour échanger le code contre un token
                URL url = new URL(TOKEN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String params = "code=" + authCode +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&redirect_uri=" + redirectUri +
                        "&grant_type=authorization_code";

                conn.getOutputStream().write(params.getBytes());

                // Lire la réponse
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // Analyser la réponse JSON
                    Reader reader = new InputStreamReader(conn.getInputStream());
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = reader.read()) != -1) {
                        sb.append((char) c);
                    }

                    JSONObject jsonResponse = new JSONObject(sb.toString());
                    String accessToken = jsonResponse.getString("access_token");

                    // Utiliser le token pour obtenir les informations de l'utilisateur
                    return getUserInfo(accessToken);
                } else {
                    throw new IOException("Erreur lors de l'échange du code: " + responseCode);
                }
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'échange du code: " + e.getMessage(), e);
            }
        });
    }

    private GoogleUser getUserInfo(String accessToken) throws IOException {
        // Requête pour obtenir les informations de l'utilisateur
        URL url = new URL(USER_INFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        // Lire la réponse
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Reader reader = new InputStreamReader(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONObject userInfo = new JSONObject(sb.toString());

            // Créer et retourner un objet GoogleUser
            GoogleUser user = new GoogleUser();
            user.setId(userInfo.getString("sub"));
            user.setEmail(userInfo.getString("email"));
            user.setName(userInfo.optString("name", ""));
            user.setGivenName(userInfo.optString("given_name", ""));
            user.setFamilyName(userInfo.optString("family_name", ""));
            user.setPicture(userInfo.optString("picture", ""));

            return user;
        } else {
            throw new IOException("Erreur lors de la récupération des informations utilisateur: " + responseCode);
        }
    }

    // Classe interne pour stocker les informations de l'utilisateur Google
    public static class GoogleUser {
        private String id;
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        private String picture;

        // Getters et setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }

        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }

        @Override
        public String toString() {
            return "GoogleUser{" +
                    "id='" + id + '\'' +
                    ", email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", givenName='" + givenName + '\'' +
                    ", familyName='" + familyName + '\'' +
                    '}';
        }
    }

    private GoogleUser googleUser;

    public GoogleUser getGoogleUser() {
        return googleUser;
    }
}
