package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Random;

public class Gameboard extends Application {

    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int SCENE_WIDTH = 800;
    private static final int SCENE_HEIGHT = 800;
    private static final int NUM_BOMBS = 5;
    private static final int MAX_LIVES = 3;

    enum CellType {
        GRASS, PLAYER, PRINCESS, BOMB, WALL
    }

    private CellType[][] matrix = new CellType[ROWS][COLS];

    private int playerRow = 1;
    private int playerCol = 1;
    private int lives = MAX_LIVES;

    // 🔹 Only reveal bombs when lives = 0
    private boolean showBombs = false;

    private Image grassImg;
    private Image playerImg;
    private Image princessImg;
    private Image bombImg;
    private Image wallImg;

    private GridPane grid;
    private Label livesLabel;

    @Override
    public void start(Stage stage) {

        loadImages();
        initMatrix();

        grid = new GridPane();
        grid.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        drawBoard();

        // 🔹 Lives display at the top
        livesLabel = new Label(getLivesText());
        livesLabel.setStyle("-fx-font-size: 24px; -fx-padding: 5px 10px;");

        BorderPane root = new BorderPane();
        root.setTop(livesLabel);
        root.setCenter(grid);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT + 40);

        scene.setOnKeyPressed(event -> {
            int newRow = playerRow;
            int newCol = playerCol;

            switch (event.getCode()) {
                case UP    -> newRow--;
                case DOWN  -> newRow++;
                case LEFT  -> newCol--;
                case RIGHT -> newCol++;
                default    -> { return; }
            }

            handleMove(newRow, newCol, stage);
        });

        stage.setTitle("Rescue the Princess");
        stage.setScene(scene);
        stage.show();
    }

    private void handleMove(int newRow, int newCol, Stage stage) {
        CellType target = matrix[newRow][newCol];

        // 🔹 Blocked by wall
        if (target == CellType.WALL) {
            return;
        }

        // 🔹 Hit a bomb
        if (target == CellType.BOMB) {
            lives--;
            livesLabel.setText(getLivesText());

            // Move player onto bomb cell
            matrix[playerRow][playerCol] = CellType.GRASS;
            playerRow = newRow;
            playerCol = newCol;
            matrix[playerRow][playerCol] = CellType.PLAYER;

            if (lives <= 0) {
                // 🔹 Reveal all bombs on last life
                showBombs = true;
                drawBoard();
                showAlert("💥 Game Over!", "You ran out of lives! Bombs revealed.", stage);
            } else {
                drawBoard();
                showAlert("💣 Ouch!", "You hit a bomb! Lives remaining: " + lives, stage);

                // 🔹 Reset player to [1][1] after losing a life
                matrix[playerRow][playerCol] = CellType.GRASS;
                playerRow = 1;
                playerCol = 1;
                matrix[playerRow][playerCol] = CellType.PLAYER;
                drawBoard();
            }
            return;
        }

        // 🔹 Reached princess - you win
        if (target == CellType.PRINCESS) {
            matrix[playerRow][playerCol] = CellType.GRASS;
            playerRow = newRow;
            playerCol = newCol;
            matrix[playerRow][playerCol] = CellType.PLAYER;
            drawBoard();
            showAlert("👑 You Win!", "You rescued the princess with " + lives + " lives left!", stage);
            return;
        }

        // 🔹 Normal move
        matrix[playerRow][playerCol] = CellType.GRASS;
        playerRow = newRow;
        playerCol = newCol;
        matrix[playerRow][playerCol] = CellType.PLAYER;
        drawBoard();
    }

    private String getLivesText() {
        String hearts = "❤️".repeat(lives) + "🖤".repeat(MAX_LIVES - lives);
        return "  Lives: " + hearts;
    }

    private void showAlert(String title, String message, Stage stage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.showAndWait();

        // Only ask to replay on game over or win
        if (title.contains("Game Over") || title.contains("Win")) {
            Alert replay = new Alert(Alert.AlertType.CONFIRMATION);
            replay.setTitle("Play Again?");
            replay.setHeaderText(null);
            replay.setContentText("Would you like to play again?");
            replay.initOwner(stage);
            replay.showAndWait().ifPresent(response -> {
                if (response.getText().equals("OK")) {
                    resetGame();
                } else {
                    stage.close();
                }
            });
        }
    }

    private void resetGame() {
        playerRow = 1;
        playerCol = 1;
        lives = MAX_LIVES;
        showBombs = false;
        livesLabel.setText(getLivesText());
        initMatrix();
        drawBoard();
    }

    private void loadImages() {
        grassImg    = new Image(getClass().getResourceAsStream("/images/grass.png"));
        playerImg   = new Image(getClass().getResourceAsStream("/images/player.png"));
        princessImg = new Image(getClass().getResourceAsStream("/images/princess.png"));
        bombImg     = new Image(getClass().getResourceAsStream("/images/bomb.png"));
        wallImg     = new Image(getClass().getResourceAsStream("/images/wall.png"));
    }

    private void initMatrix() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                matrix[r][c] = CellType.GRASS;
            }
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                    matrix[r][c] = CellType.WALL;
                }
            }
        }

        matrix[1][1] = CellType.PLAYER;
        placeRandom(CellType.PRINCESS);

        for (int i = 0; i < NUM_BOMBS; i++) {
            placeRandom(CellType.BOMB);
        }
    }

    private void placeRandom(CellType type) {
        Random rand = new Random();
        int row, col;
        do {
            row = 1 + rand.nextInt(ROWS - 2);
            col = 1 + rand.nextInt(COLS - 2);
        } while (matrix[row][col] != CellType.GRASS);
        matrix[row][col] = type;
    }

    private void drawBoard() {
        grid.getChildren().clear();

        double cellWidth  = SCENE_WIDTH  / (double) COLS;
        double cellHeight = SCENE_HEIGHT / (double) ROWS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                StackPane cell = new StackPane();
                cell.setPrefSize(cellWidth, cellHeight);

                ImageView grassView = new ImageView(grassImg);
                grassView.setFitWidth(cellWidth);
                grassView.setFitHeight(cellHeight);
                cell.getChildren().add(grassView);

                if (matrix[row][col] == CellType.WALL) {
                    ImageView iv = new ImageView(wallImg);
                    iv.setFitWidth(cellWidth);
                    iv.setFitHeight(cellHeight);
                    cell.getChildren().add(iv);

                } else if (matrix[row][col] == CellType.PLAYER) {
                    ImageView iv = new ImageView(playerImg);
                    iv.setFitWidth(cellWidth);
                    iv.setFitHeight(cellHeight);
                    cell.getChildren().add(iv);

                } else if (matrix[row][col] == CellType.PRINCESS) {
                    ImageView iv = new ImageView(princessImg);
                    iv.setFitWidth(cellWidth);
                    iv.setFitHeight(cellHeight);
                    cell.getChildren().add(iv);

                } else if (matrix[row][col] == CellType.BOMB) {
                    // 🔹 Only show bomb image if showBombs is true
                    if (showBombs) {
                        ImageView iv = new ImageView(bombImg);
                        iv.setFitWidth(cellWidth);
                        iv.setFitHeight(cellHeight);
                        cell.getChildren().add(iv);
                    }
                    // Otherwise it just shows grass — bomb is hidden
                }

                grid.add(cell, col, row);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}