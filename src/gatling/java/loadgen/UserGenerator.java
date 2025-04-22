package loadgen;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author sibmaks
 */
public class UserGenerator {
    private static final Random random = new Random();

    public static Map<String, Object> generate() {
        return Map.ofEntries(
                Map.entry("username", UUID.randomUUID().toString()),
                Map.entry("createdAt", LocalDate.now().minusDays(random.nextInt(1, 31)).format(DateTimeFormatter.ISO_LOCAL_DATE)),
                Map.entry("blocked", random.nextBoolean())
        );
    }

    public static List<Map<String, Object>> generate(int n) {
        var users = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < n; i++) {
            users.add(generate());
        }
        return users;
    }

}
