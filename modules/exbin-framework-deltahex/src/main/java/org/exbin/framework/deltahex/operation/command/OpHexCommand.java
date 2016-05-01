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
package org.exbin.framework.deltahex.operation.command;

import org.exbin.deltahex.component.Hexadecimal;
import org.exbin.framework.deltahex.operation.HexOperation;
import org.exbin.xbup.operation.OperationEvent;
import org.exbin.xbup.operation.OperationListener;
import org.exbin.xbup.operation.XBTDocOperation;

/**
 * Abstract class for operation on hexadecimal document.
 *
 * @version 0.1.0 2016/04/30
 * @author ExBin Project (http://exbin.org)
 */
public abstract class OpHexCommand extends HexCommand {

    protected HexOperation operation;
    private boolean isUndoMode = false;

    public OpHexCommand(Hexadecimal hexadecimal) {
        super(hexadecimal);
    }

    public HexOperation getOperation() {
        return operation;
    }

    public void setOperation(HexOperation operation) {
        this.operation = operation;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public void undo() throws Exception {
        if (isUndoMode) {
            HexOperation redoOperation = (HexOperation) operation.executeWithUndo();
            if (hexadecimal instanceof OperationListener) {
                ((OperationListener) hexadecimal).notifyChange(new OperationEvent(operation));
            }

            operation = redoOperation;
            isUndoMode = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() throws Exception {
        if (!isUndoMode) {
            HexOperation undoOperation = (HexOperation) operation.executeWithUndo();
            if (hexadecimal instanceof OperationListener) {
                ((OperationListener) hexadecimal).notifyChange(new OperationEvent(operation));
            }

            operation = undoOperation;
            isUndoMode = true;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}