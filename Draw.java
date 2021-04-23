import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TreeSet;

public class Draw {
    public static void main(String[] args){
        Draw game = new Draw();
        game.go();
    }

    JFrame frame;
    JFrame control;
    final int WIDTH = 500;
    final int HEIGHT = 500;
    final int PIXEL_WIDTH = 10;
    final int C_WIDTH = 175;
    final int C_HEIGHT = 300;
    JPanel[][] pixels = new JPanel[WIDTH/PIXEL_WIDTH][HEIGHT/PIXEL_WIDTH];
    boolean mousePressed;
    Color color;
    JTextField cursorWidthField;
    Hashtable<JRadioButton, Color> checkBoxes;
    Stack<TreeSet<Change>> changes;
    boolean drawFlag;
    public final static String saveFileName = "Running-Game-and-Draw";
    private void go(){
        drawFlag = false;

        frame = new JFrame("Canvas");
        frame.setLayout(null);

        control = new JFrame("Controls");
        control.setLayout(new BoxLayout(control.getContentPane(), BoxLayout.Y_AXIS));

        JLabel cursorWidthLabel = new JLabel("Cursor Width: ");
        control.getContentPane().add(cursorWidthLabel);
        cursorWidthField = new JTextField("30");
        control.getContentPane().add(cursorWidthField);

        JLabel colorLabel = new JLabel("Colors: ");
        control.getContentPane().add(colorLabel);

        checkBoxes = new Hashtable<>();
        checkBoxes.put(new JRadioButton("White"), Color.WHITE);
        checkBoxes.put(new JRadioButton("Black", true), Color.BLACK);
        checkBoxes.put(new JRadioButton("Blue"), Color.BLUE);
        checkBoxes.put(new JRadioButton("Red"), Color.RED);
        checkBoxes.put(new JRadioButton("Green"), Color.GREEN);
        checkBoxes.put(new JRadioButton("Yellow"), Color.YELLOW);

        color = Color.BLACK;
        ButtonGroup group = new ButtonGroup();

        for(JRadioButton box: checkBoxes.keySet()) {
            control.getContentPane().add(box);
            group.add(box);
        }


        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                pixels[i][j] = new JPanel();
                pixels[i][j].setBackground(Color.WHITE);
                pixels[i][j].setBounds(i*PIXEL_WIDTH, j*PIXEL_WIDTH, PIXEL_WIDTH, PIXEL_WIDTH);
                frame.getContentPane().add(pixels[i][j]);
            }
        }
        frame.addMouseListener(new Listener());
        mousePressed = false;

        JButton save = new JButton("Save");
        save.addActionListener(new SaveListener());
        control.getContentPane().add(save);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new RestoreListener());
        control.getContentPane().add(restore);

        changes = new Stack<>();
        changes.push(new TreeSet<>());

        Runnable threadJob = new Run();
        Thread drawThread = new Thread(threadJob);
        drawThread.start();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);

        control.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        control.setSize(C_WIDTH, C_HEIGHT);
        control.setLocation(WIDTH+10, 0);
        control.setVisible(true);
    }

    private void draw(){
        if(mousePressed) {
            for(JRadioButton box: checkBoxes.keySet()) {
                if(box.isSelected()){
                    color = checkBoxes.get(box);
                }
            }
            Point b = MouseInfo.getPointerInfo().getLocation();
            int x = (int) b.getX() - frame.getX();
            int y = (int) b.getY() - frame.getY();
            //System.out.println("(" + x + ", " + y + ")");
            for (JPanel[] i : pixels) {
                for (JPanel j : i) {
                    if (Math.pow(j.getX() - x, 2) + Math.pow(j.getY() - y, 2) <= Math.pow(Integer.parseInt(cursorWidthField.getText()), 2)) {
                        //changes.get(Stack.lastElement()).add(new Change(j.getBackground(), color, j.getX(), j.getY()));
                        changes.lastElement().add(new Change(j.getBackground(), color, j.getX()/PIXEL_WIDTH+1, j.getY()/PIXEL_WIDTH+1));
                        j.setBackground(color);
                        drawFlag = true;
                    }
                }
            }
        }
    }

    private class Change implements Comparable{
        private Color start;
        private Color end;
        private int x;
        private int y;
        public Change(Color start, Color end, int x, int y){
            this.start = start;
            this.end = end;
            this.x = x;
            this.y = y;
        }
        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
        public Color getStart(){
            return start;
        }
        public boolean equals(Object o){
            if(! (o instanceof Change)){return false;}
            Change c = (Change) o;
            return c.x == this.x && c.y == this.y && c.start == this.start && c.end == this.end;
        }
        public String toString(){
            return "x: " + x + "  y: " + y;
        }

        @Override
        public int compareTo(Object o) {
            Change c = (Change) o;
            if(c.x == this.x && c.y == this.y && c.start == this.start && c.end == this.end){
                return 0;
            }
            return -1;
        }
    }


    private class Run implements java.lang.Runnable{
        public void run(){
            while(true){
                draw();
                try {
                    Thread.sleep(0);
                }catch(Exception ex){
                    System.out.println(ex);
                }
            }
        }
    }

    private class Listener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
            //System.out.println("clicked");
            //frame.setContentPane(new JPanel());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //System.out.println("pressed");
            if(changes.empty()){
                changes.add(new TreeSet<>());
            }
            mousePressed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //System.out.println("resleased");
            mousePressed = false;
            if(drawFlag) {
                changes.push(new TreeSet<>());
                drawFlag = false;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //System.out.println("entered");
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //System.out.println("exit");
        }
    }

    private class SaveListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            Color[][] colors = new Color[WIDTH/PIXEL_WIDTH][HEIGHT/PIXEL_WIDTH];
            for (int i = 0; i < pixels.length; i++) {
                for (int j = 0; j < pixels[i].length; j++) {
                    colors[i][j] = pixels[i][j].getBackground();
                }
            }

            JFileChooser fileSave = new JFileChooser(saveFileName);
            fileSave.showSaveDialog(frame);
            try {
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileSave.getSelectedFile()));
                os.writeObject(colors);
                os.close();
            }catch(IOException ex){ex.printStackTrace();}
        }
    }
    private class RestoreListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            JFileChooser fileSave = new JFileChooser(saveFileName);
            fileSave.showOpenDialog(frame);

            try{
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(fileSave.getSelectedFile()));
                Color[][] colors = (Color[][]) is.readObject();
                for (int i = 0; i < colors.length; i++) {
                    for (int j = 0; j < colors[i].length; j++) {
                        pixels[i][j].setBackground(colors[i][j]);
                        //System.out.println(colors[i][j].toString());
                        pixels[i][j].setBounds(i*PIXEL_WIDTH, j*PIXEL_WIDTH, PIXEL_WIDTH, PIXEL_WIDTH);
                        frame.getContentPane().add(pixels[i][j]);
                    }
                }
            }catch (Exception ex){ex.printStackTrace();}
        }
    }
}
