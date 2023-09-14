package com.metropolitan.cs103pznemanjavilic4050;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import java.io.IOException;
import java.util.*;


public class HelloApplication extends Application {

    private static final double MIN_DISTANCE = 50.0;
    private Line line = null; // Line to be drawn between circles
    private CircleNode startCircle = null; // Start circle of the line
    private Map<CircleNode, List<Line>> circleLines = new HashMap<>();
    private CircleNode head = null;
    private CircleNode tail = null;
    private HelloController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        BorderPane root = fxmlLoader.load();
        Scene scene = new Scene(root, 1200, 800);

        controller = fxmlLoader.getController();


        // Creating the mouse event handler
        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            private int letterCounter = 65; // ASCII value of 'A'
            @Override
            public void handle(MouseEvent e) {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (letterCounter > 90) { // ASCII value of 'Z'
                        return; // Stop adding circles after 'Z'
                    }
                    // Check if the new circle overlaps with any existing circles
                    if (!checkCircleOverlap(controller.myPane, e.getX(), e.getY())) {
                        /// Drawing a Circle
                        Circle circle = new Circle();

                        // Setting the position of the circle
                        circle.setCenterX(e.getX());
                        circle.setCenterY(e.getY());

                        // Setting the radius of the circle
                        circle.setRadius(20.0f);

                        // Setting the color of the circle
                        circle.setFill(Color.web("#7d101e"));

                        // Setting the stroke width of the circle
                        circle.setStrokeWidth(20);

                        // Adding the circle to the controller.myPane pane
                        controller.myPane.getChildren().add(circle);

                        // Adding text to the center of the circle
                        Text text = new Text(Character.toString((char) letterCounter));
                        text.setX(circle.getCenterX() - text.getLayoutBounds().getWidth() / 2);
                        text.setY(circle.getCenterY() + text.getLayoutBounds().getHeight() / 4);
                        controller.myPane.getChildren().add(text);

                        letterCounter++; // Increment the letter counter

                        // Add the circle and text to the doubly linked list
                        CircleNode newNode = new CircleNode();
                        newNode.setCircle(circle);
                        newNode.setText(text);
                        newNode.setPrev(tail);
                        newNode.setNext(null);

                        if (tail != null) {
                            tail.setNext(newNode);
                        }
                        tail = newNode;

                        if (head == null) {
                            head = newNode;
                        }
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    CircleNode node = getCircleNodeAt(e.getX(), e.getY());
                    if (node != null) {
                        controller.myPane.getChildren().remove(node.getCircle());
                        controller.myPane.getChildren().remove(node.getText());

                        // Remove the node from the doubly linked list
                        if (node.getPrev() != null) {
                            node.getPrev().setNext(node.getNext());
                        } else {
                            head = node.getNext();
                        }
                        if (node.getNext() != null) {
                            node.getNext().setPrev(node.getPrev());
                        } else {
                            tail = node.getPrev();
                        }
                        removeLinesConnectedToNode(node);
                    }else {
                        // Remove the line if clicked on it
                        Line clickedLine = getLineAt(controller.myPane, e.getX(), e.getY());
                        if (clickedLine != null) {
                            controller.myPane.getChildren().remove(clickedLine);
                            removeLineFromMap(clickedLine);
                        }
                    }
                }
            }
        };

        // Registering the event filter
        controller.myPane.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

        // Registering the drag event handlers
        controller.myPane.setOnMousePressed(e -> {
            CircleNode node = getCircleNodeAt(e.getX(), e.getY());
            if (node != null) {
                startCircle = node;
                line = new Line(node.getCircle().getCenterX(), node.getCircle().getCenterY(), node.getCircle().getCenterX(), node.getCircle().getCenterY());
                line.setStroke(Color.web("#85708d"));
                controller.myPane.getChildren().add(0, line); // Add the line to the beginning of the children list
            }
        });

        controller.myPane.setOnMouseDragged(e -> {
            if (line != null) {
                line.setEndX(e.getX());
                line.setEndY(e.getY());
            }
        });

        controller.myPane.setOnMouseReleased(e -> {
            if (line != null) {
                CircleNode node = getCircleNodeAt(e.getX(), e.getY());
                if (node != null && node != startCircle) {
                    line.setEndX(node.getCircle().getCenterX());
                    line.setEndY(node.getCircle().getCenterY());
                    // Add the line to the map for both nodes
                    circleLines.computeIfAbsent(startCircle, k -> new ArrayList<>()).add(line);
                    circleLines.computeIfAbsent(node, k -> new ArrayList<>()).add(line);
                } else {
                    controller.myPane.getChildren().remove(line);
                }
                line = null;
            }
        });

        controller.dugme.setOnAction(e -> {
            CircleNode startNode = getNodeByName(controller.sourceTextField.getText());
            CircleNode endNode = getNodeByName(controller.destinationTextField.getText());

            if (startNode != null && endNode != null) {
                List<CircleNode> path = dijkstra(startNode, endNode);

                if (path != null) {
                    for (CircleNode node : path) {
                        node.getCircle().setFill(Color.web("#26781c"));
                    }

                    for (int i = 0; i < path.size() - 1; i++) {
                        CircleNode node1 = path.get(i);
                        CircleNode node2 = path.get(i + 1);
                        for (Line line : circleLines.get(node1)) {
                            if (circleLines.get(node2).contains(line)) {
                                line.setStroke(Color.web("#34b7eb"));
                                break;
                            }
                        }
                    }
                }
            }
        });

        stage.setTitle("Metro");
        stage.setScene(scene);
        stage.show();
    }

    // Check if the new circle overlaps with any existing circles
    private boolean checkCircleOverlap(Pane pane, double x, double y) {
        CircleNode currentNode = head;
        while (currentNode != null) {
            Circle circle = currentNode.getCircle();
            double distance = Math.sqrt(Math.pow(circle.getCenterX() - x, 2) + Math.pow(circle.getCenterY() - y, 2));
            if (distance < MIN_DISTANCE) {
                return true;
            }
            currentNode = currentNode.getNext();
        }
        return false;
    }

    // Get the circle at the given position, if any
    private CircleNode getCircleNodeAt(double x, double y) {
        CircleNode currentNode = head;
        while (currentNode != null) {
            Circle circle = currentNode.getCircle();
            double distance = Math.sqrt(Math.pow(circle.getCenterX() - x, 2) + Math.pow(circle.getCenterY() - y, 2));
            if (distance < circle.getRadius()) {
                return currentNode;
            }
            currentNode = currentNode.getNext();
        }
        return null;
    }
    private Line getLineAt(Pane pane, double x, double y) {
        for (javafx.scene.Node node : pane.getChildren()) {
            if (node instanceof Line line) {
                double distance = pointToLineDistance(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY(), x, y);
                if (distance < 5) { // You can adjust this threshold as needed
                    return line;
                }
            }
        }
        return null;
    }

    private double pointToLineDistance(double x1, double y1, double x2, double y2, double x0, double y0) {
        double numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1);
        double denominator = Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
        return numerator / denominator;
    }
    private void printLinkedListAndLines() {
        CircleNode currentNode = head;
        while (currentNode != null) {
            System.out.println("Node: " + currentNode.getText().getText());
            currentNode = currentNode.getNext();
        }
        System.out.println("------------------------------------------------------------------");
        for (Map.Entry<CircleNode, List<Line>> entry : circleLines.entrySet()) {
            System.out.println("Node: " + entry.getKey().getText().getText());
            for (Line line : entry.getValue()) {
                System.out.println("Line: Start[" + line.getStartX() + ", " + line.getStartY() + "], End[" + line.getEndX() + ", " + line.getEndY() + "]");
            }
        }
    }
    public void removeLineFromMap(Line lineToRemove) {
        Iterator<Map.Entry<CircleNode, List<Line>>> iterator = circleLines.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<CircleNode, List<Line>> entry = iterator.next();
            // Check if the list of lines contains the line to remove
            if (entry.getValue().contains(lineToRemove)) {
                // If it does, remove the line from the list
                entry.getValue().remove(lineToRemove);
                // If the list is now empty, remove the entry from the HashMap
                if (entry.getValue().isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    public void removeLinesConnectedToNode(CircleNode nodeToRemove) {
        Iterator<Map.Entry<CircleNode, List<Line>>> iterator = circleLines.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<CircleNode, List<Line>> entry = iterator.next();
            List<Line> lines = entry.getValue();
            if (lines != null) {
                for (Iterator<Line> lineIterator = lines.iterator(); lineIterator.hasNext();) {
                    Line currentLine = lineIterator.next();
                    // Check if the start or end node of the current line is the same as the node to remove
                    if (currentLine.getStartX() == nodeToRemove.getCircle().getCenterX() && currentLine.getStartY() == nodeToRemove.getCircle().getCenterY() ||
                            currentLine.getEndX() == nodeToRemove.getCircle().getCenterX() && currentLine.getEndY() == nodeToRemove.getCircle().getCenterY()) {
                        lineIterator.remove();
                        controller.myPane.getChildren().remove(currentLine);
                    }
                }
                if (lines.isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    private List<CircleNode> dijkstra(CircleNode start, CircleNode end) {
        Map<CircleNode, Double> distances = new HashMap<>();
        Map<CircleNode, CircleNode> predecessors = new HashMap<>();
        PriorityQueue<CircleNode> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        distances.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            CircleNode currentNode = queue.poll();

            if (currentNode == end) {
                break;
            }

            for (Line line : circleLines.getOrDefault(currentNode, new ArrayList<>())) {
                CircleNode neighbor = getOtherNode(line, currentNode);
                double newDistance = distances.get(currentNode) + lineLength(line);

                if (!distances.containsKey(neighbor) || newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, currentNode);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (!predecessors.containsKey(end)) {
            return null;
        }

        LinkedList<CircleNode> path = new LinkedList<>();
        CircleNode node = end;
        while (node != null) {
            path.addFirst(node);
            node = predecessors.get(node);
        }

        return path;
    }

    private CircleNode getOtherNode(Line line, CircleNode node) {
        for (Map.Entry<CircleNode, List<Line>> entry : circleLines.entrySet()) {
            if (entry.getValue().contains(line) && entry.getKey() != node) {
                return entry.getKey();
            }
        }
        return null;
    }

    private double lineLength(Line line) {
        double dx = line.getStartX() - line.getEndX();
        double dy = line.getStartY() - line.getEndY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    private CircleNode getNodeByName(String name) {
        CircleNode currentNode = head;
        while (currentNode != null) {
            if (currentNode.getText().getText().equals(name)) {
                return currentNode;
            }
            currentNode = currentNode.getNext();
        }
        return null;
    }


    public static void main(String[] args) {
        launch();
    }
}