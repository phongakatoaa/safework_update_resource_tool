import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Tool {
    private static JFileChooser fileChooser = new JFileChooser();
    private static Config config = new Config();

    public static void main(String[] args) throws ClassNotFoundException,
            UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final JFrame frame = new JFrame();
        frame.setTitle("SW RESOURCES UPDATE TOOL");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(600, 600));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                config.save();
            }
        });

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JPanel updateLanguageResourcesPane = new UpdateLanguageResourcesPane(config);
        JPanel updateSiteLanguageResourcesPane = new UpdateSiteLanguageResourcesPane(config);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Update multi-language resources", null, updateLanguageResourcesPane, "");
        tabbedPane.addTab("Update sites language resources", null, updateSiteLanguageResourcesPane, "");

        frame.add(tabbedPane);
        frame.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2,
                dim.height / 2 - frame.getSize().height / 2);
    }
}
