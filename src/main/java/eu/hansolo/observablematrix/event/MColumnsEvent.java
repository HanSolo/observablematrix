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

public class MColumnsEvent extends MEvent {
    public static final MEventType<MColumnsEvent> ANY                   = new MEventType<>(MEvent.ANY, "COLUMNS_ANY");
    public static final MEventType<MColumnsEvent> NO_OF_COLUMNS_CHANGED = new MEventType<>(MColumnsEvent.ANY, "NO_OF_COLUMNS_CHANGED");
    public static final MEventType<MColumnsEvent> COLUMNS_MIRRORED      = new MEventType<>(MColumnsEvent.ANY, "COLUMNS_MIRRORED");

    private final int noOfColumns;


    // ******************** Constructors **************************************
    public MColumnsEvent(final Object source, final MEventType<? extends MColumnsEvent> eventType, final int noOfColumns) {
        super(source, eventType);
        this.noOfColumns = noOfColumns;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the number of columns of the matrix that was affected
     * @return the number of columns of the matrix that was affected
     */
    public int getNoOfColumns() { return noOfColumns; }


    @Override public MEventType<? extends MColumnsEvent> getEventType() {
        return (MEventType<? extends MColumnsEvent>) super.getEventType();
    }
}
