package resource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class User {

    private final String email;
    private final String name;
    private final String phone;
    private final String password;
    private final String salt;
    private final Role role;
    private final boolean verified;


    public enum Role {
        admin,
        manager,
        secretary,
        customer
    }

    public User(String email, String name, String phone, String password, String salt, Role role, boolean verified) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.salt = salt;
        this.role = role;
        this.verified = verified;
    }

    public User(String email, String name, String phone, String password, Role role) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.salt = generateSalt();
        this.password = hashPassword(password,this.salt);
        this.verified = false;
    }

    public User(String email, String name, String phone, Role role) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = "";
        this.salt = "";
        this.role = role;
        this.verified = false;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", role=" + role +
                '}';
    }

    public static String hashPassword(String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            md.update(salt.getBytes());

            byte[] bytes = md.digest(passwordToHash.getBytes());

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }

            // Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return generatedPassword;
    }

    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();

        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        return salt.toString();
    }
}
