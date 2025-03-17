package com.dsa.dsa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javafx.scene.text.Text;

public class Controller {
    @FXML
    private Button btn_saveKeys;

    @FXML
    private Button btn_sign;

    @FXML
    private Button btn_verify;

    @FXML
    private Text text_file_created;

    @FXML
    private Text text_file_extension;

    @FXML
    private Text text_file_modified;

    @FXML
    private Text text_file_path;

    @FXML
    private Text text_file_size;

    @FXML
    private Text text_file_hash;

    @FXML
    private Text text_verify_answer;

    @FXML
    private Text text_private_key_path;

    @FXML
    private Text text_public_key_path;

    @FXML
    private TextArea textarea_private_key;

    @FXML
    private TextArea textarea_public_key;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    DSALogic logic = new DSALogic();

    File inputFile;
    String filePath;
    String fileSize;
    String fileExtension;
    String fileHash;
    String fileModified;
    String fileCreated;
    File privFile;
    File pubFile;
    String[] keys = new String[2];
    File signFile;

    //Buttons
    boolean privateKeyIsPresent = false;
    boolean publicKeyIsPresent = false;

    @FXML
    void initialize(){
        textarea_public_key.setWrapText(true);
        textarea_private_key.setWrapText(true);
        btn_verify.setDisable(true);
        btn_sign.setDisable(true);
        btn_saveKeys.setDisable(true);
    }

    @FXML
    void handleChooseFileButton(ActionEvent event) throws IOException {
        //Choose file path
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        inputFile = fileChooser.showOpenDialog(stage);

        if (inputFile != null) {
            //If File loaded successfully
            System.out.println("Input file: OK");

            //Get path of file
            filePath = inputFile.getAbsolutePath();

            //Get size of file in KB
            fileSize = (filePath.length() / 1024)+1 + " KB";

            //Get hash of file
            fileHash = logic.createFileHash(inputFile);

            //Get last edited date
            fileModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(inputFile.lastModified());

            //Get file created date
            BasicFileAttributes fileAttributes = Files.readAttributes(Path.of(filePath), BasicFileAttributes.class);
            fileCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date(fileAttributes.creationTime().toMillis()));

            //Get extension
            int dotIndex = filePath.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < filePath.length() - 1)
                fileExtension = filePath.substring(dotIndex + 1);

            //Print to GUI
            text_file_path.setText(filePath);
            text_file_extension.setText(fileExtension);
            text_file_size.setText(fileSize);
            text_file_created.setText(fileCreated);
            text_file_modified.setText(fileModified);
            text_file_hash.setText(fileHash.substring(0, 77) + "\n" + fileHash.substring(77));

        } else {
            //If File somehow wasn't loaded. Should not ever be the case (But just in case :D)
            System.err.println("Input file: WASN'T LOADED");
        }

        if (fileExtension != null && fileExtension.equals("zip") && publicKeyIsPresent) {
            btn_verify.setDisable(false);
        } else {
            btn_verify.setDisable(true);
        }
        if (privateKeyIsPresent && inputFile != null) {
            btn_sign.setDisable(false);
        } else {
            btn_sign.setDisable(true);
        }

        text_verify_answer.setText("");
    }

    @FXML
    void handleGenerateKeyButton(ActionEvent event) {
        //Generate Keys
        //[Public key, Private key]
        keys = logic.generatePrivateAndPublicKeys();

        //Set public values in RSALogic from keys
        logic.parseKey(keys[1], false);
        logic.parseKey(keys[0], true);

        //Update GUI
        textarea_public_key.setText(keys[0]);
        textarea_private_key.setText(keys[1]);
        text_public_key_path.setText("");
        text_private_key_path.setText("");

        publicKeyIsPresent = true;
        privateKeyIsPresent = true;

        if (inputFile != null) {
            if (fileExtension != null && fileExtension.equals("zip")) btn_verify.setDisable(false);
            btn_sign.setDisable(false);
        } else {
            if (fileExtension != null && !fileExtension.equals("zip")) btn_verify.setDisable(true);
            btn_sign.setDisable(true);
        }

        text_verify_answer.setText("");
        btn_saveKeys.setDisable(false);
    }

    @FXML
    void handleSaveKeysButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        //If keys are generated or loaded -> Save them
        if (keys[0] != null && keys[1] != null) {

            //Public Key
            fileChooser.setTitle("Save Public Key");
            //Set extensions
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".pub", "*.pub"));
            //Set default folder (Project folder)
            fileChooser.setInitialFileName(System.getProperty("user.name"));

            pubFile = fileChooser.showSaveDialog(stage);

            //If key path was chosen
            if (pubFile != null) {
                //Write key into file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(pubFile))) {
                    writer.write("RSA " + keys[0]);
                    //Update GUI
                    text_public_key_path.setText(pubFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Saving Public Key: ERROR");
                    System.err.println(e.getMessage());
                }
            } else {
                System.err.println("Saving Public Key: CANCELED");
            }

            fileChooser.getExtensionFilters().clear();

            //Private Key
            fileChooser.setTitle("Save Private Key");
            //Set extensions
            fileChooser.setInitialFileName(System.getProperty("user.name"));
            //Set default folder (Project folder)
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".priv", "*.priv"));

            privFile = fileChooser.showSaveDialog(stage);

            //If key path was chosen
            if (privFile != null) {
                //Write key into file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(privFile))) {
                    writer.write("RSA " + keys[1]);
                    //Update GUI
                    text_private_key_path.setText(privFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Saving Private Key: ERROR");
                    System.err.println(e.getMessage());
                }
            } else {
                System.err.println("Saving Private Key: CANCELED");
            }
        } else {
            System.err.println("Can't save keys. Keys weren't probably generated or loaded (Why would anybody like to save loaded keys? :D)");
        }
    }

    @FXML
    void handleLoadPublicKeyButton(ActionEvent event) {
        //Choose key path
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Private Key");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".pub", "*.pub"));
        pubFile = fileChooser.showOpenDialog(stage);

        //Read key after "RSA "
        try (BufferedReader reader = new BufferedReader(new FileReader(pubFile))) {
            keys[0] = reader.readLine().split(" ")[1];
            logic.parseKey(keys[0], true);
            publicKeyIsPresent = true;
        } catch (IOException e) {
            System.err.println("Reading Public Key: ERROR");
            System.err.println(e.getMessage());
            publicKeyIsPresent = false;
        }

        //Update GUI
        textarea_public_key.setText(keys[0]);
        text_public_key_path.setText(pubFile.getAbsolutePath());
        logic.parseKey(keys[0], true);

        text_verify_answer.setText("");

        btn_verify.setDisable(!publicKeyIsPresent || !fileExtension.equals("zip"));
        btn_saveKeys.setDisable(!publicKeyIsPresent || !privateKeyIsPresent);
    }

    @FXML
    void handleLoadPrivateKeyButton(ActionEvent event) {
        //Choose key path
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Private Key");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".priv", "*.priv"));
        privFile = fileChooser.showOpenDialog(stage);

        //Read key after "RSA "
        try (BufferedReader reader = new BufferedReader(new FileReader(privFile))) {
            keys[1] = reader.readLine().split(" ")[1];
            logic.parseKey(keys[1], false);
            privateKeyIsPresent = true;
        } catch (IOException e) {
            System.err.println("Reading Private Key: ERROR");
            System.err.println(e.getMessage());
            privateKeyIsPresent = false;
        }

        //Update GUI
        textarea_private_key.setText(keys[1]);
        text_private_key_path.setText(privFile.getAbsolutePath());

        btn_sign.setDisable(!privateKeyIsPresent || inputFile == null);
        btn_saveKeys.setDisable(!publicKeyIsPresent || !privateKeyIsPresent);
    }

    @FXML
    void handleSignFileButton(ActionEvent event) {
        //Create signature of file
        String signature = logic.createSignature(fileHash);

        //Create fileObject .temp file in project folder, default name of file: Name of user
        signFile = new File(System.getProperty("user.dir"), System.getProperty("user.name") + ".sign");

        //Try creating file
        try {
            if (signFile.createNewFile()) {
                System.out.println("Creating signature file: OK");
            } else {
                System.err.println("Creating signature file: ERROR if (signFile.createNewFile())");
                return;
            }

            //Write content into .sign file
            BufferedWriter writer = new BufferedWriter(new FileWriter(signFile));
            writer.write("RSA_SHA3-512 " + signature);
            writer.close();

        } catch (IOException e) {
            System.out.println("Creating signature file: ERROR");
            System.err.println(e.getMessage());
            signFile.delete();
        }

        //Choose .zip file destination
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select .ZIP destination");
        fileChooser.setInitialFileName(System.getProperty("user.name") + ".zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".zip", "*.zip"));
        File fileZIP = fileChooser.showSaveDialog(stage);

        //If file destination was selected
        if (fileZIP != null) {
            String filePathZIP = fileZIP.getAbsolutePath();

            //In case if file doesn't end with .zip
            if (!filePathZIP.endsWith(".zip")) filePathZIP += ".zip";

            //[Signature file path, Input file path]
            String[] filePaths = {signFile.getAbsolutePath(), inputFile.getAbsolutePath()};

            try (FileOutputStream fileoutputstream = new FileOutputStream(filePathZIP);
            ZipOutputStream zipoutputstream = new ZipOutputStream(fileoutputstream)) {

                for (String filePath : filePaths) {File file = new File(filePath);
                    try (FileInputStream fis = new FileInputStream(file)) {
                        //Put file into zip
                        ZipEntry zipEntry = new ZipEntry(file.getName());

                        //Move to another position in zip
                        zipoutputstream.putNextEntry(zipEntry);

                        byte[] buffer = new byte[1024];

                        //Write content
                        int length;
                        while ((length = fis.read(buffer)) >= 0) zipoutputstream.write(buffer, 0, length);

                        //Close entry
                        zipoutputstream.closeEntry();
                    } catch (IOException e ) {
                        System.err.println("Creating ZIP: ERROR");
                        System.out.println(e.getMessage());
                    }
                }

                System.out.println("Zip file created successfully: " + filePathZIP);

                //Deleting .sign temp file
                if (signFile.delete()) {
                    System.out.println("Temp .sign file was deleted.");
                } else {
                    System.err.println("Error while deleting temp .sign file");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            signFile.delete();
        }
    }

    @FXML
    void handleVerifyFileButton(ActionEvent event) {
        //Set temp folder path
        String outputPath = System.getProperty("user.dir") + "\\temp";
        File tempFolder = new File(outputPath);

        //Delete any files which are in temp folder of project
        File[] tempFilesCheck = tempFolder.listFiles();
        if (tempFilesCheck != null) {
            for(File tempFile : tempFilesCheck) {
                if (tempFile.delete()) {
                    System.out.println("Temp file " + tempFile.getName() + " was deleted!");
                } else {
                    System.out.println("Temp file " + tempFile.getName() + " was not deleted!");
                }
            }
        }

        //Read .zip file and put content into temp folder
        try (FileInputStream fis = new FileInputStream(inputFile);
        ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File outFile = new File(outputPath, zipEntry.getName());

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    System.err.println("Error while reading .zip file.");
                    System.err.println(e.getMessage());
                }

                outFile.deleteOnExit();

                zis.closeEntry();
            }

            System.out.println("Zip file extracted successfully to: " + outputPath);

        } catch (IOException e) {
            System.err.println("Error while reading .zip file.");
            System.err.println(e.getMessage());
        }

        File[] files = tempFolder.listFiles();

        String signature = "";
        File inputFileFromZip = null;

        if (files != null && files.length == 2) {

            for (File file : files) {
                if (file.isFile()) {
                    //If signature file
                    if (file.getName().endsWith(".sign")) {
                        //Get signature from .sign file
                        try {
                            Path filePath = Paths.get(file.getAbsolutePath());
                            signature = Files.readString(filePath).split(" ")[1];
                            System.out.println("Signature from file: " + signature);

                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file.getName());
                            System.err.println(e.getMessage());
                        }
                    } else {
                        System.out.println("Input file: " + file.getName());
                        inputFileFromZip = file;
                    }
                }
            }
        }

        //Get if file is verified
        if (logic.verifySignature(inputFileFromZip, signature)) {
            text_verify_answer.setText("-> OK");
            text_verify_answer.setFill(Color.GREEN);
        } else {
            text_verify_answer.setText("-> FAIL");
            text_verify_answer.setFill(Color.RED);
        }
    }
}
