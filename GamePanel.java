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

    public Bullet(int x, int y, int dx, int dy, int speed) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.speed = speed;
    }

    public void move() {
        x += dx * speed;
        y += dy * speed;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 5, 5);
    }
}

class Enemy {
    int x, y;
    int speed = 2;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveTowards(int playerX, int playerY, Rectangle wallTop, Rectangle wallBottom) {
        int newX = x;
        int newY = y;

        if (x < playerX) newX += speed;
        else if (x > playerX) newX -= speed;
        
        if (y < playerY) newY += speed;
        else if (y > playerY) newY -= speed;

        Rectangle newPos = new Rectangle(newX, newY, 20, 20);

        if (!newPos.intersects(wallTop) && !newPos.intersects(wallBottom)) {
            x = newX;
            y = newY;
        } else {
            // Try moving in a different direction
            if (newPos.intersects(wallTop)) {
                if (y < wallTop.y) y -= speed;
                else y += speed;
            }
            if (newPos.intersects(wallBottom)) {
                if (y < wallBottom.y) y -= speed;
                else y += speed;
            }
        }
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
    private Rectangle wallTop, wallBottom;
    private int score = 0;
    private String currentWeapon = "Pistol";

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

        wallTop = new Rectangle(250, 250, 300, 20);
        wallBottom = new Rectangle(250, 330, 300, 20);
        
        repaint();
    }

    private void shoot() {
        int bulletSpeed;
        if (currentWeapon.equals("Pistol")) bulletSpeed = 5;
        else if (currentWeapon.equals("Shotgun")) bulletSpeed = 3;
        else bulletSpeed = 7; // SMG

        bullets.add(new Bullet(playerX, playerY, 0, -1, bulletSpeed));
    }


    public void resetGame() {
        playerX = 100;
        playerY = 100;
        health = 3;
        enemies.clear();
        bullets.clear();
        gameOver = false;
        score = 0;
        resetButton.setVisible(false);
        requestFocusInWindow();
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

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, getWidth() - 100, 20); // Move score to top-right corner
        
        if (gameOver) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("YOU DIED!", 300, 250);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("Final Score: " + score, 320, 300); // Display score on game over screen
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

        g2d.setColor(Color.GRAY);
        g2d.fillRect(wallTop.x, wallTop.y, wallTop.width, wallTop.height); // Top line of '='
        g2d.fillRect(wallBottom.x, wallBottom.y, wallBottom.width, wallBottom.height); // Bottom line of '='

        g2d.setColor(Color.GREEN);
        g2d.fillRect(10, 10, health * 40, 10); // Health bar
    }

    private void updateGame() {
        if (gameOver) return;

        int newX = playerX;
        int newY = playerY;
        
        if (up) playerY -= speed;
        if (down) playerY += speed;
        if (left) playerX -= speed;
        if (right) playerX += speed;


        if (up && !new Rectangle(playerX, playerY - speed, 20, 20).intersects(wallTop) && !new Rectangle(playerX, playerY - speed, 20, 20).intersects(wallBottom)) {
            newY -= speed;
        }
        if (down && !new Rectangle(playerX, playerY + speed, 20, 20).intersects(wallTop) && !new Rectangle(playerX, playerY + speed, 20, 20).intersects(wallBottom)) {
            newY += speed;
        }
        if (left && !new Rectangle(playerX - speed, playerY, 20, 20).intersects(wallTop) && !new Rectangle(playerX - speed, playerY, 20, 20).intersects(wallBottom)) {
            newX -= speed;
        }
        if (right && !new Rectangle(playerX + speed, playerY, 20, 20).intersects(wallTop) && !new Rectangle(playerX + speed, playerY, 20, 20).intersects(wallBottom)) {
            newX += speed;
        }

        for (Enemy enemy : enemies) {   //Zombie ai
            enemy.moveTowards(playerX, playerY, wallTop, wallBottom);
        }

        for (Bullet bullet : bullets) { //Bullets
            bullet.move();
        }

        bullets.removeIf(bullet -> bullet.getBounds().intersects(wallTop) || bullet.getBounds().intersects(wallBottom));
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
                } //Zombie dies if it bites the player

                if (enemy.x == playerX && enemy.y == playerY) {
                    score += 10;
                    enemyIterator.remove();
                }

                if ((enemy.x < wallTop.x + wallTop.width && enemy.x + 20 > wallTop.x && enemy.y < wallTop.y + wallTop.height && enemy.y + 20 > wallTop.y) ||
                (enemy.x < wallBottom.x + wallBottom.width && enemy.x + 20 > wallBottom.x && enemy.y < wallBottom.y + wallBottom.height && enemy.y + 20 > wallBottom.y)) {
                enemyIterator.remove(); // Stop enemies from passing through the walls
            }
        }
            
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                if (new Rectangle(bullet.x, bullet.y, 5, 5).intersects(new Rectangle(enemy.x, enemy.y, 20, 20))) {
                    bulletIterator.remove();
                    enemyIterator.remove();
                    score += 10; // Increase score when an enemy is killed
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
        if (key == KeyEvent.VK_1) currentWeapon = "Pistol";
        if (key == KeyEvent.VK_2) currentWeapon = "Shotgun";
        if (key == KeyEvent.VK_3) currentWeapon = "SMG";
        if (key == KeyEvent.VK_UP) bullets.add(new Bullet(playerX, playerY, 0, -5, speed));
        if (key == KeyEvent.VK_DOWN) bullets.add(new Bullet(playerX, playerY, 0, 5, speed));
        if (key == KeyEvent.VK_LEFT) bullets.add(new Bullet(playerX, playerY, -5, 0, speed));
        if (key == KeyEvent.VK_RIGHT) bullets.add(new Bullet(playerX, playerY, 5, 0, speed));
        if (key == KeyEvent.VK_SPACE) shoot();
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
