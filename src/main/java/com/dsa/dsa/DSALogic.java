package com.dsa.dsa;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DSALogic {

    RSALogic rsa_logic = new RSALogic();

    public String createFileHash(File file) {
        try {
            MessageDigest digest = new SHA3.Digest512();

            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            fis.close();

            byte[] hashByteArray = digest.digest();

            //Debug Prints
            System.out.println(Arrays.toString(hashByteArray));
            System.out.println(Base64.getEncoder().encodeToString(hashByteArray));

            return Base64.getEncoder().encodeToString(hashByteArray);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String[] generatePrivateAndPublicKeys() {
        rsa_logic.generate();

        String publicKeyRSA = rsa_logic.e.toString() + "," + rsa_logic.n.toString();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyRSA.getBytes());

        String privateKeyRSA = rsa_logic.d.toString() + "," + rsa_logic.n.toString();
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyRSA.getBytes());

        System.out.println("Private Key: " + privateKeyBase64);
        System.out.println("Public Key: " + publicKeyBase64);

        return new String[]{publicKeyBase64, privateKeyBase64};
    }

    // typeOfKey
    // True = Public Key
    // False = Private Key
    public void parseKey(String key, boolean typeOfKey) {

        String[] values = new String(Base64.getDecoder().decode(key)).split(",");

        if (typeOfKey) {
            rsa_logic.e = new BigInteger(values[0]);
            System.out.println("rsa_logic.e = " + rsa_logic.e);
        } else {
            rsa_logic.d = new BigInteger(values[0]);
            System.out.println("rsa_logic.d = " + rsa_logic.d);

        }
        rsa_logic.n = new BigInteger(values[1]);
        System.out.println("rsa_logic.n = " + rsa_logic.n);
    }

    public String createSignature(String fileHash) {
        // Use RSA block encryption on Base64 hash string
        List<BigInteger> encryptedBlocks = rsa_logic.encryptMessage(fileHash);

        // blocks to string
        StringBuilder blocksStr = new StringBuilder();
        for (BigInteger block : encryptedBlocks) blocksStr.append(block.toString()).append(",");
        if (!blocksStr.isEmpty()) blocksStr.setLength(blocksStr.length() - 1);

        System.out.println("createSignature blocks: " + blocksStr);

        return Base64.getEncoder().encodeToString(blocksStr.toString().getBytes());
    }

    public boolean verifySignature(File inputFile, String signature) {
        try {
            String signatureStr = new String(Base64.getDecoder().decode(signature));
            String[] blockStrings = signatureStr.replaceAll(" ", "").split(",");

            // Parsing blocks to ArrayList
            List<BigInteger> encryptedBlocks = new ArrayList<>();
            for (String blockStr : blockStrings) encryptedBlocks.add(new BigInteger(blockStr));

            return rsa_logic.decryptMessage(encryptedBlocks).equals(createFileHash(inputFile));
        } catch (Exception e) {
//            System.err.println(e.getMessage());
            return false;
        }
    }
}
