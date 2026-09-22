package it.nexera.ris.common.helpers;

import org.apache.commons.lang3.ArrayUtils;

import java.security.SecureRandom;
import java.util.Random;

public class PasswordUtils {
    private static char[] SYMBOLS = (".,<>?^&*()=/@#$%!").toCharArray();
    private static char[] LOWERCASE = ("abcdefghijklmnopqrstuvwxyz").toCharArray();
    private static char[] UPPERCASE = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
    private static char[] NUMBERS = ("0123456789").toCharArray();
    private static Random rand = new SecureRandom();

    public static String generatePassword(int length) {
        char[] password = new char[length];

        //get the requirements out of the way
        password[0] = LOWERCASE[rand.nextInt(LOWERCASE.length)];
        password[1] = UPPERCASE[rand.nextInt(UPPERCASE.length)];
        password[2] = NUMBERS[rand.nextInt(NUMBERS.length)];
        password[3] = SYMBOLS[rand.nextInt(SYMBOLS.length)];

        char[] allChars = ArrayUtils.addAll(ArrayUtils.addAll(ArrayUtils.addAll(SYMBOLS, LOWERCASE), UPPERCASE), NUMBERS);

        //populate rest of the password with random chars
        for (int i = 4; i < length; i++) {
            password[i] = allChars[rand.nextInt(allChars.length)];
        }

        //shuffle it up
        for (int i = 0; i < password.length; i++) {
            int randomPosition = rand.nextInt(password.length);
            char temp = password[i];
            password[i] = password[randomPosition];
            password[randomPosition] = temp;
        }

        return new String(password);
    }


}
