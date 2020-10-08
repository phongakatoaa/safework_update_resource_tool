import javax.swing.*;

public abstract class MyTabbedPane extends JPanel {
    protected Config config;

    public MyTabbedPane(Config config) {
        this.config = config;
        init();
    }

    protected abstract void init();
}
