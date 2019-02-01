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

public class MColumnEvent extends MEvent {
    public static final MEventType<MColumnEvent> ANY            = new MEventType<>(MEvent.ANY, "COLUMN_ANY");
    public static final MEventType<MColumnEvent> COLUMN_ADDED   = new MEventType<>(MColumnEvent.ANY, "COLUMN_ADDED");
    public static final MEventType<MColumnEvent> COLUMN_REMOVED = new MEventType<>(MColumnEvent.ANY, "COLUMN_REMOVED");

    private final int column;


    // ******************** Constructors **************************************
    public MColumnEvent(final Object source, final MEventType<? extends MColumnEvent> eventType, final int column) {
        super(source, eventType);
        this.column = column;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the column of the matrix that was affected
     * @return the column of the matrix that was affected
     */
    public int getColumn() { return column; }


    @Override public MEventType<? extends MColumnEvent> getEventType() {
        return (MEventType<? extends MColumnEvent>) super.getEventType();
    }
}
