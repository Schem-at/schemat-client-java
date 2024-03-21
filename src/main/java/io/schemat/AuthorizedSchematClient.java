package io.schemat;

import org.apache.http.client.HttpClient;

public class AuthorizedSchematClient extends SchematClient implements IAuthorizedSchematClient {
    protected final String jwt;

    public AuthorizedSchematClient(HttpClient httpClient, String jwt) {
        super(httpClient);
        this.jwt = jwt;
    }
}
