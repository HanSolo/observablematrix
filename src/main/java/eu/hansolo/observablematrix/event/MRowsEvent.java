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

public class MRowsEvent extends MEvent {
    public static final MEventType<MRowsEvent> ANY                = new MEventType<>(MEvent.ANY, "ROWS_ANY");
    public static final MEventType<MRowsEvent> NO_OF_ROWS_CHANGED = new MEventType<>(MRowsEvent.ANY, "NO_OF_ROWS_CHANGED");
    public static final MEventType<MRowsEvent> ROWS_MIRRORED      = new MEventType<>(MRowsEvent.ANY, "ROWS_MIRRORED");

    private final int noOfRows;


    // ******************** Constructors **************************************
    public MRowsEvent(final Object source, final MEventType<? extends MRowsEvent> eventType, final int noOfRows) {
        super(source, eventType);
        this.noOfRows = noOfRows;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the number of rows of the matrix that was affected
     * @return the number of rows of the matrix that was affected
     */
    public int getNoOfRows() { return noOfRows; }


    @Override public MEventType<? extends MRowsEvent> getEventType() {
        return (MEventType<? extends MRowsEvent>) super.getEventType();
    }
}
