package com.ragnarady.game.puyo.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by Arlabunakty on 8/29/2015.
 */
public class ImageUtils {

    public static final String IMAGES_FOLDER_NAME = "images";

    /**
     * Loads images from resources folder
     */
    static public Image[] loadImages() {
        try {
            return new Image[]{
                loadImage(IMAGES_FOLDER_NAME + "/puyo_blue.png"),
                loadImage(IMAGES_FOLDER_NAME + "/puyo_red.png"),
                loadImage(IMAGES_FOLDER_NAME + "/puyo_yellow.png"),
                loadImage(IMAGES_FOLDER_NAME + "/puyo_green.png")
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(ImageUtils.class.getClassLoader().getResource(path));
    }
}
