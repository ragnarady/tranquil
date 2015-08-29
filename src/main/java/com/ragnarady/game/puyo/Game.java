package main.java.com.ragnarady.game.puyo;

import javax.swing.*;
import java.awt.*;

/**
 * Main game window w/ {@link main.java.com.ragnarady.game.puyo.Board} component.
 */
public class Game extends JFrame {

    public static final String PUYO_GAME = "PuyoGame";

    Board gameBoard;

    public Game() {
        super(PUYO_GAME);

        int columnsNumber = 6;
        int rowsNumber = 12;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;

        int nodeSize = screenWidth / (4 * columnsNumber);

        gameBoard = new Board(nodeSize, rowsNumber, columnsNumber);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        getContentPane().add(gameBoard);

        setResizable(false);

        setPreferredSize(new Dimension(nodeSize * columnsNumber + 5, nodeSize * rowsNumber + 25));

        pack();

        setLocationRelativeTo(null);

        setVisible(true);
    }

    public static void main(String args[]) {
        System.out.println("Starting PuyoGame...");
        new Game();
    }
}

