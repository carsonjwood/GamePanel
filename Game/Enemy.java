package Game;
import java.awt.*;
import java.util.*;

public class Enemy {
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