public class LanguageConfig {
    private String language;
    private int valueColIndex;

    public LanguageConfig(String language, int valueColIndex) {
        this.language = language;
        this.valueColIndex = valueColIndex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getValueColIndex() {
        return valueColIndex;
    }

    public void setValueColIndex(int valueColIndex) {
        this.valueColIndex = valueColIndex;
    }
}
