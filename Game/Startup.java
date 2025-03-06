package Game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Startup {
        public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Basic Shooting Game with Enemies");
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            gamePanel.startGame();
            gamePanel.requestFocusInWindow();
        });
    }
    
}
