import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final String CONFIG_FILE = "config.xml";
    private static final String DEFAULT_LANGUAGE_RESOURCE_FILENAME = "safework.language.${lang}.resx";

    private List<LanguageConfig> languageConfigs;
    private String languageResourceFileName;
    private List<SiteConfig> siteConfigs;
    private String lastResourceLocation;
    private String lastLanguageCSVLocation;
    private String lastSiteLanguageXLSXLocation;
    private String appDirectory;

    public Config() {
        languageConfigs = new ArrayList<>();
        siteConfigs = new ArrayList<>();
        languageResourceFileName = DEFAULT_LANGUAGE_RESOURCE_FILENAME;
        lastResourceLocation = "";
        lastLanguageCSVLocation = "";
        lastSiteLanguageXLSXLocation = "";
        try {
            appDirectory = new File(Tool.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
                    .getParentFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(null, "failed to get config path: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            appDirectory = "";
        }
        System.out.println("CONFIG_PATH=" + appDirectory + "\\" + CONFIG_FILE);
        load();
    }

    public void load() {
        try {
            InputStream inputStream = new FileInputStream(appDirectory + "\\" + CONFIG_FILE);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document configDoc = saxBuilder.build(inputStream);

            Element el_root = configDoc.getRootElement();

            languageResourceFileName = el_root.getChild("languageResourceFileName").getText();
            lastResourceLocation = el_root.getChild("lastResourceLocation").getText();
            lastLanguageCSVLocation = el_root.getChild("lastLanguageCSVLocation").getText();
            lastSiteLanguageXLSXLocation = el_root.getChild("lastSiteLanguageXLSXLocation").getText();

            Element el_languageConfigs = el_root.getChild("languageConfigs");
            el_languageConfigs.getChildren().forEach(languageNode -> {
                LanguageConfig lc = new LanguageConfig(
                        languageNode.getChild("language").getText(),
                        Integer.parseInt(languageNode.getChild("valueColIndex").getText())
                );
                languageConfigs.add(lc);
            });

            Element el_siteConfigs = el_root.getChild("siteConfigs");
            el_siteConfigs.getChildren().forEach(siteNode -> {
                SiteConfig sc = new SiteConfig(
                        siteNode.getChild("site").getText(),
                        siteNode.getChild("saveLocation").getText());
                siteConfigs.add(sc);
            });
        } catch (IOException | JDOMException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading config: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void save() {
        try {
            Element el_root = new Element("config");
            Document configDoc = new Document(el_root);

            Element el_languageConfigs = new Element("languageConfigs");
            languageConfigs.forEach(lc -> {
                Element el_lc = new Element("languageConfig");
                Element el_language = new Element("language");
                el_language.setText(lc.getLanguage());
                Element el_valueColIndex = new Element("valueColIndex");
                el_valueColIndex.setText(Integer.toString(lc.getValueColIndex()));
                el_lc.addContent(el_language);
                el_lc.addContent(el_valueColIndex);

                el_languageConfigs.addContent(el_lc);
            });

            Element el_languageResourceFileName = new Element("languageResourceFileName");
            el_languageResourceFileName.setText(languageResourceFileName);

            Element el_lastResourceLocation = new Element("lastResourceLocation");
            el_lastResourceLocation.setText(lastResourceLocation);

            Element el_lastLanguageCSVLocation = new Element("lastLanguageCSVLocation");
            el_lastLanguageCSVLocation.setText(lastLanguageCSVLocation);

            Element el_lastSiteLanguageXLSXLocation = new Element("lastSiteLanguageXLSXLocation");
            el_lastSiteLanguageXLSXLocation.setText(lastSiteLanguageXLSXLocation);

            Element el_siteConfigs = new Element("siteConfigs");
            siteConfigs.forEach(sc -> {
                Element el_sc = new Element("siteConfig");
                Element el_siteName = new Element("site");
                el_siteName.setText(sc.getSiteName());
                Element el_saveLocation = new Element("saveLocation");
                el_saveLocation.setText(sc.getSaveLocation());

                el_sc.addContent(el_siteName);
                el_sc.addContent(el_saveLocation);

                el_siteConfigs.addContent(el_sc);
            });

            el_root.addContent(el_siteConfigs);
            el_root.addContent(el_languageConfigs);
            el_root.addContent(el_languageResourceFileName);
            el_root.addContent(el_lastResourceLocation);
            el_root.addContent(el_lastLanguageCSVLocation);
            el_root.addContent(el_lastSiteLanguageXLSXLocation);

            XMLOutputter output = new XMLOutputter();
            output.setFormat(Format.getPrettyFormat());

            FileWriter fileWriter = new FileWriter(new File(appDirectory + "\\" + CONFIG_FILE));
            output.output(configDoc, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "error saving config: " + e.getMessage());
        }
    }

    public List<LanguageConfig> getLanguageConfigs() {
        return languageConfigs;
    }

    public String getLanguageResourceFileName() {
        return languageResourceFileName;
    }

    public List<SiteConfig> getSiteConfigs() {
        return siteConfigs;
    }

    public String getLastLanguageCSVLocation() {
        return lastLanguageCSVLocation;
    }

    public void setLastLanguageCSVLocation(String lastLanguageCSVLocation) {
        this.lastLanguageCSVLocation = lastLanguageCSVLocation;
    }

    public String getLastResourceLocation() {
        return lastResourceLocation;
    }

    public void setLastResourceLocation(String lastResourceLocation) {
        this.lastResourceLocation = lastResourceLocation;
    }

    public String getLastSiteLanguageXLSXLocation() {
        return lastSiteLanguageXLSXLocation;
    }

    public void setLastSiteLanguageXLSXLocation(String lastSiteLanguageXLSXLocation) {
        this.lastSiteLanguageXLSXLocation = lastSiteLanguageXLSXLocation;
    }
}
