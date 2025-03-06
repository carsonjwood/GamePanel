package Game;
import java.awt.*;
import java.util.ArrayList;
import java.util.*;

public class HospitalMap {
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