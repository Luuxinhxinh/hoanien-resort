import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixDataSQL {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY = "KawaiResortSecretKeyForAES256Enc";

    public static String encrypt(String plainText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static void main(String[] args) throws Exception {
        String path = "src/main/resources/data.sql";
        String content = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");

        // Fix phone
        Pattern phonePattern = Pattern.compile("'090(\\d{3})'");
        Matcher m = phonePattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String num = m.group(1);
            m.appendReplacement(sb, "'0900000" + num + "'");
        }
        m.appendTail(sb);
        content = sb.toString();

        // Fix CCCD
        Pattern cccdPattern = Pattern.compile("'(CCCD[^']+)'");
        Matcher cm = cccdPattern.matcher(content);
        StringBuffer csb = new StringBuffer();
        while (cm.find()) {
            String original = cm.group(1);
            String numPart = original.replaceAll("\\D", "");
            if (numPart.isEmpty()) numPart = "0";
            while (numPart.length() < 6) {
                numPart = "0" + numPart;
            }
            if (numPart.length() > 6) {
                numPart = numPart.substring(0, 6);
            }
            String cccdPlain = "001099" + numPart;
            String encrypted = encrypt(cccdPlain);
            cm.appendReplacement(csb, "'" + encrypted + "'");
        }
        cm.appendTail(csb);
        content = csb.toString();

        Files.write(Paths.get(path), content.getBytes("UTF-8"));
        System.out.println("Fixed data.sql successfully!");
    }
}
