import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

class Bullet {
    int x, y, dx, dy;
    int speed = 5;

    public Bullet(int x, int y, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    public void move() {
        x += dx * speed;
        y += dy * speed;
    }
}

class Enemy {
    int x, y;
    int speed = 2;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveTowards(int playerX, int playerY) {
        if (x < playerX) x += speed;
        else if (x > playerX) x -= speed;
        
        if (y < playerY) y += speed;
        else if (y > playerY) y -= speed;
    }
}

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private Thread gameThread;
    private boolean running = false;
    private int playerX = 100, playerY = 100;
    private int speed = 3;
    private boolean up, down, left, right;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private Random random;
    private int health = 3;
    private long lastSpawnTime = 0;
    private boolean gameOver = false;
    private JButton resetButton;
    private int spawnRate = 1;
    private long lastIncreaseTime = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setLayout(null);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
        
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        random = new Random();
        
        resetButton = new JButton("Restart");
        resetButton.setBounds(350, 300, 100, 50);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        resetButton.setVisible(false);
        add(resetButton);
        
        repaint();
    }

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

    public void startGame() {
        if (gameThread == null || !running) {
            gameThread = new Thread(this);
            running = true;
            gameThread.start();
        }
        requestFocusInWindow();
        repaint();
    }

    @Override
    public void run() {
        while (running) {
            updateGame();
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (gameOver) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("YOU DIED!", 300, 250);
            resetButton.setVisible(true);
            return;
        }

        g2d.setColor(Color.WHITE);
        g2d.fillOval(playerX, playerY, 20, 20); // Player
        
        g2d.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            g2d.fillOval(enemy.x, enemy.y, 20, 20); // Enemies
        }
        
        g2d.setColor(Color.YELLOW);
        for (Bullet bullet : bullets) {
            g2d.fillOval(bullet.x, bullet.y, 5, 5); // Bullets
        }

        g2d.setColor(Color.GREEN);
        g2d.fillRect(10, 10, health * 40, 10); // Health bar
    }

    private void updateGame() {
        if (gameOver) return;
        
        if (up) playerY -= speed;
        if (down) playerY += speed;
        if (left) playerX -= speed;
        if (right) playerX += speed;

        for (Enemy enemy : enemies) {
            enemy.moveTowards(playerX, playerY);
        }

        for (Bullet bullet : bullets) {
            bullet.move();
        }

        bullets.removeIf(bullet -> bullet.x < 0 || bullet.x > 800 || bullet.y < 0 || bullet.y > 600);
        
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (new Rectangle(enemy.x, enemy.y, 20, 20).intersects(new Rectangle(playerX, playerY, 20, 20))) {
                health--;
                enemyIterator.remove();
                if (health <= 0) {
                    gameOver = true;
                    return;
                }
            }
            
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                if (new Rectangle(bullet.x, bullet.y, 5, 5).intersects(new Rectangle(enemy.x, enemy.y, 20, 20))) {
                    bulletIterator.remove();
                    enemyIterator.remove();
                    break;
                }
            }
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= 3000) {
            for (int i = 0; i < spawnRate; i++) {
                spawnEnemy();
            }
            lastSpawnTime = currentTime;
        }

        if (currentTime - lastIncreaseTime >= 15000) {
            spawnRate++;
            lastIncreaseTime = currentTime;
        }
    }

    private void spawnEnemy() {
        int edge = random.nextInt(4);
        int x = 0, y = 0;
        switch (edge) {
            case 0: x = 0; y = random.nextInt(600); break;
            case 1: x = 800; y = random.nextInt(600); break;
            case 2: x = random.nextInt(800); y = 0; break;
            case 3: x = random.nextInt(800); y = 600; break;
        }
        enemies.add(new Enemy(x, y));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_D) right = true;
        if (key == KeyEvent.VK_UP) bullets.add(new Bullet(playerX, playerY, 0, -5));
        if (key == KeyEvent.VK_DOWN) bullets.add(new Bullet(playerX, playerY, 0, 5));
        if (key == KeyEvent.VK_LEFT) bullets.add(new Bullet(playerX, playerY, -5, 0));
        if (key == KeyEvent.VK_RIGHT) bullets.add(new Bullet(playerX, playerY, 5, 0));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_D) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
