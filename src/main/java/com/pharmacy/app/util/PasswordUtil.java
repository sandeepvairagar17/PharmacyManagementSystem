package com.pharmacy.app.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hash(String plainPassword){
        return BCrypt.hashpw(plainPassword,BCrypt.gensalt());
    }

    public static boolean verify(String plainPassword, String storedHash){
        return BCrypt.checkpw(plainPassword,storedHash);
    }
}
