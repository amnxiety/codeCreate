package com.example.new_bounce;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class StaticConstants {

    // https://coolors.co/palettes/popular/violet
    public ArrayList<Color> rgbColors;

    StaticConstants() {
        rgbColors = new ArrayList<>();
//        addPurpleColors();
        addPallateColors();
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

}
