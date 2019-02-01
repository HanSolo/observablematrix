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

package eu.hansolo.observablematrix.event;

public class MItemEvent<T> extends MEvent {
    public static final MEventType<MItemEvent> ANY          = new MEventType<>(MEvent.ANY, "ITEM_ANY");
    public static final MEventType<MItemEvent> ITEM_ADDED   = new MEventType<>(MItemEvent.ANY, "ITEM_ADDED");
    public static final MEventType<MItemEvent> ITEM_CHANGED = new MEventType<>(MItemEvent.ANY, "ITEM_CHANGED");
    public static final MEventType<MItemEvent> ITEM_REMOVED = new MEventType<>(MItemEvent.ANY, "ITEM_REMOVED");

    private final int x;
    private final int y;
    private final T   oldItem;
    private final T   item;


    // ******************** Constructors **************************************
    public MItemEvent(final Object source, final MEventType<? extends MItemEvent> eventType, final int x, final int y, final T oldItem, final T item) {
        super(source, eventType);
        this.x       = x;
        this.y       = y;
        this.oldItem = oldItem;
        this.item    = item;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the column of the item that was added/removed/changed in the matrix
     * @return the column of the item that was added/removed/changed in the matrix
     */
    public int getX() { return x; }

    /**
     * Returns the row of the item that was added/removed/changed in the matrix
     * @return the row of the item that was added/removed/changed in the matrix
     */
    public int getY() { return y; }

    /**
     * Returns the former item that was replaced by the new item in the matrix
     * @return the former item that was replaced by the new item in the matrix
     */
    public T getOldItem() { return oldItem; }

    /**
     * Returns the item that replaces the oldItem in the matrix
     * @return the item that replaces the oldItem in the matrix
     */
    public T getItem() { return item; }


    @Override public MEventType<? extends MItemEvent> getEventType() {
        return (MEventType<? extends MItemEvent>) super.getEventType();
    }
}
