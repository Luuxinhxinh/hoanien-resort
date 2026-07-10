import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class CheckDuplicates {
    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/data.sql")));
        Matcher m = Pattern.compile("(?i)INSERT INTO Accounts \\(.*?\\) VALUES\\s*([\\s\\S]*?);").matcher(content);
        if (m.find()) {
            String values = m.group(1);
            Set<String> seen = new HashSet<>();
            Matcher m2 = Pattern.compile("\\(\\s*(\\d+)\\s*,").matcher(values);
            while(m2.find()) {
                String id = m2.group(1);
                if (!seen.add(id)) {
                    System.out.println("DUPLICATE ID: " + id);
                }
            }
            System.out.println("Done checking IDs.");
        }
    }
}
