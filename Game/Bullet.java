package Game;
import java.awt.*;


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
