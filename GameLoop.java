package main;

import controllers.InputHandler;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import models.Entity;
import models.characters.Fighter;
import models.Projectile;
import view.ArenaView;

import java.util.ArrayList;
import java.util.List;

public class GameLoop extends AnimationTimer {

    private ArenaView view;
    private InputHandler input;
    private Fighter p1;
    private Fighter p2;
    private double screenWidth;
    private double screenHeight;

    // Optimization: Reuse this list to avoid "Garbage Collection" lag
    private List<Entity> toRemove = new ArrayList<>();

    public GameLoop(ArenaView view, InputHandler input) {
        this.view = view;
        this.input = input;
        this.p1 = view.getPlayer1();
        this.p2 = view.getPlayer2();
        
        // Get screen size for boundaries
        this.screenWidth = view.getScene().getWidth();
        this.screenHeight = view.getScene().getHeight();
    }

    @Override
    public void handle(long now) {
        // 1. Handle Inputs (Move & Shoot)
        handlePlayer1();
        handlePlayer2();

        // 2. Update Entities
        // CRITICAL: This runs the Weapon Reload Timer inside the Fighter class
        p1.update();
        p2.update();
        
        // 3. Physics & Collisions
        updateProjectiles();

        // 4. Draw
        view.render();
        
        // 5. Check Win Condition
        checkGameOver();
    }

    private void handlePlayer1() {
        // MOVEMENT: W, A, S, D
        double dx = 0, dy = 0;
        if (input.isKeyPressed(KeyCode.W)) dy = -1;
        if (input.isKeyPressed(KeyCode.S)) dy = 1;
        if (input.isKeyPressed(KeyCode.A)) dx = -1;
        if (input.isKeyPressed(KeyCode.D)) dx = 1;

        // Apply Move (Fighter class calculates rotation here automatically)
        p1.move(dx, dy, 0, screenWidth, 0, screenHeight);

        // SHOOTING: F
        if (input.isKeyPressed(KeyCode.F)) {
            // FIX: Call attack() with NO arguments.
            // The Fighter class will check its own rotation to decide the direction.
            Projectile bullet = p1.attack(); 
            if (bullet != null) {
                view.getEntities().add(bullet);
            }
        }
        if (input.isKeyPressed(KeyCode.G)) {
            p1.switchWeapon();

        }       
        // Manual Reload
        if (input.isKeyPressed(KeyCode.H)) {
            p1.getWeapon().reload();
    }
    }
    private void handlePlayer2() {
        // MOVEMENT: Arrow Keys
        double dx = 0, dy = 0;
        if (input.isKeyPressed(KeyCode.UP))    dy = -1;
        if (input.isKeyPressed(KeyCode.DOWN))  dy = 1;
        if (input.isKeyPressed(KeyCode.LEFT))  dx = -1;
        if (input.isKeyPressed(KeyCode.RIGHT)) dx = 1;

        // Apply Move
        p2.move(dx, dy, 0, screenWidth, 0, screenHeight);

        // SHOOTING: L
        if (input.isKeyPressed(KeyCode.L)) {
            // FIX: Call attack() with NO arguments.
            Projectile bullet = p2.attack(); 
            if (bullet != null) {
                view.getEntities().add(bullet);
            }
        }
        if (input.isKeyPressed(KeyCode.K)) {
            p2.switchWeapon();
        }
        // Manual Reload
        if (input.isKeyPressed(KeyCode.J)) {
            p2.getWeapon().reload();
        }

    }

    private void updateProjectiles() {
        // Clear the reusable list
        toRemove.clear();
        
        List<Entity> allEntities = view.getEntities();

        for (int i = 0; i < allEntities.size(); i++) {
            Entity e = allEntities.get(i);
            
            if (e instanceof Projectile) {
                Projectile p = (Projectile) e;
                p.update(); // Move the bullet

                // Cleanup: Remove if off-screen
                if (!p.isActive()) {
                    toRemove.add(p);
                    continue;
                }

                // --- UPDATED COLLISION LOGIC ---
                // We removed the "if directionX > 0" check because bullets can now fly vertically.
                // We simply check if the bullet hits a player.
                
                // Check Collision with Player 1
                if (p.getHitbox().intersects(p1.getHitbox())) {
                    // Note: Since we don't have an "owner" variable yet, 
                    // this assumes if you touch a bullet, you take damage.
                    // The offset in Fighter.attack() prevents you from shooting yourself instantly.
                    p1.takeDamage(p.getDamage());
                    p.deactivate(); 
                    toRemove.add(p);
                }
                // Check Collision with Player 2
                else if (p.getHitbox().intersects(p2.getHitbox())) {
                    p2.takeDamage(p.getDamage());
                    p.deactivate();
                    toRemove.add(p);
                }
            }
        }

        // Remove dead bullets to save memory
        allEntities.removeAll(toRemove);
    }
    
    private void checkGameOver() {
        if (!p1.isAlive()) {
            this.stop(); // 1. FREEZE the game loop
            view.showWinner("PLAYER 2"); // 2. Show CS-Style Menu
        } else if (!p2.isAlive()) {
            this.stop();
            view.showWinner("PLAYER 1");
        }
    }
}