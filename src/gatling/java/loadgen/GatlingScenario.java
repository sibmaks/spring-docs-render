package loadgen;

import io.gatling.javaapi.core.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * @author sibmaks
 */
public enum GatlingScenario {
    TEXT_SHAKESPEAR("text.jrxml", it -> sendText("text.shakespeare.txt")),
    TEXT_SHAKESPEAR_2("text.jrxml", it -> sendText("text.shakespeare.2.txt")),

    TEXT_SHAKESPEAR_USER_10("table_text_bar.jrxml", it -> sendTextAndUsers("text.shakespeare.txt", 10)),
    TEXT_SHAKESPEAR_USER_50("table_text_bar.jrxml", it -> sendTextAndUsers("text.shakespeare.txt", 50)),
    TEXT_SHAKESPEAR_USER_100("table_text_bar.jrxml", it -> sendTextAndUsers("text.shakespeare.txt", 100)),

    TEXT_SHAKESPEAR_2_USER_10("table_text_bar.jrxml", it -> sendTextAndUsers("text.shakespeare.2.txt", 10)),
    TEXT_SHAKESPEAR_2_USER_50("table_text_bar.jrxml", it -> sendTextAndUsers("text.shakespeare.2.txt", 50)),
    TEXT_SHAKESPEAR_2_USER_100("table_text_bar.jrxml", it -> sendTextAndUsers("text.shakespeare.2.txt", 100)),

    ;

    final String template;
    final Function<Session, Map<String, Object>> modelGenerator;

    GatlingScenario(
            String templateFilePath,
            Function<Session, Map<String, Object>> modelGenerator
    ) {
        this.modelGenerator = modelGenerator;
        try (var templateStream = GatlingScenario.class.getResourceAsStream("/data/templates/" + templateFilePath)) {
            var template = templateStream.readAllBytes();
            var base64Encoder = Base64.getEncoder();
            this.template = base64Encoder.encodeToString(template);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> sendText(String textPath) {
        try {
            var text = getText(textPath);

            return Map.ofEntries(
                    Map.entry(
                            "model",
                            Map.ofEntries(
                                    Map.entry("key", text)
                            )
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getText(String textPath) throws IOException {
        try (var contentStream = GatlingScenario.class.getResourceAsStream("/data/input/" + textPath)) {
            var binaryContent = contentStream.readAllBytes();
            var charsContent = new String(binaryContent).toCharArray();
            var chars = new ArrayList<Character>(charsContent.length);
            for (char c : charsContent) {
                chars.add(c);
            }
            Collections.shuffle(chars);
            var content = new StringBuilder();
            for (var c : chars) {
                content.append(c);
            }

            var encoder = Base64.getEncoder();
            return encoder.encodeToString(content.toString().getBytes());
        }
    }

    private static Map<String, Object> sendTextAndUsers(String textPath, int userCount) {
        try {
            var text = getText(textPath);

            return Map.ofEntries(
                    Map.entry(
                            "model",
                            Map.ofEntries(
                                    Map.entry("key", text),
                                    Map.entry("barcode", String.valueOf(text.hashCode())),
                                    Map.entry("users", UserGenerator.generate(userCount))
                            )
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
