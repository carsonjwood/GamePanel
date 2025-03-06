package Game;
import java.awt.*;
import java.util.ArrayList;

public class Room {
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
