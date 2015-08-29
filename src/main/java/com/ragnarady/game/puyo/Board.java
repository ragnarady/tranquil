package main.java.com.ragnarady.game.puyo;

import main.java.com.ragnarady.game.puyo.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;


class Board extends JComponent implements ActionListener {
    static int rowsNumber, columnsNumber;
    static int puyosTable[][];

    Image puyoImages[];
    PointNode combo; //Formation of combo is checked using this object
    Timer timer, timerDestroy, timerDropUndestroyed, timerAnimation;    //different timers used for animation of puyos

    Random random;

    int rotation;            //used for the rotation of the puyos
    int puyoSize;            //length of the puyo ie. width and height
    int puyosToDelete;        //count of puyos when formed combo to delete
    int leftPuyo, rightPuyo;            //The two puyos generate to be next are stored in leftPuyo and rightPuyo
    int delay, puyosJoint, puyosRemoved;//puyosJoint is number of joint puyos(single piece) generated
    //number of removed puyos by forming combo
    int animation;        //to build the pixel by pixel animation (movement of generated puyos)

    boolean isPuyoDropped;    //generated puyo isPuyoDropped the bottom or in movement
    boolean isGameStarted;
    boolean isGameLost;


    public Board(int length, int rows, int columns) {
        puyoSize = length;        //length of puyos is set by the Game class where it is calculated and sent here
        rowsNumber = rows;
        columnsNumber = columns;

        initialize();
        puyoImages = ImageUtils.loadImages();
        createPuyosAndPutOnTop();                //to start the generating puyos

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                //move puyos left if each puyo not isPuyoDropped to ground
                if (event.getKeyCode() == KeyEvent.VK_LEFT && !isPuyoDropped) {
                    moveLeft();
                } else if (event.getKeyCode() == KeyEvent.VK_RIGHT && !isPuyoDropped) {
                    moveRight();
                } else if (event.getKeyCode() == KeyEvent.VK_UP) {
                    if (!isPuyoDropped) {
                        rotate();
                    }
                } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                    moveDown();
                }
            }
        });

        setFocusable(true);
    }

    /**
     * Init main state
     */
    public void initialize() {
        puyosTable = new int[rowsNumber][columnsNumber];
        rotation = 1;
        isPuyoDropped = true;
        puyosToDelete = 0;
        isGameStarted = false;
        isGameLost = false;
        leftPuyo = 0;
        rightPuyo = 0;
        delay = 0;
        puyosJoint = -1;
        puyosRemoved = 0;
        animation = 0;
        random = new Random();
        timer = new Timer(1000, this);    //generates action event for each 1075 milli seconds when timer is started
        timer.setInitialDelay(0);    //generates first event after 0 ms when timer starts
        timerDestroy = new Timer(1000, this);
        timerDropUndestroyed = new Timer(500, this);
        timerAnimation = new Timer(50, this);
        timerAnimation.start();            //starting the timer

        if (!isGameStarted) {
            setDelays(delay);
            timer.start();
            isGameStarted = true;
        }
        if (isGameLost) {
            initialize();
            createPuyosAndPutOnTop();
            isGameStarted = false;
        }
        repaint();
    }

    public void setDelays(int delay) {
        timer.setDelay(1075 - delay);
        timerAnimation.setDelay(52 - delay);
        timerAnimation.restart();
    }

    /**
     * Generate Puyos at the top of the game window
     */
    public void createPuyosAndPutOnTop() {
        //Checking Top of the game window is occupied by any puyos
        //if occupied then game is over
        int p;
        if (columnsNumber % 2 == 0) {
            p = columnsNumber / 2 - 1;
        } else {
            p = columnsNumber / 2;
        }
        if (puyosTable[0][p] == 0 && puyosTable[1][p] == 0) {
            puyosTable[0][p] = leftPuyo;        //leftPuyo and rightPuyo are randomly generated Puyos
            puyosTable[1][p] = rightPuyo;
        } else {
            timer.stop();
            isGameLost = true;        //game is over
            return;
        }
        int randomInt;                    //Generating puyos randomly for the next fall
        //Odd numbers 1,3,5,7(for 4 colors) are used for generating puyos which are in movement
        //Even numbers 2,4,6,6 are used for fallen puyos on bottom of the window
        while ((randomInt = random.nextInt(8)) % 2 == 0) ;
        leftPuyo = randomInt;
        while ((randomInt = random.nextInt(8)) % 2 == 0) ;
        rightPuyo = randomInt;
        puyosJoint++;
        rotation = 1;
    }

    public void actionPerformed(ActionEvent event) {
        //If Event is generated by the timer object
        if (event.getSource() == timer) {
            movePuyos();
        } else if (event.getSource() == timerDestroy) { //If Event is generated by the timerDestroy object
            removeComboPuyos();
        } else if (event.getSource() == timerDropUndestroyed) {
            fillFreeNodes();
            timerDropUndestroyed.stop();
        }
        //timerDestroy and timerDropUndestroyed are used for make delay b/w erasing puyos and filling vacated places by puyos
        repaint();
    }

    /**
     * Move puyos down
     */
    public void movePuyos() {
        int flag = 0;
        for (int i = rowsNumber - 1; i >= 0; i--)
            for (int j = 0; j < columnsNumber; j++)
                if (puyosTable[i][j] % 2 == 1) {
                    if (i == rowsNumber - 1) {  //for each moving puyo ie. on last row 12
                        puyosTable[i][j] += 1;  //When generated puyo isPuyoDropped the ground ie. 12 row
                        isPuyoDropped = true;   //then make it as fallen puyo by making it as even number
                                                //isPuyoDropped the ground
                    } else if (puyosTable[i + 1][j] == 0) {          //if the next row is empty
                        puyosTable[i + 1][j] = puyosTable[i][j];    //for each generated puyo in moment increase the row number
                        puyosTable[i][j] = 0;
                        flag = 1;            //to build the movement for that puyo
                    } else {
                        puyosTable[i][j] += 1;       //If the next row contain any puyo then stop the movement
                        isPuyoDropped = true;        //of puyo by making it even
                    }
                    animation = 0;            //for pixel by pixel animation of puyos here animation starts
                }

        //if flag is not set mean that there is no puyo is in moment
        if (flag == 0) {
            removeComboPuyos();
        }
    }

    public void removeComboPuyos() {
        int flag = 0;
        for (int i = 0; i < rowsNumber; i++)
            for (int j = 0; j < columnsNumber; j++)
                if (puyosTable[i][j] > 0) {           //for all color puyos
                    puyosToDelete = 1;
                    combo = new PointNode(i, j);   //create a node for puyo
                    checkForCombo(i, j);           //check that node attached to the combo puyos of same color
                    if (puyosToDelete >= 4) {      //if combo forms
                        removeAllCombos();         //remove the puyos which form combo
                        flag = 1;
                        ///////////////////////////////////////
                        //I made change here to remove the bug in this revised game (by moving 4 lines of code to the below if block)
                        //ie. erasing all the chain combos formed at a time will be removed at a time.
                        ///////////////////////////////////////
                    }
                }
        if (flag == 1) {
            timer.stop();      //stop the generating puyos
            timerDestroy.start();     //erase puyos if there is any other form combo with delay
            timerDropUndestroyed.start();     //fill vacated places of erased puyos with remain by the law of gravity with delay
            return;
        }
        timerDestroy.stop();            //If there is no puyos form combo then stop timer for erasing puyos
        createPuyosAndPutOnTop();        //start generating puyos
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void checkForCombo(int x, int y) {
        //check for the same color puyo at the right side of
        if (y < columnsNumber - 1 && puyosTable[x][y] == puyosTable[x][y + 1] && !existsCombo(x, y + 1)) {
            puyosToDelete++;                //current puyo which is not already added to the current combo
            addCombo(x, y + 1);        //If there is then add that to the current combo
            checkForCombo(x, y + 1);    //Then check this node can connected to any same color puyo
        }
        if (x < rowsNumber - 1 && puyosTable[x][y] == puyosTable[x + 1][y] && !existsCombo(x + 1, y)) {
            puyosToDelete++;
            addCombo(x + 1, y);        //check at the down side
            checkForCombo(x + 1, y);
        }
        if (y > 0 && puyosTable[x][y] == puyosTable[x][y - 1] && !existsCombo(x, y - 1)) {
            puyosToDelete++;
            addCombo(x, y - 1);        //check at the left side
            checkForCombo(x, y - 1);
        }
        //I found a bug here and i rectified it by adding below if block
        //Before iam not checked up side
        //but if below type of chain combo formed by puyos
        //			* *
        //          ***
        //then up right puyo cannot be added to the combo to remove
        if (x > 0 && puyosTable[x][y] == puyosTable[x - 1][y] && !existsCombo(x - 1, y)) {
            puyosToDelete++;
            addCombo(x - 1, y);        //check at the up side
            checkForCombo(x - 1, y);
        }
    }

    /**
     * Adds another node to the present combo
     */
    public void addCombo(int x, int y) {
        combo.setNext(new PointNode(x, y));
        combo.getNext().setPrev(combo);//It is totally the linked list concept used here
        combo = combo.getNext();
    }

    /**
     * Compares with the all the nodes in present combo that it is already exists or not
     */
    public boolean existsCombo(int x, int y) {
        PointNode n = combo;
        while (n != null) {
            if (n.getX() == x && n.getY() == y) {
                return true;
            }
            n = n.getPrev();
        }
        return false;
    }

    public void removeAllCombos() {
        PointNode n = combo;
        while (n != null) {
            puyosTable[n.getX()][n.getY()] = 0;    //removing puyos which are in combo
            n = n.getPrev();
        }
        puyosRemoved += puyosToDelete;
    }

    /**
     * Fills with the puyos vacated places formed by removed puyos
     */
    public void fillFreeNodes() {
        for (int i = rowsNumber - 2; i >= 0; i--)
            for (int j = 0; j < columnsNumber; j++)
                if (puyosTable[i][j] > 0)                //for all puyos
                {
                    int k;
                    for (k = i + 1; k <= rowsNumber - 1; k++)
                        if (puyosTable[k][j] > 0) {           //any puyo exist below the current puyo up to the ground
                            puyosTable[k - 1][j] = puyosTable[i][j]; //then move on to it
                            if (i != k - 1) {
                                puyosTable[i][j] = 0;
                            }
                            break;
                        } else if (k == rowsNumber - 1) {      //if no puyo exist below the current puyo up to the ground
                            puyosTable[rowsNumber - 1][j] = puyosTable[i][j];    //then move on to the ground
                            if (i != rowsNumber - 1) {
                                puyosTable[i][j] = 0;
                            }
                        }
                }
    }

    /**
     * Moves the coming down puyos one step left
     */
    public void moveLeft() {
        for (int i = 0; i < rowsNumber; i++)
            for (int j = 0; j < columnsNumber; j++)
                if (puyosTable[i][j] > 0 && puyosTable[i][j] % 2 == 1 && j > 0) { //for all the coming down puyos which are not in first column(or left column)
                    if (j < columnsNumber - 1 && puyosTable[i][j + 1] % 2 == 1 && puyosTable[i][j - 1] == 0) {   //if two puyo are in horizontal
                        //if there is no puyo in left
                        puyosTable[i][j - 1] = puyosTable[i][j];        //move two puyo to one step left
                        puyosTable[i][j] = puyosTable[i][j + 1];
                        puyosTable[i][j + 1] = 0;
                    } else if (puyosTable[i][j - 1] == 0 && puyosTable[i + 1][j - 1] == 0) { //if two puyo are in vertical & there is no puyos in left
                        puyosTable[i][j - 1] = puyosTable[i][j];
                        puyosTable[i + 1][j - 1] = puyosTable[i + 1][j];        //move two puyo to one step left
                        puyosTable[i][j] = 0;
                        puyosTable[i + 1][j] = 0;
                    }
                    return;
                }
    }

    /**
     * Moves the coming down puyos one step right
     */
    public void moveRight() {
        for (int i = 0; i < rowsNumber; i++)
            for (int j = columnsNumber - 1; j >= 0; j--)
                if (puyosTable[i][j] > 0 && puyosTable[i][j] % 2 == 1 && j < columnsNumber - 1) { //for all the coming down puyos which are not in last column(or right column)
                    if (j > 0 && puyosTable[i][j - 1] % 2 == 1 && puyosTable[i][j + 1] == 0) { //if two puyo are in horizontal
                        //if there is no puyo in right
                        puyosTable[i][j + 1] = puyosTable[i][j];
                        puyosTable[i][j] = puyosTable[i][j - 1];                //move two puyo to one step right
                        puyosTable[i][j - 1] = 0;
                    } else if (puyosTable[i][j + 1] == 0 && puyosTable[i + 1][j + 1] == 0) { //if two puyo are in vertical & there is no puyos in right
                        puyosTable[i][j + 1] = puyosTable[i][j];
                        puyosTable[i + 1][j + 1] = puyosTable[i + 1][j];            //move two puyo to one step right
                        puyosTable[i][j] = 0;
                        puyosTable[i + 1][j] = 0;
                    }
                    return;
                }
    }

    /**
     * Rotates the coming down puyos in clock wise direction by 90 degrees
     */
    public void rotate() {
        for (int i = 0; i < rowsNumber; i++)
            for (int j = 0; j < columnsNumber; j++)
                if (puyosTable[i][j] > 0 && puyosTable[i][j] % 2 == 1) { //for all the coming down puyos
                    //always consider the puyo left and top for horizontal and vertical positions as present
                    //because checking is taking from top to bottom
                    //Two puyos can make only four different positions rotating 90 degrees each time
                    if (rotation == 1)    //first position=vertical
                    {            //moving down puyo to the left of first puyo makes 90 degrees rotation to get second position
                        if (j > 0 && puyosTable[i][j - 1] == 0) { //is left side is empty or not
                            puyosTable[i][j - 1] = puyosTable[i + 1][j];
                            puyosTable[i + 1][j] = 0;        //If it is empty location move there
                            rotation = 2;                //change to second position
                        } else if (j < columnsNumber - 1 && puyosTable[i][j + 1] == 0) { //is left side is not empty, chk for right position
                            puyosTable[i][j + 1] = puyosTable[i][j];//move present puyo to the right
                            puyosTable[i][j] = puyosTable[i + 1][j];//move down puyo to the present location
                            puyosTable[i + 1][j] = 0;
                            rotation = 2;                //change to second position
                        }
                        //second position=horizontal
                    } else if (rotation == 2 && i > 1) { //moving present puyo to the up of right puyo makes 90 degrees rotation to get third position
                        puyosTable[i - 1][j + 1] = puyosTable[i][j];//move present puyo to the up of the right puyo
                        puyosTable[i][j] = 0;
                        rotation = 3;                    //change to third position
                        //third position=vertical (invert to the first position in two puyos positions)
                    } else if (rotation == 3) {          //moving present puyo to the up of right puyo makes 90 degrees rotation to get fourth position
                        if (j < columnsNumber - 1 && puyosTable[i + 1][j + 1] == 0) { //is right side of the down puyo is empty or not
                            puyosTable[i + 1][j + 1] = puyosTable[i][j];    //If it is empty location move there
                            puyosTable[i][j] = 0;
                            rotation = 4;                        //change to fourth position
                        } else if (j > 0 && puyosTable[i + 1][j - 1] == 0) { //is left side of the down puyo is empty or not
                            puyosTable[i + 1][j - 1] = puyosTable[i + 1][j];    //move down puyo to the left
                            puyosTable[i + 1][j] = puyosTable[i][j];        //move present puyo to the down
                            puyosTable[i][j] = 0;
                            rotation = 4;                        //change to fourth position
                        }
                        //fourth position=horizontal (invert to the second position in two puyos positions)
                    } else if (rotation == 4 && i < rowsNumber - 1) { //moving right puyo to the down of the current puyo makes 90 degrees rotation to get first position
                        if (puyosTable[i + 1][j] == 0) { //is down position is empty or not
                            puyosTable[i + 1][j] = puyosTable[i][j + 1];//move right puyo to the down of the present puyo
                            puyosTable[i][j + 1] = 0;
                            rotation = 1;                    //change to first position
                        }
                    }
                    return;
                }
    }

    /**
     * Moves generated puyos by one step down
     */
    public void moveDown() {
        for (int i = rowsNumber - 1; i >= 0; i--)
            for (int j = 0; j < columnsNumber; j++)
                if (puyosTable[i][j] % 2 == 1) { //For all moving puyos
                    if (i == rowsNumber - 1) {  //if puyo is in last row
                        puyosTable[i][j] = puyosTable[i][j] + 1;//making it as grounded
                        isPuyoDropped = true;
                    } else if (puyosTable[i + 1][j] > 0 && puyosTable[i + 1][j] % 2 == 0) { //if next row of puyo contains another puyo
                        puyosTable[i][j] = puyosTable[i][j] + 1;//then stop the current puyo at the current position
                        isPuyoDropped = true;
                    } else {
                        puyosTable[i + 1][j] = puyosTable[i][j];//Move present puyo one step down
                        puyosTable[i][j] = 0;
                    }
                }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, puyoSize * columnsNumber, puyoSize * rowsNumber);//background fill with white color

        for (int i = 0; i < rowsNumber; i++) {
            for (int j = 0; j < columnsNumber; j++) {
                if (puyosTable[i][j] > 0) { //For all the puyos exists presently
                    int k = puyosTable[i][j];
                    if (k % 2 == 0) { //if puyo value is even then simply display it
                        k = k / 2;
                        g.drawImage(puyoImages[k - 1], j * puyoSize, i * puyoSize, puyoSize, puyoSize, null);
                    } else {        //if puyo value is odd then add animation to it
                        k = k / 2 + 1;
                        g.drawImage(puyoImages[k - 1], j * puyoSize, (i - 1) * puyoSize + animation, puyoSize, puyoSize, null);
                        animation += 2;
                        if (animation >= puyoSize) {
                            animation = puyoSize;
                        }
                        if (isPuyoDropped && i == 2) {
                            isPuyoDropped = false;
                        }
                    }
                }
            }
        }
    }
}
