package loadgen;

/**
 * @author sibmaks
 */
public enum GatlingScenario {
    TEXT_A("text.json", "text.a.json"),
    TEXT_B("text.json", "text.b.json"),

    TABLE_TEXT_A("table_text.json", "table_text.a.json"),
    TABLE_TEXT_B("table_text.json", "table_text.b.json"),

    ;

    final String templateFilePath;
    final String inputFilePath;


    GatlingScenario(String templateFilePath, String inputFilePath) {
        this.templateFilePath = templateFilePath;
        this.inputFilePath = inputFilePath;
    }
}
