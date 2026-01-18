package com.starmuseum.modules.catalog.util;

import java.io.InputStream;
import java.security.MessageDigest;

public class Sha256Util {

    private Sha256Util() {}

    public static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(bytes);
            return toHex(dig);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String sha256Hex(InputStream in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            return toHex(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 compute failed", e);
        }
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            String h = Integer.toHexString(x & 0xff);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return sb.toString();
    }
}
