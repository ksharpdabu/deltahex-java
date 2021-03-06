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
package org.exbin.deltahex.operation;

import org.exbin.deltahex.CodeArea;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for inserting data.
 *
 * @version 0.1.0 2015/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class InsertDataOperation extends CodeAreaOperation {

    private final long position;
    private final int codeOffset;
    private final BinaryData data;

    public InsertDataOperation(CodeArea codeArea, long position, int codeOffset, BinaryData data) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.data = data;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.INSERT_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public CodeAreaOperation executeWithUndo() throws Exception {
        return execute(true);
    }

    private CodeAreaOperation execute(boolean withUndo) {
        CodeAreaOperation undoOperation = null;
        ((EditableBinaryData) codeArea.getData()).insert(position, data);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(codeArea, position, codeOffset, data.getDataSize());
        }
        codeArea.getCaret().setCaretPosition(position + data.getDataSize(), codeOffset);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
    }
}
