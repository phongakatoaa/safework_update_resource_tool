public class SiteConfig {
    private String siteName;
    private String saveLocation;

    public SiteConfig(String siteName, String saveLocation) {
        this.siteName = siteName;
        this.saveLocation = saveLocation;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSaveLocation() {
        return saveLocation;
    }

    public void setSaveLocation(String saveLocation) {
        this.saveLocation = saveLocation;
    }
}
