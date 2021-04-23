import Draw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;

public class RunningGame {
    public static void main(String[] args){
        RunningGame game = new RunningGame();
        game.go();
    }

    JFrame frame;
    JPanel player;
    JLabel pointCounter;
    final int WIDTH = 1000;
    final int HEIGHT = 400;
    final int GROUND_SIZE = 50;
    final int PLAYER_SIZE = 50;
    final int PLAYER_DISTANCE = 50;
    int jumpStage;
    final int[] allJumpStages = {70, 60, 50, 40, 30, 20, 10, -10, -20, -30, -40, -50, -60, -70};
    int points;
    int speed;
    //int obsticlesInPlay;
    ArrayList<Obsticle> obsticles;
    int count;
    final int POINT_CAP = 3000;
    int pointCap;
    int highScore;
    JLabel highLabel;
    final String highScoreFileName = "High Score";
    private void go(){
        frame = new JFrame("Running Game");
        frame.setLayout(null);

        JPanel ground = new JPanel();
        ground.setBackground(Color.BLACK);
        // ground.setSize(WIDTH, HEIGHT/4);
        ground.setBounds(0, HEIGHT-GROUND_SIZE, WIDTH, GROUND_SIZE);
        frame.getContentPane().add(ground);

        createPlayer();

        pointCounter = new JLabel();
        pointCounter.setBounds(WIDTH-150, 10, 150, 50);
        pointCounter.setFont(new Font("Time", Font.PLAIN, 25));
        pointCounter.setText(Integer.toString(points));
        frame.getContentPane().add(pointCounter);

        try {
            Reader read = new FileReader(highScoreFileName);
            highScore = read.read();
            read.close();
        }catch(Exception ex){
            System.out.println(ex);
        }
        highLabel = new JLabel();
        highLabel.setBounds(WIDTH-350, 10, 150, 50);
        highLabel.setFont(new Font("Time", Font.PLAIN, 25));
        highLabel.setText("High: " + highScore);
        frame.getContentPane().add(highLabel);
        //System.out.println(highScore);

        frame.setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);

        play();
    }

    private void play(){
        jumpStage = -1;
        points = 0;
        obsticles = new ArrayList<>();
        count = 0;
        player.setLocation(PLAYER_DISTANCE, HEIGHT - GROUND_SIZE - PLAYER_SIZE);
        while(true){
            try {
                Thread.sleep(1000/24);
            }catch(Exception ex){
                System.out.println(ex);
            }
            points++;
            pointCounter.setText(Integer.toString(points));

            pointCap = points;
            if(points > POINT_CAP){
                pointCap = POINT_CAP;
            }

            playerLocation();
            obsticleLocations();
            if(gameEnd()){
                break;
            }
        }
        if(points > highScore){
            highScore = points;
            highLabel.setText("High: " + highScore);
            try{
                Writer wr = new FileWriter(highScoreFileName);
                wr.write(highScore);
                wr.close();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
        try {
            Thread.sleep(3000);
        }catch(Exception ex){
            System.out.println(ex);
        }
        for (Obsticle obj: obsticles) {
            //frame.remove(obj.obj);
            obj.obj.setLocation(-100, -200);
        }
        play();
    }

    private void createPlayer(){
        player = new JPanel();
        player.setBackground(Color.BLUE);
        player.setBounds(PLAYER_DISTANCE, HEIGHT - GROUND_SIZE - PLAYER_SIZE, PLAYER_SIZE, PLAYER_SIZE);
        player.setLayout(null);

        try{
            JFileChooser fileSave = new JFileChooser(Draw.saveFileName);
            fileSave.showOpenDialog(frame);
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(fileSave.getSelectedFile()));
            Color[][] colors = (Color[][]) is.readObject();
            System.out.println(colors.length);
            JPanel[][] temps = new JPanel[colors.length][colors[1].length];
            for (int i = 0; i < colors.length; i++) {
                for (int j = 0; j < colors[i].length; j++) {
                    //System.out.println(colors[i][j]);
                    temps[i][j] = new JPanel();
                    temps[i][j].setBounds(i, j, 1, 1);
                    temps[i][j].setBackground(colors[i][j]);
                    //System.out.println(colors[i][j].toString());
                    player.add(temps[i][j]);
                }
            }
        }catch (Exception ex){ex.printStackTrace();}

        Action space = new SpaceAction();
        player.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "Space Action");
        player.getActionMap().put("Space Action", space);

        frame.getContentPane().add(player);
    }

    private boolean gameEnd(){
        for (Obsticle obj: obsticles) {
            if (player.getY() + PLAYER_SIZE >= HEIGHT - GROUND_SIZE - obj.height) {
                return obj.obj.getX() <= player.getX() + PLAYER_SIZE && obj.obj.getX() + obj.width >= player.getX();
            }
        }
        return false;
    }

    private void playerLocation(){
        //System.out.println("(" + player.getX() + ", " + player.getY() + ")");
        if(jumpStage == allJumpStages.length){
            jumpStage = -1;
        }
        else if(jumpStage > -1){
            player.setLocation(PLAYER_DISTANCE, player.getY() - allJumpStages[jumpStage]);
            jumpStage++;
        }
    }

    private void obsticleLocations(){
        /* if(obsticlesInPlay > obsticles.size()){
            obsticles.add(new Obsticle((int)Math.round(Math.random()*temp/50+20), (int)Math.round(Math.random()*temp/15+50)));
        }*/
        count++;
        speed = (int)Math.round(0.8*(pointCap/120f) + 20);
        // obsticlesInPlay = speed/10 + 1;
        //System.out.println(Integer.toString(speed));

        if(WIDTH/speed/2 < count){
            count = 0;
            addObstical();
        }
        else if(Math.random() <= 0.01){
            addObstical();
        }
        boolean remove = false;
        for (Obsticle obj: obsticles) {
            obj.move();
        }
        if(obsticles.size() > 0 && !obsticles.get(0).visible){
            obsticles.remove(0);
        }
        // System.out.println(obsticles.size());
    }

    private void addObstical(){
        obsticles.add(new Obsticle((int)Math.round(Math.random()*pointCap/100+25),
                (int)Math.round(Math.random()*pointCap/30+50)));
    }


    private class Obsticle{
        int height;
        int width;
        JPanel obj;
        boolean visible;
        public Obsticle(int width, int height){
            this.height = height;
            this.width = width;
            visible = true;

            obj = new JPanel();
            obj.setBackground(Color.GREEN);
            obj.setBounds(WIDTH - this.width, HEIGHT - GROUND_SIZE - this.height, this.width, this.height);
            frame.getContentPane().add(obj);
        }
        public void move(){
            obj.setLocation(obj.getX() - speed, obj.getY());
            if(obj.getX() + width < 0){
                visible = false;
            }
        }
    }

    private class SpaceAction extends AbstractAction{

        @Override
        public void actionPerformed(ActionEvent e) {
            //player.setLocation(player.getX(), player.getY()-50);
            if(jumpStage == -1) {
                jumpStage = 0;
            }
        }
    }
}
