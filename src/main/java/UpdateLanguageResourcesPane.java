import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UpdateLanguageResourcesPane extends MyTabbedPane {
    public UpdateLanguageResourcesPane(Config config) {
        super(config);
    }

    @Override
    protected void init() {
        this.setLayout(new GridBagLayout());

        JTextField csvPath = new JTextField();
        csvPath.setEditable(false);
        csvPath.setBackground(Color.WHITE);
        csvPath.setText(config.getLastLanguageCSVLocation());

        JButton csvButton = new JButton("Choose CSV File");
        csvButton.addActionListener(e -> Common.instance.openFileChooser(csvPath, "csv",
                JFileChooser.FILES_ONLY));

        JPanel csvRow = new JPanel();
        csvRow.setLayout(new BorderLayout());
        csvRow.add(csvPath, BorderLayout.CENTER);
        csvRow.add(csvButton, BorderLayout.EAST);

        JTextField resourceDirectory = new JTextField();
        resourceDirectory.setEditable(false);
        resourceDirectory.setBackground(Color.WHITE);
        resourceDirectory.setText(config.getLastResourceLocation());

        JButton resourceButton = new JButton("Choose Resource Directory");
        resourceButton.addActionListener(e -> Common.instance.openFileChooser(resourceDirectory, "",
                JFileChooser.DIRECTORIES_ONLY));

        JPanel resourceRow = new JPanel();
        resourceRow.setLayout(new BorderLayout());
        resourceRow.add(resourceDirectory, BorderLayout.CENTER);
        resourceRow.add(resourceButton, BorderLayout.EAST);

        final String[] columns = new String[]{"Language", "CSV Value Col", ""};
        DefaultTableModel tableModel = new DefaultTableModel();
        for (String column : columns) {
            tableModel.addColumn(column);
        }
        config.getLanguageConfigs().forEach(lc -> tableModel.addRow(new Object[]{
                lc.getLanguage().isEmpty() ? "(Default)" : lc.getLanguage(),
                lc.getValueColIndex(),
                "Edit"}));

        JTable languageConfigTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        languageConfigTable.setRowHeight(40);
        languageConfigTable.setFont(new Font("Arial", Font.PLAIN, 12));
        languageConfigTable.setModel(tableModel);
        languageConfigTable.setCellSelectionEnabled(true);

        DefaultTableCellRenderer headerCellRenderer = (DefaultTableCellRenderer) languageConfigTable.getTableHeader().getDefaultRenderer();
        headerCellRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerCellRenderer = new DefaultTableCellRenderer();
        centerCellRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer editCellRenderer = new DefaultTableCellRenderer();
        editCellRenderer.setHorizontalAlignment(JLabel.CENTER);
        editCellRenderer.setBackground(Color.darkGray);
        editCellRenderer.setForeground(Color.WHITE);
        editCellRenderer.setFont(new Font("Arial", Font.BOLD, 12));

        languageConfigTable.getColumnModel().getColumn(1).setCellRenderer(centerCellRenderer);
        languageConfigTable.getColumnModel().getColumn(2).setCellRenderer(editCellRenderer);

        languageConfigTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = languageConfigTable.columnAtPoint(e.getPoint());
                int row = languageConfigTable.rowAtPoint(e.getPoint());
                if (col == 2) {
                    JTextField textField = new JTextField();
                    textField.setText(languageConfigTable.getValueAt(row, 2).toString());
                    int option = JOptionPane.showConfirmDialog(null, textField, "Set CSV Value Column Index",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        int value = Integer.parseInt(textField.getText());
                        String language = languageConfigTable.getValueAt(row, 1).toString();
                        for (LanguageConfig lc : config.getLanguageConfigs()) {
                            if (lc.getLanguage().equals(language)) {
                                lc.setValueColIndex(value);
                                break;
                            }
                        }
                        languageConfigTable.setValueAt(value, row, 2);
                    }
                }
            }
        });
        languageConfigTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = languageConfigTable.columnAtPoint(e.getPoint());
                languageConfigTable.setCursor(Cursor.getPredefinedCursor(col == 3 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane scrollPane = new JScrollPane(languageConfigTable);

        JPanel languageRow = new JPanel();
        languageRow.setLayout(new BorderLayout());
        languageRow.add(scrollPane, BorderLayout.CENTER);

        JButton executeButton = new JButton("Execute update for selected languages");
        executeButton.addActionListener(e -> execute(csvPath.getText(), resourceDirectory.getText()));

        JPanel executeRow = new JPanel();
        executeRow.setLayout(new BorderLayout());
        executeRow.add(executeButton, BorderLayout.CENTER);

        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.insets = new Insets(10, 10, 10, 10);
        this.add(csvRow, gbConstraints);
        gbConstraints.gridy++;
        this.add(resourceRow, gbConstraints);
        gbConstraints.gridy++;
        gbConstraints.weightx = 1.0;
        gbConstraints.weighty = 1.0;
        gbConstraints.fill = GridBagConstraints.BOTH;
        this.add(languageRow, gbConstraints);
        gbConstraints.gridy++;
        gbConstraints.weightx = 0;
        gbConstraints.weighty = 0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        this.add(executeRow, gbConstraints);
    }

    private void execute(String csvPath, String resourceDir) {
        if (readyToExecute(csvPath, resourceDir)) {
            config.setLastLanguageCSVLocation(csvPath);
            config.setLastResourceLocation(resourceDir);
            Common.instance.updateLanguageResources(config);
        }
    }

    private boolean readyToExecute(String csvPath, String resourceDir) {
        String err = "";
        if (csvPath.isEmpty()) {
            err = "Please choose CSV path";
        } else if (resourceDir.isEmpty()) {
            err = "Please choose resource directory";
        } else {
            for (LanguageConfig lc : config.getLanguageConfigs()) {
                if (lc.getValueColIndex() <= 0) {
                    err = "Please choose a valid value column index for " + lc.getLanguage() + "language";
                    break;
                }
            }
        }
        if (!err.isEmpty()) {
            JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
