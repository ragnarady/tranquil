package com.ragnarady.game.puyo;

import javax.swing.*;
import java.awt.*;

/**
 * Main game window w/ {@link com.ragnarady.game.puyo.Board} component.
 */
public class Game extends JFrame {

    public static final String PUYO_GAME = "PuyoGame";
    public static final String PUYO_RUN = "PuyoGame is started";
    public static final int COLUMNS_NUMBER = 6;
    public static final int ROWS_NUMBER = 12;

    Board gameBoard;

    public Game() {
        super(PUYO_GAME);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;

        int nodeSize = screenWidth / (4 * COLUMNS_NUMBER);

        gameBoard = new Board(nodeSize, ROWS_NUMBER, COLUMNS_NUMBER);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        getContentPane().add(gameBoard);

        setResizable(false);

        pack();

        setLocationRelativeTo(null);

        setVisible(true);
    }

    public static void main(String args[]) {
        System.out.println(PUYO_RUN);
        new Game();
    }
}

