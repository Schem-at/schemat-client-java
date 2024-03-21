package io.schemat;

import java.util.UUID;

public interface ISchematClient {
    IAuthorizedSchematClient authorizeUsingPassword(String username, String password);
    IAuthorizedSchematClient authorizeUsingMojang(String username, UUID uuid, String accessToken);

    // TODO: Add methods that don't require authorization
}
