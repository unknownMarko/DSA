package com.dsa.dsa;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSALogic {

    public BigInteger p = null;
    public BigInteger q = null;
    public BigInteger n = null;
    public BigInteger phi = null;
    public BigInteger e = null;
    public BigInteger d = null;

    private static final int BLOCK_SIZE = 6;

    //Generuje prvocisla o dlzky 13 cislic (Zadane na hodine)
    public BigInteger generatePrimeNumber() {
        BigInteger number;
        do {
            number = new BigInteger(new BigInteger("9000000000000").bitLength(), new Random());
            number = number.mod(new BigInteger("9000000000000")).add(new BigInteger("1000000000000"));
        } while (!isPrime(number));
        return number;
    }
    //Kontroluje, ci je cislo prvocislo. Pomocna funkcia pre generatePrimeNumber()
    public boolean isPrime(BigInteger n) {
        for (int i = 2; i <= Math.sqrt(n.doubleValue()); i++)
            if (n.mod(BigInteger.valueOf(i)).equals(BigInteger.ZERO))
                return false;
        return true;
    }


    public void generate() {
        //Generuje P a Q
        do {
            p = generatePrimeNumber();
            q = generatePrimeNumber();
        } while (p.equals(q));

        //Generuje PHI (p − 1)(q − 1)
        phi = (p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));

        e = generateE();

        // Vypocita N (P*Q)
        n = p.multiply(q);

        d = e.modInverse(phi);
    }

    public BigInteger generateE() {
        BigInteger e;
        Random random = new Random();
        do {
            e = new BigInteger(phi.bitLength() - 1, random);
        } while (!gcd(e, phi).equals(BigInteger.ONE) || e.compareTo(BigInteger.ONE) <= 0 || e.compareTo(phi) >= 0);
        return e;
    }

    public BigInteger gcd(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) return a;
        return gcd(b, a.mod(b));
    }

    public List<BigInteger> encryptMessage(String message) {
        List<BigInteger> encrypted = new ArrayList<>();
        List<BigInteger> blocks = convertMessageToBlocks(message);

        for (BigInteger block : blocks) {
            BigInteger encryptedBlock = block.modPow(d, n);
            encrypted.add(encryptedBlock);
        }

        return encrypted;
    }

    public String decryptMessage(List<BigInteger> encryptedBlocks) {
        List<BigInteger> decrypted = new ArrayList<>();

        for (BigInteger block : encryptedBlocks) {
            BigInteger decryptedBlock = block.modPow(e, n);
            decrypted.add(decryptedBlock);
        }

        return convertBlocksToMessage(decrypted);
    }

    public List<BigInteger> convertMessageToBlocks(String message) {
        List<BigInteger> blocks = new ArrayList<>();

        while (message.length() % BLOCK_SIZE != 0) message += " ";

        for (int i = 0; i < message.length(); i += BLOCK_SIZE) {
            StringBuilder blockValue = new StringBuilder();

            for (int j = 0; j < BLOCK_SIZE && (i + j) < message.length(); j++) {
                String asciiValue = String.format("%03d", (int)message.charAt(i + j));
                blockValue.append(asciiValue);
            }

            blocks.add(new BigInteger(blockValue.toString()));
        }

        return blocks;
    }

    public String convertBlocksToMessage(List<BigInteger> blocks) {
        StringBuilder message = new StringBuilder();

        for (BigInteger block : blocks) {
            String blockStr = String.format("%018d", block);

            for (int i = 0; i < blockStr.length(); i += 3) {
                String asciiStr = blockStr.substring(i, i + 3);
                int asciiVal = Integer.parseInt(asciiStr);
                if (asciiVal > 0) {
                    message.append((char)asciiVal);
                }
            }
        }

        return message.toString().trim();
    }

    public String convertTextToHexPairs(String text) {
        StringBuilder output = new StringBuilder("[");
        for (int i = 0; i < text.length(); i++) {
            output.append(String.format("%d", (int)text.charAt(i)));
            if (i < text.length() - 1) output.append(", ");
        }
        output.append("]");
        return output.toString();
    }
}
