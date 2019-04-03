/*
 * Copyright (c) 2019 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.observablematrix;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class DemoAtomic extends Application {
    private static final int                             MAX_THREADS = 4;
    private static final Random                          RND         = new Random();
    private static final int                             COLS        = 3;
    private static final int                             ROWS        = 2;
    private static volatile int                          taskCounter = 0;
    private              AtomicObservableMatrix<Integer> integerMatrix;
    private              GridPane                        grid;
    private              long                            lastTimerCall;
    private              AnimationTimer                  timer;
    private              Executor                        executor;


    @Override public void init() {
        this.executor = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t ;
        });

        System.out.println("Create matrix of integers [3][2]:");
        integerMatrix = new AtomicObservableMatrix<>(Integer.class, 3, 2);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        System.out.println("Fill matrix with random values:");
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                Integer value = RND.nextInt(10);
                Label   label = new Label(Integer.toString(value));
                integerMatrix.setItemAt(x, y, value);
                grid.add(label, x, y);
            }
        }

        System.out.println("Create matrix of integers [5][3] and fill with random values:");
        AtomicObservableMatrix<Integer> matrix = new AtomicObservableMatrix<>(Integer.class, 5, 3);
        for (int y = 0 ; y < 3 ; y++) {
            for (int x = 0 ; x < 5 ; x++) {
                matrix.setItemAt(x, y, RND.nextInt(10));
            }
        }
        System.out.println("Reihen in Matrix    : " + matrix.getNoOfRows());
        System.out.println("Anzahl leerer Reihen: " + matrix.getAllEmptyRows());

        System.out.println("Remove row 1:");
        matrix.removeRow(1);

        System.out.println("Rows in matrix      : " + matrix.getNoOfRows());
        System.out.println("Number of empty rows: " + matrix.getAllEmptyRows());

        System.out.println("Create new matrix of integers [5][4] and fill with random values:");
        matrix = new AtomicObservableMatrix<>(Integer.class, 5, 4);
        for (int y = 0 ; y < 4 ; y++) {
            for (int x = 0 ; x < 5 ; x++) {
                matrix.setItemAt(x, y, RND.nextInt(10));
            }
        }
        System.out.println("Output matrix values:");
        for (int y = 0 ; y < matrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < matrix.getNoOfCols() ; x++) {
                System.out.print(matrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }

        System.out.println("\nAdd column of 'null' objects at column 0:\n");
        matrix.addNullCol(0);
        System.out.println("Output matrix values:");
        for (int y = 0 ; y < matrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < matrix.getNoOfCols() ; x++) {
                System.out.print(matrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }

        System.out.println("\nRemove last column:\n");
        matrix.removeCol(matrix.getNoOfCols() - 1);
        System.out.println("Output matrix values:");
        for (int y = 0 ; y < matrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < matrix.getNoOfCols() ; x++) {
                System.out.print(matrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }


        integerMatrix.setOnItemChanged(e -> {
            Node node = getNodeAt(e.getX(), e.getY(), grid);
            if (null == node) { return; }
            ((Label) node).setText(Integer.toString(e.getItem()));
        });

        // Other examples
        /*
        integerMatrix.setOnMEvent(MItemEvent.ITEM_ADDED, event -> {

        });

        integerMatrix.setOnMEvent(MItemEvent.ANY, event -> {

        });
        */

        System.out.println("\nMirror matrix rows:\n");
        AtomicObservableMatrix<Integer> mirrorRowsMatrix = new AtomicObservableMatrix<>(Integer.class, 3, 4);
        mirrorRowsMatrix.setItemAt(0, 0, 1);
        mirrorRowsMatrix.setItemAt(1, 0, 2);
        mirrorRowsMatrix.setItemAt(2, 0, 3);
        mirrorRowsMatrix.setItemAt(0, 1, 4);
        mirrorRowsMatrix.setItemAt(1, 1, 5);
        mirrorRowsMatrix.setItemAt(2, 1, 6);
        mirrorRowsMatrix.setItemAt(0, 2, 7);
        mirrorRowsMatrix.setItemAt(1, 2, 8);
        mirrorRowsMatrix.setItemAt(2, 2, 9);
        mirrorRowsMatrix.setItemAt(0, 3, 10);
        mirrorRowsMatrix.setItemAt(1, 3, 11);
        mirrorRowsMatrix.setItemAt(2, 3, 12);

        for (int y = 0 ; y < mirrorRowsMatrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < mirrorRowsMatrix.getNoOfCols() ; x++) {
                System.out.print(mirrorRowsMatrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }

        mirrorRowsMatrix.mirrorRows();
        System.out.println("\nMirror rows\n");
        for (int y = 0 ; y < mirrorRowsMatrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < mirrorRowsMatrix.getNoOfCols() ; x++) {
                System.out.print(mirrorRowsMatrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }

        System.out.println("\nMirror matrix columns:\n");
        AtomicObservableMatrix<Integer> mirrorColsMatrix = new AtomicObservableMatrix<>(Integer.class, 3, 4);
        mirrorColsMatrix.setItemAt(0, 0, 1);
        mirrorColsMatrix.setItemAt(1, 0, 2);
        mirrorColsMatrix.setItemAt(2, 0, 3);
        mirrorColsMatrix.setItemAt(0, 1, 4);
        mirrorColsMatrix.setItemAt(1, 1, 5);
        mirrorColsMatrix.setItemAt(2, 1, 6);
        mirrorColsMatrix.setItemAt(0, 2, 7);
        mirrorColsMatrix.setItemAt(1, 2, 8);
        mirrorColsMatrix.setItemAt(2, 2, 9);
        mirrorColsMatrix.setItemAt(0, 3, 10);
        mirrorColsMatrix.setItemAt(1, 3, 11);
        mirrorColsMatrix.setItemAt(2, 3, 12);

        for (int y = 0 ; y < mirrorColsMatrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < mirrorColsMatrix.getNoOfCols() ; x++) {
                System.out.print(mirrorColsMatrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }

        mirrorColsMatrix.mirrorColumns();
        System.out.println("\nMirror columns\n");
        for (int y = 0 ; y < mirrorColsMatrix.getNoOfRows() ; y++) {
            for (int x = 0 ; x < mirrorColsMatrix.getNoOfCols() ; x++) {
                System.out.print(mirrorColsMatrix.getItemAt(x, y) + " ");
            }
            System.out.println();
        }


        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 100_000_000l) {
                    startTask();
                    startTask();
                    startTask();
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(grid);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("Observable Matrix");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private void startTask() {

        Task<Integer> task = new Task<Integer>() {
            public int no = taskCounter++;
            @Override protected Integer call() throws Exception {
                int col = RND.nextInt(COLS);
                int row = RND.nextInt(ROWS);
                Platform.runLater(() -> integerMatrix.setItemAt(col, row, RND.nextInt(10)));
                return no;
            }
        };
        task.setOnSucceeded(e -> System.out.println("Task " + task.getValue() + " finished"));
        executor.execute(task);
    }

    private Node getNodeAt(final int col, final int row, final GridPane grid) {
        return grid.getChildren().stream()
                   .filter(node -> col == grid.getColumnIndex(node))
                   .filter(node -> row == grid.getRowIndex(node))
                   .findFirst()
                   .orElse(null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
