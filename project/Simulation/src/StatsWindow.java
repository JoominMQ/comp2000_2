import java.awt.*;
import javax.swing.*;

public class StatsWindow extends JFrame {

    private final JLabel normalCarLabel = new JLabel();
    private final JLabel recklessCarLabel = new JLabel();
    private final JLabel pedestrianLabel = new JLabel();

    public StatsWindow() {
        super("Live Stats");

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Font font = new Font("Arial", Font.BOLD, 22);
        for (JLabel label : new JLabel[]{normalCarLabel, recklessCarLabel, pedestrianLabel}) {
            label.setFont(font);
            panel.add(label);
        }

        add(panel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 이 창을 닫아도 프로그램은 종료되지 않음
        setSize(300, 200);
        setLocation(850, 100); // 메인 창(800x800) 옆
        update(0, 0, 0);
    }

    public void update(long normalCars, long recklessCars, long pedestrians) {
        normalCarLabel.setText("Normal cars: " + normalCars);
        recklessCarLabel.setText("Reckless cars: " + recklessCars);
        pedestrianLabel.setText("Pedestrians: " + pedestrians);
    }
}