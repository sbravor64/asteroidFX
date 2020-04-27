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
    private Random random = new Random();
    private int width = 800;
    private int height = 600;
    private int player_size = 60;
    Image player_img = new Image("images/rockets/rocket_3@4x.png");
    Image explosion_img = new Image("images/explosion/explosion_2@4x.png");
    int explosion_w = 128;
    int explosion_rows = 3;
    int explosion_col = 3;
    int explosion_h = 128;
    int explosion_steps = 15;

    static final Image marcianos_img[] = {
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

    final int max_marcianos = 10,  max_disparos = max_marcianos * 2;
    boolean gameOver = false;
    private GraphicsContext gc;

    Rocket player;
    List<Disparo> disparos;
    List<Universo> univ;
    List<Marciano> marcianos;

    private double mouseX;
    private int puntos;

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
            if(disparos.size() < max_disparos) disparos.add(player.disparo());
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

    private void setup() {
        univ = new ArrayList<>();
        disparos = new ArrayList<>();
        marcianos = new ArrayList<>();
        player = new Rocket(width / 2, height - player_size, player_size, player_img);
        puntos = 0;
        IntStream.range(0, max_marcianos).mapToObj(i -> this.newMarciano()).forEach(marcianos::add);
    }

    private void run(GraphicsContext gc) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, width, height);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Puntos: " + puntos, 60,  20);


        if(gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over \n Tu puntuación es: " + puntos + " \n Click para jugar otra ves", width / 2, height /2.5);
            //	return;
        }
        univ.forEach(Universo::draw);

        player.update();
        player.draw();
        player.posX = (int) mouseX;

        marcianos.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if(player.colide(e) && !player.exploding) {
                player.explode();
            }
        });


        for (int i = disparos.size() - 1; i >=0 ; i--) {
            Disparo shot = disparos.get(i);
            if(shot.posY < 0 || shot.toRemove)  {
                disparos.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for (Marciano bomb : marcianos) {
                if(shot.colide(bomb) && !bomb.exploding) {
                    puntos++;
                    bomb.explode();
                    shot.toRemove = true;
                }
            }
        }

        for (int i = marcianos.size() - 1; i >= 0; i--){
            if(marcianos.get(i).destroyed)  {
                marcianos.set(i, newMarciano());
            }
        }

        gameOver = player.destroyed;
        if(random.nextInt(10) > 2) {
            univ.add(new Universo());
        }
        for (int i = 0; i < univ.size(); i++) {
            if(univ.get(i).posY > height)
                univ.remove(i);
        }
    }

    public class Disparo {

        static final int size = 6;
        public boolean toRemove;
        int posX;
        public int posY;
        int speed = 30;

        public Disparo(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update() {
            posY-=speed;
        }

        public void draw() {
            gc.setFill(Color.RED);
            if (puntos >=50 && puntos <=70 || puntos >=120) {
                gc.setFill(Color.YELLOWGREEN);
                speed = 50;
                gc.fillRect(posX-5, posY-10, size +10, size +30);
            } else {
                gc.fillOval(posX, posY, size, size);
            }
        }

        public boolean colide(GameWindow.Rocket Rocket) {
            int distance = distancia(this.posX + size / 2, this.posY + size / 2,
                    Rocket.posX + Rocket.size / 2, Rocket.posY + Rocket.size / 2);
            return distance  < Rocket.size / 2 + size / 2;
        }
    }

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
            destroyed = explosionStep > explosion_steps;
        }

        public void draw() {
            if(exploding) {
                gc.drawImage(explosion_img, explosionStep % explosion_col * explosion_w, (explosionStep / explosion_rows) * explosion_h + 1,
                        explosion_w, explosion_h,
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

    public class Marciano extends Rocket {

        int SPEED = (puntos /5)+2;

        public Marciano(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if(!exploding && !destroyed) posY += SPEED;
            if(posY > height) destroyed = true;
        }
    }

    public class Universo {
        int posX, posY;
        private int h, w, r, g, b;
        private double opacity;

        public Universo() {
            posX = random.nextInt(width);
            posY = 0;
            w = random.nextInt(5) + 1;
            h =  random.nextInt(5) + 1;
            r = random.nextInt(100) + 150;
            g = random.nextInt(100) + 150;
            b = random.nextInt(100) + 150;
            opacity = random.nextFloat();
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

    Marciano newMarciano() {
        return new Marciano(50 + random.nextInt(width - 100), 0, player_size-20, marcianos_img[random.nextInt(marcianos_img.length)]);
    }

    int distancia(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }
}
