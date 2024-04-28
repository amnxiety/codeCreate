package com.example.new_bounce;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Ball {
    private static final double SIZE_INCREMENT = 3.15;
    private static final double BORDER_RADIUS = 250;
    private static final double SPEED_INCREMENT = 0.1;
    private final Text collisionText;
    private final MusicPlayer musicPlayer = new MusicPlayer("src/main/java/com/example/new_bounce/midi/sad.mid");
    //    private static final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.VIOLET};
    private final List<Circle> tail = new ArrayList<>();
    private final List<Circle> layers = new ArrayList<>();
    private final Circle circle;
    private final StaticConstants staticConstants = new StaticConstants();
    private final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.VIOLET};
    private final Color color;
    private final Color targetBallColor = Color.RED;
    public double x;
    public double y;
    public double radius;
    public int collisionCount = 0;
    ObservableList<Node> children;
    private double dx;
    private double dy;
    private Line lineToBorder;
    public double distanceToCenterPublic;

    public Ball(double x, double y, double dx, double dy, double radius, Color color, Text collisionText, ObservableList<Node> children) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.radius = radius;
        this.color = color;
        this.collisionText = collisionText;
        this.circle = createCircle();
        this.children = children;
        children.add(circle);

    }

    public void updatePosition(double gravity, List<Border> allBorder) {

        for(Border border: allBorder){
            border.getCircle().setRadius(border.getCircle().getRadius() - 2);
        }

        dy += gravity;
        radius = circle.getRadius() + 10;
        x += dx;
        y += dy;

        if(allBorder.isEmpty()){
            BouncingBallFX.flag = true;
            allBorder.add(new Border(1500 / 2.0, 1400 / 2.0, 26000, staticConstants.rgbColors.get(1)));
        }

        Border border = allBorder.getFirst();

        // Check for collisions with border
        double dxToCenter = x - border.getCircle().getCenterX();
        double dyToCenter = y - border.getCircle().getCenterY();
        double distanceToCenter = Math.sqrt(dxToCenter * dxToCenter + dyToCenter * dyToCenter);

        distanceToCenterPublic = distanceToCenter;

        if (distanceToCenter + radius >= border.getCircle().getRadius()) {

            musicPlayer.playNotesWhenAsked();


            double angleToCenter = Math.atan2(dyToCenter, dxToCenter);
            double incidenceAngle = Math.atan2(dy, dx);
            double reflectionAngle = 2 * angleToCenter - incidenceAngle + Math.PI;

            double speed = Math.sqrt(dx * dx + dy * dy);
            dx = Math.cos(reflectionAngle) * (speed + SPEED_INCREMENT);
            dy = Math.sin(reflectionAngle) * (speed + SPEED_INCREMENT);

            collisionCount++;
            collisionText.setText("Collisions: " + collisionCount);


            children.remove(allBorder.getFirst().getCircle());
            allBorder.removeFirst();
        }

        if (distanceToCenter + radius >= border.getCircle().getRadius()) {
            double normX = dxToCenter / distanceToCenter;
            double normY = dyToCenter / distanceToCenter;
            x = border.getCircle().getCenterX() + normX * (border.getCircle().getRadius() - radius);
            y = border.getCircle().getCenterY() + normY * (border.getCircle().getRadius() - radius);
        }
        updateCirclePosition();
    }

    public void updateTail(ObservableList<Node> children) {
        // Create a new circle for the tail piece
        radius = circle.getRadius();
        Circle newTailPiece = new Circle(x, y, radius * 0.9); // Adjust the size of the tail pieces as needed
        newTailPiece.setFill(Color.GRAY); // Set the color of the tail piece

        // Limit the number of tail pieces
        if (tail.size() > 20) { // Adjust the number of tail pieces as needed
            children.remove(tail.remove(0)); // Remove the oldest tail piece from the root pane and the tail list
        }

        // Set transparency based on position in the tail
        double transparency = 1.0;
        for (int i = tail.size(); i > 0; i--) {
            tail.get(i - 1).setOpacity(transparency);
            transparency *= 0.8; // Adjust the rate of transparency decrease as needed
        }

        // Add the new tail piece to the root pane and the tail list
        children.add(0, newTailPiece); // Add the tail piece before the ball
        tail.add(newTailPiece);
    }

    private void updateCirclePosition() {
        circle.setCenterX(x);
        circle.setCenterY(y);

        for (Circle layer : layers) {
            layer.setCenterX(x);
            layer.setCenterY(y);
        }
    }

    public void addLayerToCircle(Color color) {
        circle.setRadius(circle.getRadius() + 30);
        Circle layer = createCircle();
        layer.setFill(null);
        layer.setStrokeWidth(30);
        layer.setStroke(color);
        layer.setRadius(circle.getRadius());
        layers.add(layer);
        children.add(layer);
        //Implement logic to add layer for the 30 size we incremented,
        // that 30 incremented size would be last borders color
        //and when called twice, it should have color of 1st border and 2nd respectively as layers

    }

    public Circle createCircle() {
        Circle circle = new Circle(x, y, radius);
        circle.setFill(color);
        return circle;
    }

}
