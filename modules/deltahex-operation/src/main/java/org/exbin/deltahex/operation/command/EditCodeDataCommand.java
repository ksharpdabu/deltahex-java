/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.deltahex.operation.command;

import org.exbin.deltahex.CodeArea;
import org.exbin.deltahex.operation.DeleteCodeEditDataOperation;
import org.exbin.deltahex.operation.CodeEditDataOperation;
import org.exbin.deltahex.operation.InsertCodeEditDataOperation;
import org.exbin.deltahex.operation.OverwriteCodeEditDataOperation;
import org.exbin.deltahex.operation.CodeAreaOperation;
import org.exbin.deltahex.operation.CodeAreaOperationEvent;
import org.exbin.xbup.operation.OperationListener;
import org.exbin.deltahex.operation.CodeAreaOperationListener;

/**
 * Command for editing data in hexadecimal mode.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class EditCodeDataCommand extends EditDataCommand {

    private final EditCommandType commandType;
    protected boolean operationPerformed = false;
    private CodeAreaOperation[] operations = null;

    public EditCodeDataCommand(CodeArea codeArea, EditCommandType commandType, long position, int positionCodeOffset) {
        super(codeArea);
        this.commandType = commandType;
        CodeAreaOperation operation;
        switch (commandType) {
            case INSERT: {
                operation = new InsertCodeEditDataOperation(codeArea, position, positionCodeOffset);
                break;
            }
            case OVERWRITE: {
                operation = new OverwriteCodeEditDataOperation(codeArea, position, positionCodeOffset);
                break;
            }
            case DELETE: {
                operation = new DeleteCodeEditDataOperation(codeArea, position);
                break;
            }
            default: {
                throw new IllegalStateException("Unsupported command type " + commandType.name());
            }
        }
        operations = new CodeAreaOperation[]{operation};
        operationPerformed = true;
    }

    @Override
    public void undo() throws Exception {
        if (operations.length == 1 && operations[0] instanceof CodeEditDataOperation) {
            CodeAreaOperation operation = operations[0];
            operations = ((CodeEditDataOperation) operation).generateUndo();
        }

        if (operationPerformed) {
            for (int i = operations.length - 1; i >= 0; i--) {
                CodeAreaOperation redoOperation = operations[i].executeWithUndo();
                if (codeArea instanceof OperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operations[i]));
                }
                operations[i] = redoOperation;
            }
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() throws Exception {
        if (!operationPerformed) {
            for (int i = 0; i < operations.length; i++) {
                CodeAreaOperation undoOperation = operations[i].executeWithUndo();
                if (codeArea instanceof OperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operations[i]));
                }

                operations[i] = undoOperation;
            }
            operationPerformed = true;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    /**
     * Appends next hexadecimal value in editing action sequence.
     *
     * @param value half-byte value (0..15)
     */
    public void appendEdit(byte value) {
        if (operations.length == 1 && operations[0] instanceof CodeEditDataOperation) {
            ((CodeEditDataOperation) operations[0]).appendEdit(value);
        } else {
            throw new IllegalStateException("Cannot append edit on reverted command");
        }
    }

    @Override
    public EditCommandType getCommandType() {
        return commandType;
    }

    @Override
    public boolean wasReverted() {
        return !(operations.length == 1 && operations[0] instanceof CodeEditDataOperation);
    }
}
