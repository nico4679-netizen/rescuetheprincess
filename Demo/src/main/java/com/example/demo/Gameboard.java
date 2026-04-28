package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Random;

public class Gameboard extends Application {

    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int SCENE_WIDTH = 800;
    private static final int SCENE_HEIGHT = 800;
    private static final int NUM_BOMBS = 5;

    enum CellType {
        GRASS, PLAYER, PRINCESS, BOMB, WALL
    }

    private CellType[][] matrix = new CellType[ROWS][COLS];

    private Image grassImg;
    private Image playerImg;
    private Image princessImg;
    private Image bombImg;
    private Image wallImg;

    @Override
    public void start(Stage stage) {

        loadImages();
        initMatrix();

        GridPane grid = new GridPane();
        grid.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);

        drawBoard(grid);

        BorderPane root = new BorderPane();
        root.setCenter(grid);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        stage.setTitle("Rescue the Princess");
        stage.setScene(scene);
        stage.show();
    }

    private void loadImages() {
        grassImg    = new Image(getClass().getResourceAsStream("/images/grass.png"));
        playerImg   = new Image(getClass().getResourceAsStream("/images/player.png"));
        princessImg = new Image(getClass().getResourceAsStream("/images/princess.png"));
        bombImg     = new Image(getClass().getResourceAsStream("/images/bomb.png"));
        wallImg     = new Image(getClass().getResourceAsStream("/images/wall.png"));
    }

    private void initMatrix() {
        // Step 1 - Fill everything with grass first
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                matrix[r][c] = CellType.GRASS;
            }
        }

        // Step 2 - Place walls on the perimeter
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                    matrix[r][c] = CellType.WALL;
                }
            }
        }

        // Step 3 - Place player at [1][1]
        matrix[1][1] = CellType.PLAYER;

        // Step 4 - Place princess randomly (not at [1][1], not on wall)
        placeRandom(CellType.PRINCESS);

        // Step 5 - Place bombs randomly
        for (int i = 0; i < NUM_BOMBS; i++) {
            placeRandom(CellType.BOMB);
        }
    }

    private void placeRandom(CellType type) {
        Random rand = new Random();
        int row, col;
        do {
            // Stay inside the walls (rows 1-8, cols 1-8)
            row = 1 + rand.nextInt(ROWS - 2);
            col = 1 + rand.nextInt(COLS - 2);
        } while (matrix[row][col] != CellType.GRASS); // retry if cell is taken
        matrix[row][col] = type;
    }

    private void drawBoard(GridPane grid) {
        grid.getChildren().clear();

        double cellWidth  = SCENE_WIDTH  / (double) COLS;
        double cellHeight = SCENE_HEIGHT / (double) ROWS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                StackPane cell = new StackPane();
                cell.setPrefSize(cellWidth, cellHeight);

                // Always add grass as background layer
                ImageView grassView = new ImageView(grassImg);
                grassView.setFitWidth(cellWidth);
                grassView.setFitHeight(cellHeight);
                cell.getChildren().add(grassView);

                // Add object on top based on matrix value
                if (matrix[row][col] == CellType.WALL) {
                    ImageView wallView = new ImageView(wallImg);
                    wallView.setFitWidth(cellWidth);
                    wallView.setFitHeight(cellHeight);
                    cell.getChildren().add(wallView);

                } else if (matrix[row][col] == CellType.PLAYER) {
                    ImageView playerView = new ImageView(playerImg);
                    playerView.setFitWidth(cellWidth);
                    playerView.setFitHeight(cellHeight);
                    cell.getChildren().add(playerView);

                } else if (matrix[row][col] == CellType.PRINCESS) {
                    ImageView princessView = new ImageView(princessImg);
                    princessView.setFitWidth(cellWidth);
                    princessView.setFitHeight(cellHeight);
                    cell.getChildren().add(princessView);

                } else if (matrix[row][col] == CellType.BOMB) {
                    ImageView bombView = new ImageView(bombImg);
                    bombView.setFitWidth(cellWidth);
                    bombView.setFitHeight(cellHeight);
                    cell.getChildren().add(bombView);
                }

                grid.add(cell, col, row);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}