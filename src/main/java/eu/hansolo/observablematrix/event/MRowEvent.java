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

public class MRowEvent extends MEvent {
    public static final MEventType<MRowEvent> ANY         = new MEventType<>(MEvent.ANY, "ROW_ANY");
    public static final MEventType<MRowEvent> ROW_ADDED   = new MEventType<>(MRowEvent.ANY, "ROW_ADDED");
    public static final MEventType<MRowEvent> ROW_REMOVED = new MEventType<>(MRowEvent.ANY, "ROW_REMOVED");

    private final int row;


    // ******************** Constructors **************************************
    public MRowEvent(final Object source, final MEventType<? extends MRowEvent> eventType, final int row) {
        super(source, eventType);
        this.row = row;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the row of the matrix that was affected
     * @return the row of the matrix that was affected
     */
    public int getRow() { return row; }


    @Override public MEventType<? extends MRowEvent> getEventType() {
        return (MEventType<? extends MRowEvent>) super.getEventType();
    }
}
