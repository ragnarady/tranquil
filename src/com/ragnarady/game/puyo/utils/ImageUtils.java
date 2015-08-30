package com.ragnarady.game.puyo.utils;

import java.awt.*;

public class ImageUtils {

    public static final String SRC_RES_IMAGES = "res\\images";
   
    /**
     * Loads Puyo images resource
     */
   static public Image[] loadImages() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return new Image[] {
        	toolkit.getImage(SRC_RES_IMAGES + "\\puyo_blue.png"),
            toolkit.getImage(SRC_RES_IMAGES + "\\puyo_red.png"),
            toolkit.getImage(SRC_RES_IMAGES + "\\puyo_yellow.png"),
            toolkit.getImage(SRC_RES_IMAGES + "\\puyo_green.png")
        };
    }
}
