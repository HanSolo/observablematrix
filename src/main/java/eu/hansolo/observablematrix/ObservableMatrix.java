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

import eu.hansolo.observablematrix.event.MColumnEvent;
import eu.hansolo.observablematrix.event.MColumnsEvent;
import eu.hansolo.observablematrix.event.MEvent;
import eu.hansolo.observablematrix.event.MEventType;
import eu.hansolo.observablematrix.event.MItemEvent;
import eu.hansolo.observablematrix.event.MObserver;
import eu.hansolo.observablematrix.event.MRowEvent;
import eu.hansolo.observablematrix.event.MRowsEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.hansolo.observablematrix.event.MColumnEvent.COLUMN_ADDED;
import static eu.hansolo.observablematrix.event.MColumnEvent.COLUMN_REMOVED;
import static eu.hansolo.observablematrix.event.MItemEvent.ITEM_ADDED;
import static eu.hansolo.observablematrix.event.MItemEvent.ITEM_CHANGED;
import static eu.hansolo.observablematrix.event.MItemEvent.ITEM_REMOVED;
import static eu.hansolo.observablematrix.event.MRowEvent.ROW_ADDED;
import static eu.hansolo.observablematrix.event.MRowEvent.ROW_REMOVED;
import static eu.hansolo.observablematrix.event.MColumnsEvent.NO_OF_COLUMNS_CHANGED;
import static eu.hansolo.observablematrix.event.MRowsEvent.NO_OF_ROWS_CHANGED;


public class ObservableMatrix<T> {
    private final          Class<T>                     type;
    private final          Map<String, List<MObserver>> observers;
    private                T[][]                        matrix;
    private       volatile int                          cols;
    private       volatile int                          rows;
    private                Consumer<MItemEvent<T>>      itemAddedConsumer;
    private                Consumer<MItemEvent<T>>      itemRemovedConsumer;
    private                Consumer<MItemEvent<T>>      itemChangedConsumer;
    private                Consumer<MColumnEvent>       columnAddedConsumer;
    private                Consumer<MColumnEvent>       columnRemovedConsumer;
    private                Consumer<MRowEvent>          rowAddedConsumer;
    private                Consumer<MRowEvent>          rowRemovedConsumer;
    private                Consumer<MColumnsEvent>      columnsChangedConsumer;
    private                Consumer<MRowsEvent>         rowsChangedConsumer;
    private                boolean                      resizeMatrixWhenInnerRowOrColIsRemoved;


    // ******************** Constructors **************************************
    public ObservableMatrix(Class<T> type, final int cols, final int rows) {
        this(type, cols, rows, false);
    }
    public ObservableMatrix(Class<T> type, final int cols, final int rows, final boolean resizeMatrixWhenInnerRowOrColIsRemoved) {
        this.type                                   = type;
        this.observers                              = new ConcurrentHashMap<>();;
        this.matrix                                 = createArray(type, cols, rows);
        this.cols                                   = cols;
        this.rows                                   = rows;
        this.resizeMatrixWhenInnerRowOrColIsRemoved = resizeMatrixWhenInnerRowOrColIsRemoved;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the given item in the matrix at the given position defined by x and y
     * @param x Column used to return item
     * @param y Row used to return item
     * @return the given item in the matrix at the given position defined by x and y
     */
    public T getItemAt(final int x, final int y) {
        if (x < 0 || x > (cols - 1) || y < 0 || y > (rows - 1)) { throw new IllegalArgumentException("cols/rows cannot be smaller than 0"); }
        return matrix[x][y];
    }

    /**
     * Sets the given item in the matrix at the given position defined by x and y
     * @param x Column where the given item will be inserted
     * @param y Row where the given item will be inserted
     * @param item
     */
    public void setItemAt(final int x, final int y, final T item) {
        if (x < 0 || x > (cols - 1) || y < 0 || y > (rows - 1)) { throw new IllegalArgumentException("cols/rows cannot be smaller than 0"); }

        T oldItem = matrix[x][y];
        matrix[x][y] = item;

        if (null == oldItem && item != null) {
            MItemEvent<T> evt = new MItemEvent<>(ObservableMatrix.this, ITEM_ADDED, x, y, oldItem, item);
            if (null != itemAddedConsumer) { itemAddedConsumer.accept(evt);}
            fireEvent(evt);
        } else if (null != oldItem && item == null) {
            MItemEvent<T> evt = new MItemEvent<>(ObservableMatrix.this, ITEM_REMOVED, x, y, oldItem, item);
            if (null != itemRemovedConsumer) { itemRemovedConsumer.accept(evt); }
            fireEvent(evt);
        } else if (null != oldItem && item != null) {
            MItemEvent<T> evt = new MItemEvent<>(ObservableMatrix.this, ITEM_CHANGED, x, y, oldItem, item);
            if (null != itemChangedConsumer) { itemChangedConsumer.accept(evt); }
            fireEvent(evt);
        } else {
            return;
        }
    }

    /**
     * Removes item at position defined by x and y
     * Item will be set to null
     * @param x Column where the item will be set to null
     * @param y Column where the item will be set to null
     */
    public void removeItemAt(final int x, final int y) {
        if (x < 0 || x > (cols - 1) || y < 0 || y > (rows - 1)) { throw new IllegalArgumentException("cols/rows cannot be smaller than 0"); }
        T oldItem = matrix[x][y];
        matrix[x][y] = null;
        MItemEvent<T> evt = new MItemEvent<>(ObservableMatrix.this, ITEM_REMOVED, x, y, oldItem, null);
        if (null != itemRemovedConsumer) { itemRemovedConsumer.accept(evt); }
        fireEvent(evt);
        checkForRemovedColumnsAndRows(x, y);
    }

    /**
     * If item is found in matrix it's position in the matrix will be set to null
     * @param item Item to remove from matrix
     */
    public void removeItem(final T item) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                T matrixItem = matrix[x][y];
                if (null == matrixItem) {
                    continue;
                } else if (matrixItem.equals(item)) {
                    matrix[x][y] = null;
                    MItemEvent<T> evt = new MItemEvent<>(ObservableMatrix.this, ITEM_REMOVED, x, y, item, null);
                    if (null != itemRemovedConsumer) { itemRemovedConsumer.accept(evt); }
                    fireEvent(evt);
                    checkForRemovedColumnsAndRows(x, y);
                    return;
                }
            }
        }
    }

    /**
     * Returns true if the given item will be found in the matrix
     * @param item
     * @return true if the given item will be found in the matrix
     */
    public boolean contains(final T item) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (null != matrix[x][y] && matrix[x][y].equals(item)) { return true; }
            }
        }
        return false;
    }

    /**
     * Returns the indices of the given item as an array of int[],
     * where [0] contains the column and
     * [1] contains the row of the item
     * In case the item was not found the method will return [-1, -1]
     * @param item
     * @return the indices of the given item as an array of in[]
     */
    public int[] getIndicesOf(final T item) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (matrix[x][y].equals(item)) { return new int[]{x, y}; }
            }
        }
        return new int[]{-1, - 1};
    }

    /**
     * Returns the 2-dimensional array of type <T>
     * @return the 2-dimensional array of type <T>
     */
    public T[][] getMatrix() { return matrix; }

    /**
     * Returns all items in matrix that are non null as list
     * @return all items in matrix that are non null as list
     */
    public List<T> getAllItems() { return stream().filter(Objects::nonNull).collect(Collectors.toList()); }

    /**
     * Returns all items in matrix as stream
     * @return all items in matrix as stream
     */
    public Stream<T> stream() { return Arrays.stream(matrix).flatMap(t -> Arrays.stream(t)); }

    public void reset() {
        if (rows == -1 || cols == -1) { throw new IllegalArgumentException("cols/rows cannot be smaller 0"); }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = null;
            }
        }
    }

    /**
     * Get all items in column specified by index as list
     * @param col index of column to return as list
     * @return all items in column specified by index as list
     */
    public List<T> getCol(final int col) {
        if (rows == -1 || cols == -1 || col < 0 || col > cols) { throw new IllegalArgumentException("cols/rows cannot be smaller 0"); }
        List<T> c = new ArrayList<>();
        for (int y = 0 ; y < rows ; y++) { c.add(matrix[col][y]); }
        return c;
    }

    /**
     * Get all items in row specified by index as list
     * @param row index of row to return as list
     * @return all items in row specified by index as list
     */
    public List<T> getRow(final int row) {
        if (rows == -1 || cols == -1 || row < 0 || row > rows) { throw new IllegalArgumentException("cols/rows cannot be smaller 0"); }
        List<T> r = new ArrayList<>();
        for (int x = 0 ; x < cols ; x++) { r.add(matrix[x][row]); }
        return r;
    }

    /**
     * Returns true if all items in given column index equals null
     * @param col
     * @return true if all items in given column index equals null
     */
    public boolean isColEmpty(final int col) {
        List<T> c = getCol(col);
        long count = 0;
        for(T item : c) { if (null != item) { count++; }}
        return 0 == count;
    }

    /**
     * Returns true if all items in given row index equals null
     * @param row
     * @return true if all items in given row index equals null
     */
    public boolean isRowEmpty(final int row) {
        List<T> r = getRow(row);
        long count = 0;
        for (T item : r) { if (null != item) { count++; }}
        return 0 == count;
    }

    /**
     * Returns the number of columns in the matrix
     * @return the number of columsn of the matrix
     */
    public int getNoOfCols() { return cols; }

    /**
     * Sets the number of columsn in the matrix.
     * Existing entries will be copied to the new matrix.
     * If the number of columns is smaller than the old
     * one, all items in columns outside of the new matrix
     * will be lost.
     * @param cols
     */
    public void setCols(final int cols) {
        if (rows == -1 || cols == -1 || this.cols == -1) { throw new IllegalArgumentException("cols/rows cannot be smaller 0"); }
        T[][] oldMatrix = (T[][]) new Object[cols][rows];

        for (int y = 0 ; y < this.rows ; y++) {
            for (int x = 0 ; x < this.cols ; x++) {
                oldMatrix[x][y] = matrix[x][y];
            }
        }
        int oldCols = this.cols;
        this.cols   = cols;
        matrix = createArray(type, cols, rows);
        int c;
        int r  = rows;
        if (cols > this.cols) {
            c = oldCols;
        } else if (cols < this.cols) {
            c = cols;
        } else {
            c = cols;
        }
        for (int y = 0 ; y < r ; y++) {
            for (int x = 0 ; x < c ; x++) {
                matrix[x][y] = oldMatrix[x][y];
            }
        }
        MColumnsEvent evt = new MColumnsEvent(ObservableMatrix.this, NO_OF_COLUMNS_CHANGED, cols);
        if (null != columnsChangedConsumer) { columnsChangedConsumer.accept(evt); }
        fireEvent(evt);
    }

    /**
     * Adds a column at the given position in the matrix and fills it with the items from the itemSupplier
     * @param at position of where to add the new column
     * @param itemSupplier supplier of items
     */
    public void addCol(final int at, final Supplier<T> itemSupplier) {
        if (at < 0 || at > cols) { throw new IllegalArgumentException("index cannot be smaller or larger than cols"); }

        cols++;

        T[][] newMatrix = createArray(type, cols, rows);
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < at ; x++) {
                newMatrix[x][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { newMatrix[at][y] = itemSupplier.get(); }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = at + 1 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x - 1][y];
            }
        }

        matrix = newMatrix;
        MColumnEvent evt = new MColumnEvent(ObservableMatrix.this, COLUMN_ADDED, at);
        if (null != columnAddedConsumer) { columnAddedConsumer.accept(evt); }
        fireEvent(evt);
    }

    public void addCol(final int at, final List<T> items) {
        if (at < 0 || at > cols) { throw new IllegalArgumentException("index cannot be smaller or larger than cols"); }
        if (items.size() != rows) { throw new IllegalArgumentException("no of items must be equal to number of rows"); }

        cols++;

        T[][] newMatrix = createArray(type, cols, rows);
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < at ; x++) {
                newMatrix[x][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { newMatrix[at][y] = items.get(y); }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = at + 1 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x - 1][y];
            }
        }

        matrix = newMatrix;
        MColumnEvent evt = new MColumnEvent(ObservableMatrix.this, COLUMN_ADDED, at);
        if (null != columnAddedConsumer) { columnAddedConsumer.accept(evt); }
        fireEvent(evt);
    }

    public void addNullCol(final int at) {
        if (at < 0 || at > cols) { throw new IllegalArgumentException("index cannot be smaller or larger than cols"); }

        cols++;

        T[][] newMatrix = createArray(type, cols, rows);
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < at ; x++) {
                newMatrix[x][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { newMatrix[at][y] = null; }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = at + 1 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x - 1][y];
            }
        }

        matrix = newMatrix;
        MColumnEvent evt = new MColumnEvent(ObservableMatrix.this, COLUMN_ADDED, at);
        if (null != columnAddedConsumer) { columnAddedConsumer.accept(evt); }
        fireEvent(evt);
    }

    /**
     * Removes col at given index
     * @param at index of col that should be removed
     */
    public void removeCol(final int at) {
        if (at < 0 || at > cols) { throw new IllegalArgumentException("index cannot be smaller or larger than cols"); }
        if (cols <= 1) { throw new IllegalArgumentException("there is just one column in the matrix"); }

        for (int y = 0 ; y < getNoOfRows() ; y++) { matrix[at][y] = null; }

        if (0 == at || (cols - 1) == at || resizeMatrixWhenInnerRowOrColIsRemoved) {
            cols--;

            T[][] newMatrix = createArray(type, cols, rows);
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x <= cols; x++) {
                    if (x < at) {
                        newMatrix[x][y] = matrix[x][y];
                    } else if (x == at) {

                    } else {
                        newMatrix[x - 1][y] = matrix[x][y];
                    }
                }
            }
            matrix = newMatrix;
        }
        MColumnEvent evt = new MColumnEvent(ObservableMatrix.this, COLUMN_REMOVED, at);
        if (null != columnRemovedConsumer) { columnRemovedConsumer.accept(evt); }
        fireEvent(evt);
    }

    /**
     * Adds a row at the given position in the matrix and fills it with the items from the itemSupplier
     * @param at position of where to add the new row
     * @param itemSupplier supplier of items
     */
    public void addRow(final int at, final Supplier<T> itemSupplier) {
        if (at < 0 || at > rows) { throw new IllegalArgumentException("index cannot be smaller or larger than rows"); }

        rows++;

        T[][] newMatrix = createArray(type, cols, rows);
        for (int y = 0 ; y < at ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { newMatrix[x][at] = itemSupplier.get(); }
        for (int y = at + 1 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x][y - 1];
            }
        }

        matrix = newMatrix;
        MRowEvent evt = new MRowEvent(ObservableMatrix.this, ROW_ADDED, at);
        if (null != rowAddedConsumer) { rowAddedConsumer.accept(evt); }
        fireEvent(evt);
    }

    public void addRow(final int at, final List<T> items) {
        if (at < 0 || at > rows) { throw new IllegalArgumentException("index cannot be smaller or larger than rows"); }
        if (items.size() != cols) { throw new IllegalArgumentException("now of items must be equal to number of columns"); }

        rows++;

        T[][] newMatrix = createArray(type, cols, rows);
        for (int y = 0 ; y < at ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { newMatrix[x][at] = items.get(x); }
        for (int y = at + 1 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x][y - 1];
            }
        }

        matrix = newMatrix;
        MRowEvent evt = new MRowEvent(ObservableMatrix.this, ROW_ADDED, at);
        if (null != rowAddedConsumer) { rowAddedConsumer.accept(evt); }
        fireEvent(evt);
    }

    public void addNullRow(final int at) {
        if (at < 0 || at > rows) { throw new IllegalArgumentException("index cannot be smaller or larger than rows"); }

        rows++;

        T[][] newMatrix = createArray(type, cols, rows);
        for (int y = 0 ; y < at ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { newMatrix[x][at] = null; }
        for (int y = at + 1 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                newMatrix[x][y] = matrix[x][y - 1];
            }
        }

        matrix = newMatrix;

        MRowEvent evt = new MRowEvent(ObservableMatrix.this, ROW_ADDED, at);
        if (null != rowAddedConsumer) { rowAddedConsumer.accept(evt); }
        fireEvent(evt);
    }

    /**
     * Removes row at given index
     * @param at index of row that should be removed
     */
    public void removeRow(final int at) {
        if (at < 0 || at > rows) { throw new IllegalArgumentException("index cannot be smaller or larger than rows"); }
        if (rows <= 1) { throw new IllegalArgumentException("there is just one row in the matrix"); }

        for (int x = 0 ; x < getNoOfCols() ; x++) { matrix[x][at] = null; }

        if (0 == at || (rows - 1) == at || resizeMatrixWhenInnerRowOrColIsRemoved) {
            rows--;

            T[][] newMatrix = createArray(type, cols, rows);
            for (int y = 0; y <= rows; y++) {
                if (y < at) {
                    for (int x = 0; x < cols; x++) {
                        newMatrix[x][y] = matrix[x][y];
                    }
                } else if (y == at) {

                } else {
                    for (int x = 0; x < cols; x++) {
                        newMatrix[x][y - 1] = matrix[x][y];
                    }
                }
            }
            matrix = newMatrix;
        }
        MRowEvent evt = new MRowEvent(ObservableMatrix.this, ROW_REMOVED, at);
        if (null != rowRemovedConsumer) { rowRemovedConsumer.accept(evt); }
        fireEvent(evt);
    }

    /**
     * Returns the number of rows in the matrix
     * @return the number of rows in the matrix
     */
    public int getNoOfRows() { return rows; }

    /**
     * Sets the number of rows in the matrix.
     * Existing entries will be copied to the new matrix.
     * If the new number of rows is smaller than the old
     * one, all items in rows outside the new matrix will
     * be lost.
     * @param rows
     */
    public void setRows(final int rows) {
        if (rows == -1 || cols == -1 || this.rows == -1) { throw new IllegalArgumentException("cols/rows cannot be smaller 0"); }
        T[][] oldMatrix = (T[][]) new Object[cols][rows];
        for (int y = 0 ; y < this.rows ; y++) {
            for (int x = 0 ; x < this.cols ; x++) {
                oldMatrix[x][y] = matrix[x][y];
            }
        }
        int oldRows = this.rows;
        this.rows   = rows;
        matrix = createArray(type, cols, rows);

        int c  = cols;
        int r;
        if (rows > this.rows) {
            r = oldRows;
        } else if (rows < this.rows) {
            r = rows;
        } else {
            r = rows;
        }
        for (int y = 0 ; y < r ; y++) {
            for (int x = 0 ; x < c ; x++) {
                matrix[x][y] = oldMatrix[x][y];
            }
        }
        MRowsEvent evt = new MRowsEvent(ObservableMatrix.this, NO_OF_ROWS_CHANGED, rows);
        if (null != rowsChangedConsumer) { rowsChangedConsumer.accept(evt); }
        fireEvent(evt);
    }

    public List<List<T>> getAllColumns() {
        List<List<T>> columns = new ArrayList<>();
        for (int i = 0; i < getNoOfCols() ; i++) { columns.add(getCol(i)); }
        return columns;
    }
    public List<Integer> getAllEmptyColumns() {
        List<Integer> emptyColumns = new ArrayList<>();
        for (int x = 0; x < getNoOfCols() ; x++) {
            if (getCol(x).stream().filter(Objects::nonNull).count() == 0) { emptyColumns.add(x); }
        }
        return emptyColumns;
    }

    public List<List<T>> getAllRows() {
        List<List<T>> rows = new ArrayList<>();
        for (int i = 0; i < getNoOfRows() ; i++) { rows.add(getRow(i)); }
        return rows;
    }
    public List<Integer> getAllEmptyRows() {
        List<Integer> emptyRows = new ArrayList<>();
        for (int y = 0; y < getNoOfRows() ; y++) {
            if (getRow(y).stream().filter(Objects::nonNull).count() == 0) { emptyRows.add(y); }
        }
        return emptyRows;
    }

    public void mirrorColumns() {
        for(int i = 0; i < (matrix.length/2); i++) {
            T[] temp = matrix[i];
            matrix[i] = matrix[matrix.length - i - 1];
            matrix[matrix.length - i - 1] = temp;
        }
    }
    public void mirrorRows() {
        for (int j = 0; j < matrix.length; ++j) {
            T[] row = matrix[j];
            for(int i = 0; i < (row.length/2); i++) {
                T temp = row[i];
                row[i] = matrix[j][row.length - i - 1];
                row[row.length - i - 1] = temp;
            }
        }
    }

    public boolean getResizeMatrixWhenInnerRowOrColIsRemoved() { return resizeMatrixWhenInnerRowOrColIsRemoved; }
    public void setResizeMatrixWhenInnerRowOrColIsRemoved(final boolean resize) { resizeMatrixWhenInnerRowOrColIsRemoved = resize; }


    // ******************** Private methods ***********************************
    /**
     * Method to reduce matrix size in case the first column is empty (all items == null)
     */
    private void shiftLeft() {
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 1 ; x < cols ; x++) {
                matrix[x - 1][y] = matrix[x][y];
            }
        }
    }

    /**
     * Method to reduce matrix size in case the first row is empty (all items == null)
     */
    private void shiftUp() {
        for (int y = 1 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y - 1] = matrix[x][y];
            }
        }
    }

    /**
     * Returns a 2-dimensional array of the given type <T> and size
     * @param type Class of generic type
     * @param cols Number of columns for the matrix
     * @param rows Number of rows for the matrix
     * @param <T>
     * @return a 2-dimensional array of the given type <T> and size
     */
    private static <T> T[][] createArray(final Class type, final int cols, final int rows) {
        if (null == type) { throw new IllegalArgumentException("type cannot be null"); }
        if ( cols < 1 || rows < 1) { throw new IllegalArgumentException("cols/rows cannot be smaller than 1"); }
        return (T[][]) Array.newInstance(type, cols, rows);
    }

    private void checkForRemovedColumnsAndRows(final int removedItemCol, final int removedItemRow) {
        int nullItemCounter = 0;
        for (int r = 0 ; r < rows ; r++) {
            if (null == getItemAt(removedItemCol, r)) { nullItemCounter++; }
        }
        if (nullItemCounter == rows) {
            removeCol(removedItemCol);
            return;
        }

        nullItemCounter = 0;
        for (int c = 0 ; c < cols ; c++) {
            if (null == getItemAt(c, removedItemRow)) { nullItemCounter++; }
        }
        if (nullItemCounter == cols) {
            removeRow(removedItemRow);
            return;
        }
    }


    // ******************** Event Handling ************************************
    /**
     * Sets the consumer that will be triggered if an item was added to the matrix
     * @param itemAddedConsumer A Consumer of type MItemEvent<T>
     */
    public void setOnItemAdded(final Consumer<MItemEvent<T>> itemAddedConsumer) { this.itemAddedConsumer = itemAddedConsumer; }

    /**
     * Sets the consumer that will be triggered if an item was removed from the matrix
     * @param itemRemovedConsumer A consumer of type MItemEvent<T>
     */
    public void setOnItemRemoved(final Consumer<MItemEvent<T>> itemRemovedConsumer) { this.itemRemovedConsumer = itemRemovedConsumer; }

    /**
     * Sets the consumer that will be triggered if an item was changed in the matrix
     * @param itemChangedConsumer A consumer of type MItemEvent<T>
     */
    public void setOnItemChanged(final Consumer<MItemEvent<T>> itemChangedConsumer) { this.itemChangedConsumer = itemChangedConsumer; }

    /**
     * Sets the consumer that will be triggered if a column was added to the matrix
     * @param columnAddedConsumer A consumer of type MColumnEvent
     */
    public void setOnColumnAdded(final Consumer<MColumnEvent> columnAddedConsumer) { this.columnAddedConsumer = columnAddedConsumer; }

    /**
     * Sets the consumer that will be triggered if a column was removed from the matrix
     * @param columnRemovedConsumer A consumer of type MColumnEvent
     */
    public void setOnColumnRemoved(final Consumer<MColumnEvent> columnRemovedConsumer) { this.columnRemovedConsumer = columnRemovedConsumer; }

    /**
     * Sets the consumer that will be triggered if a row was added to the matrix
     * @param rowAddedConsumer A consumer of type MRowEvent
     */
    public void setOnRowAdded(final Consumer<MRowEvent> rowAddedConsumer) { this.rowAddedConsumer = rowAddedConsumer; }

    /**
     * Sets the consumer that will be triggered if a row was removed from the matrix
     * @param rowRemovedConsumer A consumer of type MRowEvent
     */
    public void setOnRowRemoved(final Consumer<MRowEvent> rowRemovedConsumer) { this.rowRemovedConsumer = rowRemovedConsumer; }

    /**
     * Sets the consumer that will be triggered if the number of columns in the matrix changed
     * @param columnsChangedConsumer A consumer of type MColumnsEvent
     */
    public void setOnNoOfColumnsChanged(final Consumer<MColumnsEvent> columnsChangedConsumer) { this.columnsChangedConsumer = columnsChangedConsumer; }

    /**
     * Sets the consumer that will be triggered if the number of rows in the matrix changed
     * @param rowsChangedConsumer A consumer of type MRowsEvent
     */
    public void setOnNoOfRowsChanged(final Consumer<MRowsEvent> rowsChangedConsumer) { this.rowsChangedConsumer = rowsChangedConsumer; }


    /**
     * Add the given MatrixObserver<T> for the given TYPE to the map of observers
     * @param type     The type of event that should be observed
     * @param observer An MObserver<T>
     */
    public void setOnMEvent(final MEventType<? extends MEvent> type, final MObserver observer) {
        if (!observers.keySet().contains(type.getName())) { observers.put(type.getName(), new CopyOnWriteArrayList<>()); }
        if (!observers.get(type.getName()).contains(observer)) { observers.get(type.getName()).add(observer); }
    }

    /**
     * Remove the given MObserver<T> for the given TYPE from the map of observers
     * @param type     The type of event that should be observed
     * @param observer An MObserver<T>
     */
    public void removeOnMEvent(final MEventType<? extends MEvent> type, final MObserver observer) {
        if (!observers.keySet().contains(type.getName())) { return; }
        if (observers.get(type.getName()).contains(observer)) { observers.get(type.getName()).remove(observer); }
    }

    /**
     * Removes all observers
     */
    public void removeAllObservers() { observers.entrySet().forEach(entry -> entry.getValue().clear()); }


    void fireEvent(final MEvent event) {
        final MEventType<? extends MEvent> type = event.getEventType();

        // Trigger specific observers first
        observers.entrySet()
                 .stream()
                 .filter(entry -> entry.getKey().equals(type.getName()))
                 .forEach(entry -> entry.getValue().forEach(observer -> observer.handle(event)));

        // Trigger all observers that subscribed to .ANY event types
        observers.entrySet()
                 .stream()
                 .filter(entry -> entry.getKey().equals(MEvent.ANY.getName()))
                 .forEach(entry -> entry.getValue().forEach(observer -> observer.handle(event)));

        if (ITEM_ADDED.equals(type) || ITEM_CHANGED.equals(type) || ITEM_REMOVED.equals(type)) {
            observers.entrySet()
                     .stream()
                     .filter(entry -> entry.getKey().equals(MItemEvent.ANY.getName()))
                     .forEach(entry -> entry.getValue().forEach(observer -> observer.handle(event)));
        } else if (COLUMN_ADDED.equals(type) || COLUMN_REMOVED.equals(type)) {
            observers.entrySet()
                     .stream()
                     .filter(entry -> entry.getKey().equals(MColumnEvent.ANY.getName()))
                     .forEach(entry -> entry.getValue().forEach(observer -> observer.handle(event)));
        } else if (ROW_ADDED.equals(type) || ROW_REMOVED.equals(type)) {
            observers.entrySet()
                     .stream()
                     .filter(entry -> entry.getKey().equals(MRowEvent.ANY.getName()))
                     .forEach(entry -> entry.getValue().forEach(observer -> observer.handle(event)));
        }
    }


    @Override public String toString() {
        StringBuilder output = new StringBuilder();
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                output.append(matrix[x][y]).append(" ");
            }
            output.append("\n");
        }
        return output.toString();
    }
}
