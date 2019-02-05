## Observable Matrix

An array based observable matrix implemented in Java.

It will fire the following events:
- MColumnEvent (COLUMN_ADDED, COLUMN_REMOVED)
- MRowEvent (ROW_ADDED, ROW_REMOVED)
- MItemEvent (ITEM_ADDED, ITEM_REMOVED, ITEM_CHANGED)
- MColumnsEvent (NO_OF_COLUMNS_CHANGED)
- MRowsEvent (NO_OF_ROWS_CHANGED)

All events are extending MEvent and so you could either 
subscribe to specific events e.g.
```Java
matrix.setOnItemChanged(e -> { ... })

matrix.setOnMEvent(MItemEvent.ITEM_CHANGED, event -> { ... });
```
or you could subscribe to a group of event e.g.
```Java
matrix.setOnMEvent(MItemEvent.ANY, event -> { ... });
```

The matrix can hold objects of any type, initialize it 
as follows
```Java
// Matrix with Integer objects
ObservableMatrix<Integer> integerMatrix = new ObservableMatrix<>(Integer.class, 3, 2);

// Matrix with JavaFX Label objects
ObservableMatrix<Label> labelMatrix = new ObservableMatrix<>(Label.class, 5, 5);

```

The matrix supports methods for the following operations:
- getItemAt(int x, int y) 
- setItemAt(int x, int y, T item) {
- removeItemAt(int x, int y) {
- removeItem(T item) {
- contains(T item) {
- getIndicesOf(T item) {
- getMatrix() { return matrix; }
- getAllItems() { return stream().filter(Objects::nonNull).collect(Collectors.toList()); }
- stream()
- reset()
- getCol(int col)
- getRow(int row)
- isColEmpty(int col)
- isRowEmpty(int row)
- getNoOfCols()
- setCols(int cols)
- addCol(int at, Supplier<T> itemSupplier)
- addCol(int at, List<T> items)
- addNullCol(int at)
- removeCol(int at)
- addRow(int at, Supplier<T> itemSupplier)
- addRow(int at, List<T> items)
- addNullRow(int at)
- removeRow(int at)
- getNoOfRows()
- setRows(int rows)
- getAllColumns()
- getAllEmptyColumns()
- getAllRows()
- getAllEmptyRows()
- getResizeMatrixWhenInnerRowOrColIsRemoved()
- setResizeMatrixWhenInnerRowOrColIsRemoved(boolean resize)
 
 
 Please find a little example in the Demo class.