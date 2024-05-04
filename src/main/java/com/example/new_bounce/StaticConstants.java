package com.example.new_bounce;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class StaticConstants {

    // https://coolors.co/palettes/popular/violet
    public ArrayList<Color> rgbColors;

    StaticConstants() {
        rgbColors = new ArrayList<>();
//        addPurpleColors();
//        addPallateColors();
//        addDarkPallateColors();
        addPallateColorsAllBlue();
    }

    private void addPurpleColors() {
        rgbColors.add(Color.rgb(70, 24, 115));
        rgbColors.add(Color.rgb(88, 20, 142));
        rgbColors.add(Color.rgb(105, 16, 168));
        rgbColors.add(Color.rgb(140, 7, 221));
        rgbColors.add(Color.rgb(159, 33, 227));
        rgbColors.add(Color.rgb(179, 51, 233));
        rgbColors.add(Color.rgb(203, 93, 241));
        rgbColors.add(Color.rgb(180, 95, 205));
        rgbColors.add(Color.rgb(165, 100, 211));
        rgbColors.add(Color.rgb(182, 110, 232));
        rgbColors.add(Color.rgb(200, 121, 255));
        rgbColors.add(Color.rgb(214, 137, 255));
        rgbColors.add(Color.rgb(228, 152, 255));
        rgbColors.add(Color.rgb(242, 168, 255));
        rgbColors.add(Color.rgb(255, 183, 255));
        rgbColors.add(Color.rgb(255, 196, 255));
        rgbColors.add(Color.rgb(255, 201, 255));
        rgbColors.add(Color.rgb(255, 206, 255));
        rgbColors.add(Color.rgb(255, 210, 255));
        rgbColors.add(Color.rgb(255, 215, 255));
        rgbColors.add(Color.rgb(255, 220, 255));
        rgbColors.add(Color.rgb(255, 225, 255));
    }

    private void addPallateColors() {
        rgbColors.add(Color.rgb(178, 34, 34));
        rgbColors.add(Color.rgb(255, 0, 0));
        rgbColors.add(Color.rgb(255, 99, 71));
        rgbColors.add(Color.rgb(255, 69, 0));
        rgbColors.add(Color.rgb(255, 140, 0));
        rgbColors.add(Color.rgb(184, 134, 11));
        rgbColors.add(Color.rgb(255, 215, 0));
        rgbColors.add(Color.rgb(255, 255, 0));
        rgbColors.add(Color.rgb(154, 205, 50));
        rgbColors.add(Color.rgb(173, 255, 47));
        rgbColors.add(Color.rgb(0, 128, 0));
        rgbColors.add(Color.rgb(0, 255, 127));
        rgbColors.add(Color.rgb(64, 224, 208));
        rgbColors.add(Color.rgb(72, 209, 204));
        rgbColors.add(Color.rgb(0, 255, 255));
        rgbColors.add(Color.rgb(0, 191, 255));
        rgbColors.add(Color.rgb(65, 105, 225));
        rgbColors.add(Color.rgb(0, 0, 139));
        rgbColors.add(Color.rgb(0, 0, 255));
        rgbColors.add(Color.rgb(138, 43, 226));
        rgbColors.add(Color.rgb(255, 105, 80));
        rgbColors.add(Color.rgb(240, 128, 128));
    }
    private void addDarkPallateColors() {
        rgbColors.add(Color.BLACK);
        rgbColors.add(Color.rgb(16, 0, 10));
        rgbColors.add(Color.rgb(32, 0, 21));
        rgbColors.add(Color.rgb(47, 0, 31));
        rgbColors.add(Color.rgb(63, 0, 42));
        rgbColors.add(Color.rgb(79, 1, 52));
        rgbColors.add(Color.rgb(95, 1, 62));
        rgbColors.add(Color.rgb(111, 1, 73));
        rgbColors.add(Color.rgb(126, 1, 83));
        rgbColors.add(Color.rgb(142, 1, 94));
        rgbColors.add(Color.rgb(19, 1, 47));
        rgbColors.add(Color.rgb(28, 2, 70));
        rgbColors.add(Color.rgb(37, 2, 93));
        rgbColors.add(Color.rgb(47, 3, 117));
        rgbColors.add(Color.rgb(56, 4, 140));
        rgbColors.add(Color.rgb(65, 4, 163));
        rgbColors.add(Color.rgb(74, 5, 186));
        rgbColors.add(Color.rgb(84, 5, 210));
    }

    private void addPallateColorsAllBlue() {
        rgbColors.add(Color.BLACK);
        rgbColors.add(Color.rgb(9, 1, 23));
        rgbColors.add(Color.rgb(19, 1, 47));
        rgbColors.add(Color.rgb(28, 2, 70));
        rgbColors.add(Color.rgb(32, 4, 80));
        rgbColors.add(Color.rgb(37, 2, 93));
        rgbColors.add(Color.rgb(41, 5, 101));
        rgbColors.add(Color.rgb(47, 3, 117));
        rgbColors.add(Color.rgb(56, 4, 140));
        rgbColors.add(Color.rgb(49, 6, 121));
        rgbColors.add(Color.rgb(57, 7, 141));
        rgbColors.add(Color.rgb(65, 8, 161));
        rgbColors.add(Color.rgb(65, 4, 163));
        rgbColors.add(Color.rgb(74, 5, 186));
        rgbColors.add(Color.rgb(84, 5, 210));
        rgbColors.add(Color.rgb(73, 9, 181));
    }

}
