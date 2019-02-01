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
- getItemAt(int col, int row)
- setItemAt(int col, int row)
- removeItemAt(int col, int row)
- removeItem(T item)
- contains(T item)
- getIndicesOf(T item)
- getAllItems()
- stream()
- clear()
- getCol(int index)
- getRow(int index)
- isColEmpty()
- isRowEmpty()
- getCols()
- setCols(int cols)
- getRows()
- setRows(int rows)
- addCol(int at, List items)
- addNullCol(int at)
- removeCol(int at)
- addRow(int at, List items)
- addNullRow(int at)
- removeRow(int at)
- getAllColumns()
- getAllRows()
 
 
 Please find a little example in the Demo class.