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
    int speed = 3;

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

class Room {
    int x, y, width, height;
    ArrayList<Rectangle> walls;
    
    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.walls = new ArrayList<>();
        generateWalls();
    }
    
    private void generateWalls() {
        walls.add(new Rectangle(x, y, width, 10)); // Top wall
        walls.add(new Rectangle(x, y + height - 10, width, 10)); // Bottom wall
        walls.add(new Rectangle(x, y, 10, height)); // Left wall
        walls.add(new Rectangle(x + width - 10, y, 10, height)); // Right wall
    }
    
    public boolean contains(Rectangle playerBounds) {
        return new Rectangle(x, y, width, height).contains(playerBounds);
    }
    
    public ArrayList<Rectangle> getWalls() {
        return walls;
    }
}

class HospitalMap {
    private ArrayList<Room> rooms;
    private ArrayList<Rectangle> corridors;
    private Random random;

    public HospitalMap() {
        rooms = new ArrayList<>();
        corridors = new ArrayList<>();
        random = new Random();
        generateMap();
    }

    private void generateMap() {
        rooms.add(new Room(100, 100, 200, 150)); // Reception
        rooms.add(new Room(400, 100, 200, 150)); // ER
        rooms.add(new Room(100, 300, 200, 150)); // Hallway
        rooms.add(new Room(400, 300, 200, 150)); // Patient Rooms

        corridors.add(new Rectangle(100, 250, 500, 50)); // Horizontal corridor
        corridors.add(new Rectangle(300, 100, 100, 350)); // Vertical corridor
        
        //Doors & Windows
        corridors.add(new Rectangle(220,90,50,30)); //Top Left: Up, Right
        corridors.add(new Rectangle(130,90,50,30)); //Top Left: Up, Left
        corridors.add(new Rectangle(130,230,130,20));//Top Left: Down
        corridors.add(new Rectangle(90,140,30,90)); //Top Left: Left
    
        corridors.add(new Rectangle(130,300,130,20));//Bottom Left: Up
        corridors.add(new Rectangle(90,335,30,90)); //Bottom Left: Left
        corridors.add(new Rectangle(220,430,50,30)); //Bottom Left: Up, Right
        corridors.add(new Rectangle(130,430,50,30)); //Bottom Left: Up, Left

        corridors.add(new Rectangle(520,90,50,30)); //Top Right: Up, Right
        corridors.add(new Rectangle(430,90,50,30)); //Top Right: Up, Left
        corridors.add(new Rectangle(580,140,30,90)); //Top Right: Right
        corridors.add(new Rectangle(390,140,30,90)); //Top Right: Left
        corridors.add(new Rectangle(430,230,130,20));//Top Right: Down

        corridors.add(new Rectangle(580,335,30,90)); //Bottom Right: Right
        corridors.add(new Rectangle(390,335,30,90)); //Bottom Right: Left
    
    }

    public void draw(Graphics g) {
        g.setColor(Color.GRAY);
        for (Room room : rooms) {
            g.fillRect(room.x, room.y, room.width, room.height);
        }
        g.setColor(Color.DARK_GRAY);
        for (Rectangle corridor : corridors) {
            g.fillRect(corridor.x, corridor.y, corridor.width, corridor.height);
        }
        g.setColor(Color.WHITE);
        for (Room room : rooms) {
            for (Rectangle wall : room.getWalls()) {
                g.fillRect(wall.x, wall.y, wall.width, wall.height);
            }
    }
    
}

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public ArrayList<Rectangle> getCorridors() {
        return corridors;
    }
}

class Enemy {
    int x, y, speed = 1;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveTowards(int playerX, int playerY, ArrayList<Room> rooms, ArrayList<Rectangle> corridors) {
        int newX = x;
        int newY = y;

        if (x < playerX) newX += speed;
        else if (x > playerX) newX -= speed;
        
        if (y < playerY) newY += speed;
        else if (y > playerY) newY -= speed;

        Rectangle newPos = new Rectangle(newX, newY, 20, 20);

        boolean collision = false;
        for (Room room : rooms) {
            for (Rectangle wall : room.getWalls()) {
                if (wall.intersects(newPos)) {
                    collision = true;
                    break;
                }
            }
        }
        
        for (Rectangle corridor : corridors) {
            if (corridor.intersects(newPos)) {
                collision = false;
                break;
            }
        }
        
        if (!collision) {
            x = newX;
            y = newY;
        }
    }
}


public class GamePanel extends JPanel implements Runnable, KeyListener {
    private Thread gameThread;
    private boolean running = false;
    private int playerX = 150, playerY = 150;
    private int speed = 3;
    private boolean up, down, left, right;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private ArrayList<Room> rooms;
    private Random random;
    private int health = 3;
    private long lastSpawnTime = 0;
    private boolean gameOver = false;
    private JButton resetButton;
    private int spawnRate = 1;
    private long lastIncreaseTime = 0;
    private ArrayList<Rectangle> corridors;
    private int score = 0;
    private String currentWeapon = "Pistol";
    private HospitalMap hospitalMap;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setLayout(null);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        hospitalMap = new HospitalMap();
        
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        corridors = new ArrayList<>();
        random = new Random();
        rooms = new ArrayList<>();
        generateCorridors();
        
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

    private void shoot() {
        int bulletSpeed;
        if (currentWeapon.equals("Pistol")) bulletSpeed = 3;
        else if (currentWeapon.equals("Shotgun")) bulletSpeed = 1;
        else bulletSpeed = 7; // SMG

        bullets.add(new Bullet(playerX, playerY, 0, -1, bulletSpeed));
    } 

    private void generateCorridors() {
        corridors.add(new Rectangle(300, 150, 100, 20)); // Horizontal corridor
        corridors.add(new Rectangle(300, 250, 20, 100)); // Vertical corridor
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

        hospitalMap.draw(g);

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

        int newX = playerX;
        int newY = playerY;
        
        if (up) playerY -= speed;
        if (down) playerY += speed;
        if (left) playerX -= speed;
        if (right) playerX += speed;

        Rectangle futureBounds = new Rectangle(newX, newY, 20, 20);
        boolean collision = false;

        for (Room room : hospitalMap.getRooms()) {
            for (Rectangle wall : room.getWalls()) {
                if (wall.intersects(futureBounds)) {
                    collision = true;
                    break;
                }
            }
        }

        for (Enemy enemy : enemies) {
            enemy.moveTowards(playerX, playerY, hospitalMap.getRooms(), hospitalMap.getCorridors());
        }

        for (Bullet bullet : bullets) { //Bullets
            bullet.move();
        }

        bullets.removeIf(bullet -> bullet.x < 0 || bullet.x > 800 || bullet.y < 0 || bullet.y > 600);
        
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            boolean inCorridor = false;
            enemy.moveTowards(playerX, playerY, hospitalMap.getRooms(), hospitalMap.getCorridors());
            for (Rectangle corridor : corridors) {
                if (corridor.intersects(new Rectangle(enemy.x, enemy.y, 20, 20))) {
                    inCorridor = true;
                    break;
                }
            }
            if (new Rectangle(enemy.x, enemy.y, 20, 20).intersects(new Rectangle(playerX, playerY, 20, 20))) {
                health--;
                enemyIterator.remove();
                if (health <= 0) {
                    gameOver = true;
                    return;
                } //Zombie dies if it bites the player

                if (!inCorridor) {
                    enemy.moveTowards(playerX, playerY, new ArrayList<>(), corridors);
                } else {
                    enemy.moveTowards(playerX, playerY, new ArrayList<>(), new ArrayList<>());
                }

                if (enemy.x == playerX && enemy.y == playerY) {
                    score += 10;
                    enemyIterator.remove();
                }
        }
            
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                //bullet.move();

                if (new Rectangle(bullet.x, bullet.y, 5, 5).intersects(new Rectangle(enemy.x, enemy.y, 20, 20))) {
                    bulletIterator.remove();
                    enemyIterator.remove();
                    score += 10; // Increase score when an enemy is killed
                    break;
                }

                for (Room room : rooms) {
                    for (Rectangle wall : room.getWalls()) {
                        if (wall.intersects(bullet.getBounds())) {
                            bulletIterator.remove(); // Remove bullet safely
                            break;
                        }
                    }
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
