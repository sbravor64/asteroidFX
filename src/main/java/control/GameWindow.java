package control;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class GameWindow implements Initializable {

    @FXML
    Canvas gameCanvas;

    //variables
    private static final Random RAND = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 60;
    static final Image PLAYER_IMG = new Image("images/rockets/rocket_3@4x.png");
    static final Image EXPLOSION_IMG = new Image("images/explosion/explosion_2@4x.png");
    static final int EXPLOSION_W = 128;
    static final int EXPLOSION_ROWS = 3;
    static final int EXPLOSION_COL = 3;
    static final int EXPLOSION_H = 128;
    static final int EXPLOSION_STEPS = 15;

    static final Image BOMBS_IMG[] = {
            new Image("images/marcianos/1@4x.png"),
            new Image("images/marcianos/2@4x.png"),
            new Image("images/marcianos/3@4x.png"),
            new Image("images/marcianos/4@4x.png"),
            new Image("images/marcianos/5@4x.png"),
            new Image("images/marcianos/6@4x.png"),
            new Image("images/marcianos/7@4x.png"),
            new Image("images/marcianos/8@4x.png"),
            new Image("images/marcianos/9@4x.png"),
    };

    final int MAX_BOMBS = 10,  MAX_SHOTS = MAX_BOMBS * 2;
    boolean gameOver = false;
    private GraphicsContext gc;

    Rocket player;
    List<Disparo> shots;
    List<Universo> univ;
    List<Bomb> Bombs;

    private double mouseX;
    private int score;


    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        gameCanvas = new Canvas(WIDTH, HEIGHT);
        gc = gameCanvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        gameCanvas.setCursor(Cursor.MOVE);
        gameCanvas.setOnMouseMoved(e -> mouseX = e.getX());
        gameCanvas.setOnMouseClicked(e -> {
            if(shots.size() < MAX_SHOTS) shots.add(player.disparo());
            if(gameOver) {
                gameOver = false;
                setup();
            }
        });
        setup();

    }

    public void setScene(Scene sc){
        scene=sc;
    }

    //setup the game
    private void setup() {
        univ = new ArrayList<>();
        shots = new ArrayList<>();
        Bombs = new ArrayList<>();
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add);
    }

    //run Graphics
    private void run(GraphicsContext gc) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Puntos: " + score, 60,  20);


        if(gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over \n Tu puntuación es: " + score + " \n Click para jugar otra ves", WIDTH / 2, HEIGHT /2.5);
            //	return;
        }
        univ.forEach(Universo::draw);

        player.update();
        player.draw();
        player.posX = (int) mouseX;

        Bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if(player.colide(e) && !player.exploding) {
                player.explode();
            }
        });


        for (int i = shots.size() - 1; i >=0 ; i--) {
            Disparo shot = shots.get(i);
            if(shot.posY < 0 || shot.toRemove)  {
                shots.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for (Bomb bomb : Bombs) {
                if(shot.colide(bomb) && !bomb.exploding) {
                    score++;
                    bomb.explode();
                    shot.toRemove = true;
                }
            }
        }

        for (int i = Bombs.size() - 1; i >= 0; i--){
            if(Bombs.get(i).destroyed)  {
                Bombs.set(i, newBomb());
            }
        }

        gameOver = player.destroyed;
        if(RAND.nextInt(10) > 2) {
            univ.add(new Universo());
        }
        for (int i = 0; i < univ.size(); i++) {
            if(univ.get(i).posY > HEIGHT)
                univ.remove(i);
        }
    }

    //player
    public class Rocket {

        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;

        public Rocket(int posX, int posY, int size,  Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Disparo disparo() {
            return new Disparo(posX + size / 2 - Disparo.size / 2, posY - Disparo.size);
        }

        public void update() {
            if(exploding) explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        public void draw() {
            if(exploding) {
                gc.drawImage(EXPLOSION_IMG, explosionStep % EXPLOSION_COL * EXPLOSION_W, (explosionStep / EXPLOSION_ROWS) * EXPLOSION_H + 1,
                        EXPLOSION_W, EXPLOSION_H,
                        posX, posY, size, size);
            }
            else {
                gc.drawImage(img, posX, posY, size, size);
            }
        }

        public boolean colide(Rocket other) {
            int d = distancia(this.posX + size / 2, this.posY + size /2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2 ;
        }

        public void explode() {
            exploding = true;
            explosionStep = -1;
        }

    }

    //computer player
    public class Bomb extends Rocket {

        int SPEED = (score/5)+2;

        public Bomb(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if(!exploding && !destroyed) posY += SPEED;
            if(posY > HEIGHT) destroyed = true;
        }
    }

    //bullets
    public class Disparo {

        public boolean toRemove;

        int posX, posY, speed = 10;
        static final int size = 6;

        public Disparo(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update() {
            posY-=speed;
        }


        public void draw() {
            gc.setFill(Color.RED);
            if (score >=50 && score<=70 || score>=120) {
                gc.setFill(Color.YELLOWGREEN);
                speed = 50;
                gc.fillRect(posX-5, posY-10, size+10, size+30);
            } else {
                gc.fillOval(posX, posY, size, size);
            }
        }

        public boolean colide(Rocket Rocket) {
            int distance = distancia(this.posX + size / 2, this.posY + size / 2,
                    Rocket.posX + Rocket.size / 2, Rocket.posY + Rocket.size / 2);
            return distance  < Rocket.size / 2 + size / 2;
        }


    }

    //environment
    public class Universo {
        int posX, posY;
        private int h, w, r, g, b;
        private double opacity;

        public Universo() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            w = RAND.nextInt(5) + 1;
            h =  RAND.nextInt(5) + 1;
            r = RAND.nextInt(100) + 150;
            g = RAND.nextInt(100) + 150;
            b = RAND.nextInt(100) + 150;
            opacity = RAND.nextFloat();
            if(opacity < 0) opacity *=-1;
            if(opacity > 0.5) opacity = 0.5;
        }

        public void draw() {
            if(opacity > 0.8) opacity-=0.01;
            if(opacity < 0.1) opacity+=0.01;
            gc.setFill(Color.rgb(r, g, b, opacity));
            gc.fillOval(posX, posY, w, h);
            posY+=20;
        }
    }


    Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }

    int distancia(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }
}
