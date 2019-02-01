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

public class MEventType<T extends MEvent> {
    public  static final MEventType<MEvent>    ROOT = new MEventType<>("EVENT", null);
    private        final MEventType<? super T> superType;
    private        final String                name;


    // ******************** Constructors **************************************
    public MEventType(final String name) {
        this(ROOT, name);
    }
    public MEventType(final MEventType<? super T> superType) {
        this(superType, null);
    }
    public MEventType(final MEventType<? super T> superType, final String name) {
        if (null == superType) { throw new NullPointerException("Event super type must not be null"); }
        this.superType = superType;
        this.name      = name;
    }
    MEventType(final String name, final MEventType<? super T> superType) {
        this.superType = superType;
        this.name      = name;
    }


    // ******************** Methods *******************************************
    public final MEventType<? super T> getSuperType() { return superType; }

    public final String getName() { return name; }
}
