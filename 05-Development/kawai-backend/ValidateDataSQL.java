import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class ValidateDataSQL {
    static List<String> issues = new ArrayList<>();
    static Set<String> emails = new HashSet<>();

    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/data.sql")), "UTF-8");
        String[] lines = content.split("\\r?\\n");

        Pattern cccdRaw = Pattern.compile("'(CCCD[^']+)'");
        Pattern phoneShort = Pattern.compile("'(0\\d{4,8})'");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            int lineNum = i + 1;

            // Check 1: Unencrypted CCCD
            Matcher cccdM = cccdRaw.matcher(line);
            if (cccdM.find()) {
                issues.add("LINE " + lineNum + " [CCCD_PLAIN] " + cccdM.group(1));
            }

            // Check 2: Phone number too short
            Matcher ps = phoneShort.matcher(line);
            while (ps.find()) {
                String phone = ps.group(1);
                if (phone.length() < 10) {
                    issues.add("LINE " + lineNum + " [PHONE_SHORT:" + phone.length() + "] " + phone);
                }
            }

            // Check 3: Duplicate emails
            Pattern emailPat = Pattern.compile("'([a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,})'");
            Matcher emailM = emailPat.matcher(line);
            while (emailM.find()) {
                String email = emailM.group(1).toLowerCase();
                if (emails.contains(email)) {
                    issues.add("LINE " + lineNum + " [EMAIL_DUPE] " + email);
                }
                emails.add(email);
            }

            // Check 4: Invalid status
            for (String bad : new String[]{"'PENDING'", "'ACTIVE'", "'CANCEL'"}) {
                if (line.contains(bad)) {
                    issues.add("LINE " + lineNum + " [STATUS_WARN] " + bad);
                }
            }
        }

        System.out.println("=== VALIDATE data.sql ===");
        System.out.println("Total issues: " + issues.size());
        System.out.println("Unique emails: " + emails.size());
        System.out.println("--- ISSUES ---");
        if (issues.isEmpty()) {
            System.out.println("PASS - No issues found");
        } else {
            for (String issue : issues) {
                System.out.println(issue);
            }
        }
    }
}
