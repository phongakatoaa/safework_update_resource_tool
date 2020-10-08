public class InitConfig {
    public static void main(String[] args) {
        Config config = new Config();
        config.getLanguageConfigs().add(new LanguageConfig("", 1));
        config.getLanguageConfigs().add(new LanguageConfig("en-ES", 2));
        config.getLanguageConfigs().add(new LanguageConfig("fr-FR", 3));
        config.getLanguageConfigs().add(new LanguageConfig("pa-IN", 4));
        config.getLanguageConfigs().add(new LanguageConfig("vi-VN", 5));
        config.getSiteConfigs().add(new SiteConfig("gatellc", ""));
        config.getSiteConfigs().add(new SiteConfig("berrybros", ""));

        config.save();
    }
}
