package com.bankingsystem.util;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;


public class AccountNumberGenerator {
    private static final Random random = new Random();

    public static String generate(String holderName) {
        String initials = Arrays.stream(holderName.trim().toUpperCase()
                .replaceAll("\\s+", " ")
                .split(" "))
                .map(s -> s.substring(0, 1))
                .collect(Collectors.joining());
        int num = 1000 + random.nextInt(9000);
        return initials + num;
    }
}
