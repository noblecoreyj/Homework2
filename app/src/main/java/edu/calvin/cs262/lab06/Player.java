package edu.calvin.cs262.lab06;

/**
 * Player object based on kvlinden's RESTful API.
 * Based on Deitel's WeatherViewer app (chapter 17).
 *
 * @author deitel
 * @author kvlinden
 * @author cjn8
 * @version spring, 2017
 */
public class Player {

    private int id;
    private String name, email;

    public Player(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    //Accessors
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
