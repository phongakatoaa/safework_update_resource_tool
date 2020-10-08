import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UpdateSiteLanguageResourcesPane extends MyTabbedPane {
    public UpdateSiteLanguageResourcesPane(Config config) {
        super(config);
    }

    @Override
    protected void init() {
        this.setLayout(new GridBagLayout());

        JTextField xlsxPath = new JTextField();
        xlsxPath.setEditable(false);
        xlsxPath.setBackground(Color.WHITE);
        xlsxPath.setText(config.getLastSiteLanguageXLSXLocation());

        JButton xlsxButton = new JButton("Choose XLSX File");
        xlsxButton.addActionListener(e -> Common.instance.openFileChooser(xlsxPath, "xlsx",
                JFileChooser.FILES_ONLY));

        JPanel xlsx = new JPanel();
        xlsx.setLayout(new BorderLayout());
        xlsx.add(xlsxPath, BorderLayout.CENTER);
        xlsx.add(xlsxButton, BorderLayout.EAST);

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

        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.insets = new Insets(10, 10, 10, 10);

        final String[] columns = new String[]{"Site", "Save Location", ""};
        DefaultTableModel tableModel = new DefaultTableModel();
        for (String column : columns) {
            tableModel.addColumn(column);
        }
        config.getSiteConfigs().forEach(sc -> tableModel.addRow(new Object[]{
                sc.getSiteName(),
                sc.getSaveLocation(),
                "Edit"
        }));

        JTable siteConfigTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        siteConfigTable.setRowHeight(40);
        siteConfigTable.setFont(new Font("Arial", Font.PLAIN, 12));
        siteConfigTable.setModel(tableModel);
        siteConfigTable.setCellSelectionEnabled(true);

        DefaultTableCellRenderer headerCellRenderer = (DefaultTableCellRenderer) siteConfigTable.getTableHeader().getDefaultRenderer();
        headerCellRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerCellRenderer = new DefaultTableCellRenderer();
        centerCellRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer editCellRenderer = new DefaultTableCellRenderer();
        editCellRenderer.setHorizontalAlignment(JLabel.CENTER);
        editCellRenderer.setBackground(Color.darkGray);
        editCellRenderer.setForeground(Color.WHITE);
        editCellRenderer.setFont(new Font("Arial", Font.BOLD, 12));

        siteConfigTable.getColumnModel().getColumn(1).setCellRenderer(centerCellRenderer);
        siteConfigTable.getColumnModel().getColumn(2).setCellRenderer(editCellRenderer);

        siteConfigTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = siteConfigTable.columnAtPoint(e.getPoint());
                int row = siteConfigTable.rowAtPoint(e.getPoint());
                if (col == 2) {
                    JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
                    JTextField siteNameTextField = new JTextField();
                    siteNameTextField.setText(siteConfigTable.getValueAt(row, 0).toString());
                    JTextField saveLocationTextField = new JTextField();
                    saveLocationTextField.setText(siteConfigTable.getValueAt(row, 1).toString());
                    JButton saveLocationButton = new JButton("Choose Save Location");
                    saveLocationButton.addActionListener(ev -> Common.instance.openFileChooser(saveLocationTextField,
                            "", JFileChooser.DIRECTORIES_ONLY));

                    panel.add(siteNameTextField);
                    panel.add(saveLocationTextField);
                    panel.add(saveLocationButton);

                    int option = JOptionPane.showConfirmDialog(null, panel, "Set CSV Value Column Index",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        String newSiteName = siteNameTextField.getText();
                        String saveLocation = saveLocationTextField.getText();
                        String oldSiteName = siteConfigTable.getValueAt(row, 0).toString();
                        for (SiteConfig sc : config.getSiteConfigs()) {
                            if (sc.getSiteName().equals(oldSiteName)) {
                                sc.setSaveLocation(saveLocation);
                                sc.setSiteName(newSiteName);
                                break;
                            }
                        }
                        siteConfigTable.setValueAt(newSiteName, row, 0);
                        siteConfigTable.setValueAt(saveLocation, row, 1);
                    }
                }
            }
        });
        siteConfigTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = siteConfigTable.columnAtPoint(e.getPoint());
                siteConfigTable.setCursor(Cursor.getPredefinedCursor(col == 4 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane scrollPane = new JScrollPane(siteConfigTable);

        JPanel siteRow = new JPanel();
        siteRow.setLayout(new BorderLayout());
        siteRow.add(scrollPane, BorderLayout.CENTER);

        JButton newSiteButton = new JButton("Add Site");
        newSiteButton.addActionListener(e -> {
            JTextField textField = new JTextField();
            int confirm = JOptionPane.showConfirmDialog(null, textField, "Enter Site Name",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.addRow(new Object[]{false, textField.getText(), 0, "", "Edit"});
                config.getSiteConfigs().add(new SiteConfig(textField.getText(), ""));
            }
        });

        JPanel newSiteRow = new JPanel(new BorderLayout());
        newSiteRow.add(newSiteButton, BorderLayout.EAST);

        JButton executeButton = new JButton("Execute update for selected sites");
        executeButton.addActionListener(e -> execute(xlsxPath.getText(), resourceDirectory.getText()));

        JPanel executeRow = new JPanel();
        executeRow.setLayout(new BorderLayout());
        executeRow.add(executeButton, BorderLayout.CENTER);

        this.add(xlsx, gbConstraints);
        gbConstraints.gridy++;
        this.add(resourceRow, gbConstraints);
        gbConstraints.gridy++;
        this.add(newSiteRow, gbConstraints);
        gbConstraints.gridy++;
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.weightx = 1;
        gbConstraints.weighty = 1;
        this.add(siteRow, gbConstraints);
        gbConstraints.gridy++;
        gbConstraints.weightx = 0;
        gbConstraints.weighty = 0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        this.add(executeRow, gbConstraints);
    }

    private void execute(String xlsxPath, String resourceDir) {
        if (readyToExecute(xlsxPath, resourceDir)) {
            config.setLastSiteLanguageXLSXLocation(xlsxPath);
            config.setLastResourceLocation(resourceDir);
            Common.instance.generateLanguageResourcesForSites(config);
        }
    }

    private boolean readyToExecute(String xlsxPath, String resourceDir) {
        String err = "";
        if (xlsxPath.isEmpty()) {
            err = "Please choose XLSX path";
        } else if (resourceDir.isEmpty()) {
            err = "Please choose resource directory";
        } else {
            for (SiteConfig sc : config.getSiteConfigs()) {
                if (sc.getSiteName().isEmpty()) {
                    err = "Please specify a valid site name";
                    break;
                } else if (sc.getSaveLocation().isEmpty()) {
                    err = "Please specify a save location for site " + sc.getSiteName();
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
