package com.example.new_bounce;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Border {
    private final Circle circle;
    public Color currentColor;

    public Border(double centerX, double centerY, double radius, Color color) {
        this.circle = new Circle(centerX, centerY, radius);
        circle.setFill(null);
        circle.setStrokeWidth(100);
        this.currentColor = color;
        circle.setStroke(currentColor);
        this.circle.setStrokeWidth(30);
    }

    public Circle getCircle() {
        return circle;
    }

    public void setColor(Color color) {
        this.currentColor = color;
        circle.setStroke(color);
    }
}