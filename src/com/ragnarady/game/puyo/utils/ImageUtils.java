package com.ragnarady.game.puyo.utils;

import java.awt.*;

/**
 * Created by Arlabunakty on 8/29/2015.
 */
public class ImageUtils {

    public static final String SRC_MAIN_RES_IMAGES = "src\\main\\res\\images";

    /**
     * Loads images from
     * @return
     */
    static public Image[] loadImages() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return new Image[] {
            toolkit.getImage(SRC_MAIN_RES_IMAGES + "\\puyo_blue.png"),
            toolkit.getImage(SRC_MAIN_RES_IMAGES + "\\puyo_red.png"),
            toolkit.getImage(SRC_MAIN_RES_IMAGES + "\\puyo_yellow.png"),
            toolkit.getImage(SRC_MAIN_RES_IMAGES + "\\puyo_green.png")
        };
    }
}
