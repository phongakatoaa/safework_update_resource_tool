import org.apache.poi.ss.usermodel.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Common {
    public static final Common instance = new Common();
    private JFileChooser fileChooser;

    private Common() {
        this.fileChooser = new JFileChooser();
    }

    public void openFileChooser(JTextField textField, String extension, int selectionMode) {
        fileChooser.setFileSelectionMode(selectionMode);
        if (selectionMode != JFileChooser.DIRECTORIES_ONLY) {
            if (extension.length() > 0) {
                fileChooser.resetChoosableFileFilters();
                fileChooser.setFileFilter(new FileNameExtensionFilter(extension, extension));
            }
        }
        if (!textField.getText().isEmpty()) {
            fileChooser.setCurrentDirectory(new File(textField.getText()));
        }
        int confirm = fileChooser.showOpenDialog(null);
        if (confirm == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            textField.setText(file.getAbsolutePath());
        }
    }

    public void exportXML(Document document, String dir, String fileName, StringBuilder log) {
        try {
            Path dirPath = Paths.get(dir);
            if (!Files.exists(dirPath)) {
                Files.createDirectory(Paths.get(dir));
            }
            XMLOutputter output = new XMLOutputter();
            output.setFormat(Format.getPrettyFormat().setEncoding("utf-8"));
            FileWriter fileWriter = new FileWriter(dir + "\\" + fileName);
            output.output(document, fileWriter);
            if (log != null) {
                log.append("Exported resource to ").append(dir).append("\\").append(fileName).append("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            if (log != null) {
                log.append("Error: failed to write xml file - ").append(dir).append("\\").append(fileName).append(": ")
                        .append(e.getMessage()).append("\n");
            }
        }
    }

    public void showLogs(HashMap<String, String> logs) {
        JTabbedPane tabbedPane = new JTabbedPane();
        logs.keySet().forEach(k -> {
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setText(logs.get(k));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(1200, 600));
            tabbedPane.addTab(k, null, scrollPane, "");
        });

        JOptionPane.showMessageDialog(null, tabbedPane,
                "Log entries", JOptionPane.PLAIN_MESSAGE);
    }

    private Workbook parseXLSX(String path, StringBuilder log) {
        try {
            InputStream inputStream = new FileInputStream(path);
            return WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            if (log != null) {
                log.append("Error: failed to read xlsx file - ").append(path).append(": ")
                        .append(e.getMessage()).append("\n");
            }
            return null;
        }
    }

    private Document parseXML(String path, StringBuilder log) {
        try {
            File file = new File(path);
            SAXBuilder saxBuilder = new SAXBuilder();
            return saxBuilder.build(file);
        } catch (Exception e) {
            if (log != null) {
                log.append("Error: failed to read xml file - ").append(path).append(": ")
                        .append(e.getMessage()).append("\n");
            }
            return null;
        }
    }

    private HashMap<String, String> parseCSV(String path, int keyCol, int valueCol, StringBuilder log) {
        try {
            int minNumberOfCol = Math.max(keyCol, valueCol);
            HashMap<String, String> res = new HashMap<>();
            Scanner scanner = new Scanner(new File(path));
            scanner.nextLine(); //skip header row
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                String[] splits = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (splits.length > minNumberOfCol) {
                    res.put(removeQuote(splits[keyCol]).trim(), removeQuote(splits[valueCol]).trim());
                }
            }
            scanner.close();
            return res;
        } catch (Exception e) {
            if (log != null) {
                log.append("Error: failed to read csv file - ").append(path).append(": ")
                        .append(e.getMessage()).append("\n");
            }
            return null;
        }
    }

    public void updateLanguageResources(Config config) {
        HashMap<String, String> logs = new HashMap<>();
        String csvPath = config.getLastLanguageCSVLocation();
        String resourceDir = config.getLastResourceLocation();
        config.getLanguageConfigs().forEach(lc -> {
            StringBuilder logStr = new StringBuilder();
            String fileName = config.getLanguageResourceFileName()
                    .replace("${lang}", lc.getLanguage())
                    .replace("..", "."); // for default case
            String resourcePath = resourceDir + "\\" + fileName;

            HashMap<String, String> dictionary = parseCSV(csvPath, 0, lc.getValueColIndex(), logStr);
            Document resource = parseXML(resourcePath, logStr);
            if (resource == null || dictionary == null) {
                return;
            }

            int entry = 0;
            for (String key : dictionary.keySet()) {
                Element valueNode = getLanguageValueNode(resource, key);
                String newValue = dictionary.get(key);
                if (valueNode == null || newValue == null) {
                    logStr.append("ERROR: key name {").append(key).append("} not found\n");
                    Element newDataNode = new Element("data");
                    newDataNode.setAttribute("name", key);
                    newDataNode.setAttribute("space", "preserve",
                            Namespace.XML_NAMESPACE);

                    Element newValueNode = new Element("value");
                    newValueNode.setText(newValue);

                    newDataNode.addContent(newValueNode);
                    resource.getRootElement().addContent(newDataNode);
                    logStr.append("TASK: added new key ").append(key).append("=\"").append(newValue).append("\"\n");
                    continue;
                }
                if (valueNode.getText().equals(newValue)) {
                    continue;
                }
                valueNode.setText(newValue);

                entry++;
                logStr.append(entry).append("/").append("Replaced ").append(key).append(": ").
                        append(valueNode.getText()).append(" -> ").
                        append(newValue).append("\n");
            }

            logStr.append("Replaced ").append(entry).append(" entries.\n");
            exportXML(resource, resourceDir, fileName, logStr);
            logs.put(lc.getLanguage().isEmpty() ? "Default" : lc.getLanguage(), logStr.toString());
        });
        showLogs(logs);
    }

    public void generateLanguageResourcesForSites(Config config) {
        String resourceDir = config.getLastResourceLocation();
        String resourceFileNameTemplate = config.getLanguageResourceFileName();
        StringBuilder xlsxLog = new StringBuilder();
        Workbook workbook = parseXLSX(config.getLastSiteLanguageXLSXLocation(), xlsxLog);
        if (workbook == null) {
            JOptionPane.showMessageDialog(null, xlsxLog.toString(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        HashMap<String, String> logs = new HashMap<>();
        config.getSiteConfigs().forEach(sc -> {
            StringBuilder logStr = new StringBuilder();
            Sheet sheet = workbook.getSheet(sc.getSiteName());
            if (sheet == null) {
                logStr.append("Error: no sheet found for site ").append(sc.getSiteName());
            } else {
                config.getLanguageConfigs().forEach(lc -> {
                    int entry = 0;
                    logStr.append("#==========")
                            .append(lc.getLanguage().isEmpty() ? "Default" : lc.getLanguage())
                            .append("==========#\n");
                    String resourceFileName = resourceFileNameTemplate
                            .replace("${lang}", lc.getLanguage())
                            .replace("..", ".");  // for default case
                    Document resource = parseXML(resourceDir + "\\" + resourceFileName, logStr);
                    if (resource == null) {
                        return;
                    }
                    int colIndex = lc.getValueColIndex();
                    Iterator<Row> rowIter = sheet.rowIterator();
                    while (rowIter.hasNext()) {
                        Row row = rowIter.next();
                        Cell cell = row.getCell(0);
                        if (cell == null) {
                            continue;
                        }
                        String key = row.getCell(0).getRichStringCellValue().getString();
                        cell = row.getCell(colIndex);
                        if (cell == null) {
                            continue;
                        }
                        String newValue = cell.getRichStringCellValue().getString();
                        if (key.isEmpty() || newValue.isEmpty()) {
                            continue;
                        }
                        Element valueNode = getLanguageValueNode(resource, key);
                        if (valueNode == null) {
                            logStr.append("Error: key name {").append(key).append("} not found\n");
                            continue;
                        }
                        if (valueNode.getText().equals(newValue)) {
                            continue;
                        }
                        valueNode.setText(newValue);

                        entry++;
                        logStr.append(entry).append("/").append("Replaced ").append(key).append(": ").
                                append(valueNode.getText()).append(" -> ").
                                append(newValue).append("\n");
                    }
                    logStr.append("Replaced ").append(entry).append(" entries.\n");
                    exportXML(resource, sc.getSaveLocation(), resourceFileName, logStr);
                });
            }
            logs.put(sc.getSiteName(), logStr.toString());
        });
        try {
            workbook.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "failed to close xlsx resource: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        showLogs(logs);
    }

    private Element getLanguageValueNode(Document document, String key) {
        Element root = document.getRootElement();
        for (Element child : root.getChildren("data")) {
            if (child.getAttributeValue("name").toLowerCase().equals(key.toLowerCase())) {
                return child.getChild("value");
            }
        }
        return null;
    }

    private String removeQuote(String string) {
        if (string.charAt(0) == '\"') {
            string = string.substring(1);
        }
        if (string.charAt(string.length() - 1) == '\"') {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }
}
