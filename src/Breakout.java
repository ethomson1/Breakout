import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

// top-level container class
class Breakout {

    // constructor for the game
    // instantiates all of the top-level classes (model, view)
    // and tells the model to start the game
    Breakout(int fps, int speed) {
        Breakout.Model model = this.new Model(fps, speed);
        Breakout.View view = this.new View(model);
    }

    // game elements
    public class Block {
        private int posX;
        private int posY;
        private int width;
        private int height;
        private Color colour;
        public Block(int X, int Y, int width, int height, Color colour){
            this.posX = X;
            this.posY = Y;
            this.colour = colour;
            this.width = width;
            this.height = height;

        }
        public void resize(int X, int Y, int width, int height){
            this.posX = X;
            this.posY = Y;
            this.width = width;
            this.height = height;
        }
        public int[] getSize(){
            int[] size = {width, height};
            return size;
        }
        public int[] getPos(){
            int[] pos = {posX, posY};
            return pos;
        }
        public void paint(Graphics g) {
            g.setColor(Color.black);
            g.fillRect(posX, posY, width, height);
            g.setColor(colour);
            g.fillRect(posX+1, posY+1, width-2, height-2);
        }

    }
    class Ball {
        int x_coord;
        int y_coord;
        int leftBound;
        int rightBound;
        int top;
        int bottom;
        char[] direction = {'R', 'U'};

        public Ball(int x, int y){
            this.x_coord = x;
            this.y_coord = y;
        }
        public void setBoundaries(int left, int right, int top, int bottom){
            this.leftBound = left;
            this.rightBound = right;
            this.top = top;
            this.bottom = bottom;
        }
        public void initBall(int x, int y, char[] direction){
            this.direction = direction;
            this.x_coord = x;
            this.y_coord = y;
        }
        public int[] getPos(){
            int[] pos = {x_coord, y_coord};
            return pos;
        }
        public void release(){
            if(x_coord <= leftBound) direction[0] = 'R';
            if (x_coord >= rightBound) direction[0] = 'L';
            if(y_coord <= top) direction[1] = 'D';
            if(direction[0] == 'L') x_coord -= 1;
            if(direction[0] == 'R') x_coord += 1;
            if(direction[1] == 'U') y_coord -= 1;
            if(direction[1] == 'D') y_coord +=1;
        }
        public void bounce(){
            if(direction[1] == 'U') direction[1] = 'D';
            else if(direction[1] == 'D') direction[1] = 'U';
        }
        public void paint(Graphics g){
            g.setColor(Color.GRAY);
            g.fillOval(x_coord,y_coord,10,10);
        }
    }
    class Paddle {
        private int posX;
        private int posY;
        private int width;
        private int rightBound;
        private int leftBound;
        public Paddle(int xPos, int yPos, int width){
            this.posX = xPos;
            this.posY = yPos;
            this.width = width;
        }
        public int[] getPos(){
            int[] pos = {posX, posY};
            return pos;
        }
        public int getWidth(){
            return this.width;
        }
        public void resize(int xPos, int yPos, int width, int left, int right){
            this.posX = xPos;
            this.posY = yPos;
            this.width = width;
            this.rightBound = right;
            this.leftBound = left;
        }
        public void moveLeft(){
            if(this.posX > leftBound) {
                this.posX -= 4;
            }
        }
        public void moveRight(){
            if(this.posX < rightBound){
                this.posX += 4;
            }
        }
        public void paint(Graphics g) {
            g.setColor(Color.PINK);
            g.drawRect(posX, posY, width, 10);
            g.fillRect(posX, posY, width, 10);
        }
    }

    // model keeps track of game state (objects in the game)
    // contains a Timer that ticks periodically to advance the game
    // AND calls an update() method in the View to tell it to redraw
    class Model {
        private int score;
        private int fps;
        private int speed;
        private int blockCount; // Number of bricks left in the level
        private int level;
        String state;
        View view;

        public Model(int fps, int speed) {
            score = 0;
            this.fps = fps;
            this.speed = speed;
            setState("SPLASH");
        }

        public void incScore(){ score += 100; }
        public void destroyBlock() { blockCount--; }
        public int getBlockCount(){ return blockCount; }
        public void nextLevel(){
            if(level < 2) {
                level += 1;
            }
            if(level == 2){
                blockCount = 25;
            }
        }
        public int getLevel(){ return level; }
        public void setState(String state) {
            this.state = state;
        }
        public int getScore(){ return score; }
        public int getSpeed(){ return speed; }
        public int getFPS() { return  fps; }
        public String getState() { return state; }
        public void initGame(){
            score = 0;
            blockCount = 50;
            level = 1;
        }
    }

    class GameView extends JPanel{
        Model model;
        Paddle paddle;
        Ball ball;
        Block[][] blocks = new Block[5][10];
        JLabel score;
        JLabel fps;
        JLabel endGame;
        JButton playAgain;
        Timer timer;
        Timer ballTimer;
        public GameView(Model m){
            super();
            this.model = m;
            this.paddle = new Paddle(0,0,0);
            this.ball = new Ball(0,0);
            score = new JLabel("Score " + model.getScore(), JLabel.LEFT);
            score.setForeground(Color.WHITE);
            fps = new JLabel("FPS " + model.getFPS(), JLabel.LEFT);
            fps.setForeground(Color.YELLOW);
            endGame = new JLabel("Game Over!");
            endGame.setForeground(Color.WHITE);
            endGame.setFont(endGame.getFont().deriveFont(32.0f));
            playAgain = new JButton("Play again");

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    repaint();
                }
            };
            timer = new Timer(1000/model.getFPS(), actionListener);
            timer.start();
            ActionListener ballSpeed = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(model.getState() == "GAMEPLAY"){
                        ball.release();
                        detectCollision();
                    }
                }
            };
            ballTimer = new Timer(1000/model.getSpeed(), ballSpeed);
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if(model.getState() == "GAMEPLAY") {
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            paddle.moveRight();
                        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            paddle.moveLeft();
                        }
                        if(e.getKeyCode() == KeyEvent.VK_N){
                            model.nextLevel();
                            ballTimer.stop();
                            model.setState("START_GAME");

                        }
                    }
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if(model.getState() == "START_GAME"){
                        model.setState("GAMEPLAY");
                        ballTimer.start();
                    }
                }
            });
            playAgain.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    model.initGame();
                    model.setState("START_GAME");
                }
            });
        }
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            this.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
            Dimension windowSize = this.getSize();
            // Calculate initial position of paddle and ball
            if(model.getState() == "START_GAME"){
                initGameView();
            }
            if(model.getState() == "GAME_OVER"){
                int labelHeight = endGame.getHeight();
                int labelWidth = endGame.getWidth();
                endGame.setLocation(windowSize.width/2-labelWidth/2, windowSize.height/2-labelHeight/2);
                endGame.setVisible(true);
                playAgain.setVisible(true);
                playAgain.setLocation(windowSize.width/2 - playAgain.getWidth(), windowSize.height/2 + labelHeight);
            }
            if(model.getState() == "WIN"){
                endGame.setText("You Win!");
                int labelHeight = endGame.getHeight();
                int labelWidth = endGame.getWidth();
                endGame.setLocation(windowSize.width/2-labelWidth/2, windowSize.height/2-labelHeight/2);
                endGame.setVisible(true);
                playAgain.setLocation(windowSize.width/2 - playAgain.getWidth(), windowSize.height/2+labelHeight);
                playAgain.setVisible(true);
            }
            // Handle resize window events
            int blockWidth = windowSize.width/12;
            int blockHeight = windowSize.height/20;
            for(int i = 0; i < 5; i++){
                for(int j = 0; j < 10; j++){
                    if(blocks[i][j] != null) {
                        blocks[i][j].resize((j+1) * blockWidth, (i+1) * blockHeight, blockWidth, blockHeight);
                        blocks[i][j].paint(g);
                    }
                }
            }
            int paddleWidth = windowSize.width/10;
            int rightBound = windowSize.width - blockWidth;
            int leftBound = blockWidth;
            int topBound = blockHeight;
            int bottomBound = windowSize.height;
            paddle.resize(paddle.getPos()[0], windowSize.height-10, paddleWidth, leftBound, rightBound-paddleWidth);
            ball.setBoundaries(leftBound, rightBound-20, topBound, bottomBound);
            paddle.paint(g);
            ball.paint(g);
        }

        public void initGameView(){
            Dimension windowSize = this.getSize();
            int blockWidth = windowSize.width/12;
            int paddleWidth = windowSize.width/10;
            // Calculate initial position of paddle and ball
            int paddlePos = windowSize.width/2 - paddleWidth/2;
            int ballPos = windowSize.width/2 - 5;
            // reset blocks
            for(int i = 0; i < 5; i++){
                Color colour = blockColour(i);
                for(int j = 0; j < 10; j++) {
                    if(model.getLevel() == 2){
                        if(j%2 == 0 && i%2 == 0){
                            blocks[i][j] = new Block(0,0,0,0, colour);
                        } else if(j % 2 == 1 && i % 2 == 1){
                            blocks[i][j] = new Block(0,0,0,0, colour);
                        } else {
                            blocks[i][j] = null;
                        }
                    } else {
                        blocks[i][j] = new Block(0,0,0,0, colour);
                    }
                }
            }
            this.add(endGame);
            this.add(score);
            this.add(fps);
            this.add(playAgain);
            score.setText("Score " + model.getScore());
            endGame.setVisible(false);
            playAgain.setVisible(false);
            int rightBound = windowSize.width - blockWidth;
            int leftBound = blockWidth;
            char[] ballDirection = {'R','U'};
            paddle.resize(paddlePos, windowSize.height-10, paddleWidth, leftBound, rightBound-paddleWidth);
            ball.initBall(ballPos, windowSize.height-20, ballDirection);
        }

        public Color blockColour(int row){
            if(row == 0){
                return Color.RED;
            }
            else if(row == 1){
                return Color.ORANGE;
            }
            else if(row == 2){
                return Color.YELLOW;
            }
            else if(row == 3){
                return Color.GREEN;
            }
            else {
                return Color.BLUE;
            }
        }

        public void detectCollision(){
            int ballX = ball.getPos()[0];
            int ballY = ball.getPos()[1];
            int paddleX = paddle.getPos()[0];
            int paddleY = paddle.getPos()[1];
            int paddleWidth = paddle.getWidth();
            // Collision with the paddle
            if(paddleY-ballY <= 10 && paddleY - ballY >= 0 && ballX >= paddleX && ballX <= paddleX + paddleWidth - 10){
                ball.bounce();
            }
            // Collision with a brick
            for(int i = 0; i < 5; i++){
                for(int j = 0; j < 10; j++){
                    if(blocks[i][j] != null){
                        int blockX = blocks[i][j].getPos()[0];
                        int blockY = blocks[i][j].getPos()[1];
                        int blockWidth = blocks[i][j].getSize()[0];
                        int blockHeight = blocks[i][j].getSize()[1];
                        if(ballX >= blockX && ballX <= blockX + blockWidth -10 && ballY >= blockY && ballY <= blockY + blockHeight){
                            ball.bounce();
                            blocks[i][j] = null;
                            model.incScore();
                            model.destroyBlock();
                            if(model.getBlockCount() == 0){
                                if(model.getLevel() == 1){
                                    model.nextLevel();
                                    ballTimer.stop();
                                    model.setState("START_GAME");
                                }
                                else if(model.getLevel() == 2) {
                                    model.setState("WIN");
                                }
                            }
                            score.setText("Score " + model.getScore());
                        }
                    }
                }
            }
            // Collision with bottom of the screen
            Dimension windowSize = this.getSize();
            if(ballY > windowSize.height){
                model.setState("GAME_OVER");
                ballTimer.stop();
            }
        }

    }
    // game window
    // draws everything based on the game state
    // receives notification from the model when something changes, and
    // draws components based on the model.
    class View extends JComponent {
        Model model;
        JFrame frame;
        GameView gameView;
        JPanel splash;

        public View(Model m){
            this.model = m;
            frame = new JFrame("Breakout"); // jframe is the app window
            splash = new JPanel();
            gameView = new GameView(m);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setSize(640, 480); // window size
            frame.setMinimumSize(new Dimension(640, 480));
            showSplash();
            frame.setContentPane(splash); // add canvas to jframe
            frame.setVisible(true); // show the window
            splash.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e){
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && model.getState() == "SPLASH")
                    {
                        setPanel(gameView);
                        showGame();
                        model.setState("START_GAME");
                    }
                }
            });
            gameView.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_Q) {
                        setPanel(splash);
                        showSplash();
                        model.setState("SPLASH");
                    }
                }
            });
        }

        public void setPanel(JPanel panel)
        {
            frame.getContentPane().removeAll();
            frame.setContentPane(panel);
            panel.setVisible(true);
            frame.getContentPane().revalidate();
            frame.getContentPane().repaint();
        }

        public void showSplash(){
            splash.setBackground(Color.black);
            splash.setFocusable(true);
            splash.requestFocusInWindow();
            try {
                BufferedImage myPicture = ImageIO.read(getClass().getResource("splash.png"));
                JLabel picLabel = new JLabel(new ImageIcon(myPicture));
                splash.add(picLabel);
            }
            catch (IOException e){
                System.out.println("Error: Could not read file");
                return;
            }
            splash.setVisible(true);
        }

        public void showGame(){
            gameView.setFocusable(true);
            gameView.requestFocusInWindow();
            gameView.setBackground(Color.black);
            model.initGame();
            gameView.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        }
        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g; // cast to get 2D drawing methods
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    // entry point for the application
    public static void main(String[] args) {
        int fps;
        int speed;
        if(args.length > 0) {
            fps = Integer.parseInt(args[0]);
        }
        else {
            fps = 30;
        }
        if(args.length > 1) {
            speed = Integer.parseInt(args[1]);
        }
        else {
            speed = 100;
        }
        Breakout game = new Breakout(fps, speed);
    }
}
