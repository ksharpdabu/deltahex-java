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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access window for delta data.
 *
 * @version 0.1.0 2016/06/03
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDataPageWindow {

    public static final int PAGE_SIZE = 1024;

    private final DeltaDataSource data;
    private final DataPage[] dataPages = new DataPage[]{new DataPage(), new DataPage()};
    private int activeDataPage = 1;

    public DeltaDataPageWindow(DeltaDataSource data) {
        this.data = data;
        loadPage(0);
    }

    private void loadPage(int index) {
        long pageIndex = dataPages[index].pageIndex;
        long pagePosition = pageIndex * PAGE_SIZE;
        RandomAccessFile file = data.getFile();
        try {
            file.seek(pagePosition);
            byte[] page = dataPages[index].page;
            int offset = 0;
            int toRead = PAGE_SIZE;
            if (pagePosition + PAGE_SIZE > file.length()) {
                toRead = (int) (file.length() - pagePosition);
            }
            while (toRead > 0) {
                int red = file.read(page, offset, toRead);
                toRead -= red;
                offset += red;
            }
        } catch (IOException ex) {
            Logger.getLogger(DeltaDataPageWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte getByte(long position) {
        long targetPageIndex = position / PAGE_SIZE;
        int index = -1;
        long pageIndex1 = dataPages[0].pageIndex;
        long pageIndex2 = dataPages[1].pageIndex;
        if (pageIndex1 == targetPageIndex) {
            index = 0;
        } else if (pageIndex2 == targetPageIndex) {
            index = 1;
        }
        if (index == -1) {
            DataPage dataPage = dataPages[activeDataPage];
            dataPage.pageIndex = targetPageIndex;
            loadPage(activeDataPage);
            activeDataPage = (activeDataPage + 1) & 1;
            return dataPage.page[(int) (position % PAGE_SIZE)];
        }

        return dataPages[index].page[(int) (position % PAGE_SIZE)];
    }

    /**
     * Simple structure for data page.
     */
    private class DataPage {

        public DataPage() {
            page = new byte[PAGE_SIZE];
        }

        long pageIndex;
        byte[] page;
    }
}
