/*
 * Copyright (C) ExBin Project
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
package org.exbin.deltahex.delta;

import org.exbin.deltahex.delta.list.DoublyLinkedItem;

/**
 * Abstract data segment of delta data source.
 *
 * @version 0.1.0 2016/06/07
 * @author ExBin Project (http://exbin.org)
 */
public abstract class DataSegment implements DoublyLinkedItem {

    private DataSegment previous;
    private DataSegment next;

    public DataSegment() {
    }

    /**
     * Returns length of this segment in bytes.
     *
     * @return length of this segment
     */
    public abstract long getLength();

    /**
     * Returns detached copy of this segment.
     *
     * @return copy of this segment
     */
    public abstract DataSegment copy();

    @Override
    public DataSegment getNext() {
        return next;
    }

    @Override
    public void setNext(DoublyLinkedItem next) {
        this.next = (DataSegment) next;
    }

    @Override
    public DoublyLinkedItem getPrev() {
        return previous;
    }

    @Override
    public void setPrev(DoublyLinkedItem previous) {
        this.previous = (DataSegment) previous;
    }
}
