package io.schemat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.UUID;

// TODO: Handle exceptions properly
// TODO: Use something like lombok for the request and response classes
// TODO: Maybe move some http boilerplate to the helpers class
// TODO: Add user agent to http requests (probably move that to the helpers class)

public class SchematClient implements ISchematClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected final HttpClient httpClient;

    public SchematClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public SchematClient() {
        this(HttpClients.createDefault());
    }

    @Override
    public IAuthorizedSchematClient authorizeUsingPassword(String username, String password) {
        // TODO: Call the schemat api using username and password

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public IAuthorizedSchematClient authorizeUsingMojang(String username, UUID uuid, String accessToken) {
        String serverId = Helpers.generateSecureRandomServerId();

        joinMojangServer(accessToken, Helpers.uuidToStringWithoutDashes(uuid), serverId);

        String jwt = getJwtUsingServerId(username, serverId);

        return new AuthorizedSchematClient(httpClient, jwt);
    }

    private void joinMojangServer(String accessToken, String selectedProfile, String serverId) {
        MojangJoinRequest mojangJoinRequest = new MojangJoinRequest(accessToken, selectedProfile, serverId);

        String mojangJoinJson;
        try {
            mojangJoinJson = objectMapper.writeValueAsString(mojangJoinRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpPost request = new HttpPost("https://sessionserver.mojang.com/session/minecraft/join");
        request.setEntity(new StringEntity(mojangJoinJson, ContentType.APPLICATION_JSON));

        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.getStatusLine().getStatusCode() != 204) {
            throw new RuntimeException("Failed to join server: " + response.getStatusLine().getReasonPhrase());
        }
    }

    private String getJwtUsingServerId(String username, String serverId) {
        SchematAuthorizeRequest schematAuthorizeRequest = new SchematAuthorizeRequest(username, serverId);

        String schematAuthorizeJson;
        try {
            schematAuthorizeJson = objectMapper.writeValueAsString(schematAuthorizeRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpPost request = new HttpPost("https://schemat.io/api/v1/authorize-mojang");
        request.setEntity(new StringEntity(schematAuthorizeJson, ContentType.APPLICATION_JSON));

        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed to authorize: " + response.getStatusLine().getReasonPhrase());
        }

        SchematAuthorizeResponse schematAuthorizeResponse;
        try {
            schematAuthorizeResponse = objectMapper.readValue(response.getEntity().getContent(), SchematAuthorizeResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return schematAuthorizeResponse.getJwt();
    }

    private static class MojangJoinRequest {
        private String accessToken;
        private String selectedProfile;
        private String serverId;

        public MojangJoinRequest(String accessToken, String selectedProfile, String serverId) {
            this.accessToken = accessToken;
            this.selectedProfile = selectedProfile;
            this.serverId = serverId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getSelectedProfile() {
            return selectedProfile;
        }

        public void setSelectedProfile(String selectedProfile) {
            this.selectedProfile = selectedProfile;
        }

        public String getServerId() {
            return serverId;
        }

        public void setServerId(String serverId) {
            this.serverId = serverId;
        }
    }

    private static class SchematAuthorizeRequest {
        private String username;
        private String serverId;

        public SchematAuthorizeRequest(String username, String serverId) {
            this.username = username;
            this.serverId = serverId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getServerId() {
            return serverId;
        }

        public void setServerId(String serverId) {
            this.serverId = serverId;
        }
    }

    private static class SchematAuthorizeResponse {
        private String jwt;

        public SchematAuthorizeResponse(String jwt) {
            this.jwt = jwt;
        }

        public String getJwt() {
            return jwt;
        }

        public void setJwt(String jwt) {
            this.jwt = jwt;
        }
    }
}
