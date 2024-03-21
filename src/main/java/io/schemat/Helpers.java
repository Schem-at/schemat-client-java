package io.schemat;

import java.security.SecureRandom;
import java.util.UUID;

public final class Helpers {
    private Helpers() {
    }

    public static String uuidToStringWithoutDashes(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    public static String generateSecureRandomServerId() {
        return generateSecureRandomHexString(64);
    }

    private static String generateSecureRandomHexString(int bytes) {
        byte[] randomBytes = new byte[bytes];
        new SecureRandom().nextBytes(randomBytes);

        return bytesToHex(randomBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
