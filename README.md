# Digital Signature Algorithm

![](https://github.com/unknownMarko/DigitalSignatureAlgorithm/blob/main/screenshots/screenshot.png)

The Digital Signature Algorithm (DSA) is a Federal Information Processing Standard for digital signatures, introduced by the U.S. National Institute of Standards and Technology (NIST) in 1991. It is based on the mathematical properties of modular exponentiation and the discrete logarithm problem, providing a mechanism to ensure the authenticity and integrity of digital messages or documents. DSA generates a pair of keys: a private key for signing and a public key for verification. Unlike encryption, its primary purpose is to verify that data has not been altered and originates from a legitimate source.

### Linux Project Setup
    sudo apt update
    sudo apt install openjdk-23-jdk
    sudo apt install maven
    mvn clean install

### Windows Project Setup
    Install OpenJDK (ver. 23) (https://www.oracle.com/java/technologies/downloads/)
    Install Maven (https://maven.apache.org/download.cgi)
    In project folder terminal: mvn clean install

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.
