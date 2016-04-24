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
package org.exbin.deltahex.component;

import org.exbin.deltahex.CaretPosition;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import org.exbin.deltahex.HexadecimalCommandHandler;
import org.exbin.deltahex.HexadecimalData;

/**
 * Hex editor component.
 *
 * @version 0.1.0 2016/04/20
 * @author ExBin Project (http://exbin.org)
 */
public class Hexadecimal extends JComponent {

    private HexadecimalData data;
    public static final int NO_MODIFIER = 0;
    private static final int MOUSE_SCROLL_LINES = 3;

    private HexadecimalCaret caret;
    private SelectionRange selection;
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();

    private ViewMode viewMode = ViewMode.DUAL;
    private BackgroundMode backgroundMode = BackgroundMode.STRIPPED;
    private DecorationMode decorationMode = DecorationMode.LINES;
    private Section activeSection = Section.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.OVERWRITE;
    private CharRenderingMode charRenderingMode = CharRenderingMode.AUTO;
    private CharAntialiasingMode charAntialiasingMode = CharAntialiasingMode.AUTO;
    private int lineLength = 16;
    private boolean showHeader = true;
    private boolean showLineNumbers = true;
    private boolean mouseDown;
    private boolean editable = true;
    private boolean wrapMode = false;

    private VerticalScrollMode verticalScrollMode = VerticalScrollMode.PER_LINE;
    private HorizontalScrollMode horizontalScrollMode = HorizontalScrollMode.PIXEL;
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private ScrollPosition scrollPosition = new ScrollPosition();

    private HexadecimalPainter painter;
    private HexadecimalCommandHandler commandHandler;

    private Color oddForegroundColor;
    private Color oddBackgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    /**
     * Selection background color for selection in currently not active section.
     */
    private Color dualSelectionBackgroundColor;
    private Color cursorColor;
    private int subFontSpace = 3;

    private final DimensionsCache dimensionsCache = new DimensionsCache();

    public Hexadecimal() {
        super();
        caret = new HexadecimalCaret(this);
        painter = new DefaultHexadecimalPainter(this);
        commandHandler = new DefaultCommandHandler(this);

        super.setForeground(UIManager.getColor("TextArea.foreground"));
        super.setBackground(UIManager.getColor("TextArea.background"));
        oddBackgroundColor = new Color(240, 240, 240);
        selectionColor = UIManager.getColor("TextArea.selectionForeground");
        selectionBackgroundColor = UIManager.getColor("TextArea.selectionBackground");
        dualSelectionBackgroundColor = Color.LIGHT_GRAY;
        cursorColor = UIManager.getColor("TextArea.caretForeground");

        init();
    }

    private void init() {
        verticalScrollBar = new JScrollBar(Scrollbar.VERTICAL);
        verticalScrollBar.setVisible(false);
        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
        add(verticalScrollBar);
        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.setVisible(false);
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        add(horizontalScrollBar);

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addComponentListener(new HexComponentListener());

        HexMouseListener hexMouseListener = new HexMouseListener();
        addMouseListener(hexMouseListener);
        addMouseMotionListener(hexMouseListener);
        addMouseWheelListener(hexMouseListener);
        addKeyListener(new HexKeyListener());
    }

    void moveRight(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() < data.getDataSize()) {
            if (activeSection == Section.HEXADECIMAL) {
                boolean lowerHalf = caret.isLowerHalf();
                if (caretPosition.getDataPosition() < data.getDataSize()) {
                    if (!lowerHalf) {
                        caret.setLowerHalf(true);
                        updateSelection(modifiers, caretPosition);
                    } else {
                        caret.setCaretPosition(caretPosition.getDataPosition() + 1, false);
                        updateSelection(modifiers, caretPosition);
                    }
                    notifyCaretMoved();
                }
            } else {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        }
    }

    void moveLeft(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (activeSection == Section.HEXADECIMAL) {
            boolean lowerHalf = caret.isLowerHalf();
            if (lowerHalf) {
                caret.setLowerHalf(false);
                updateSelection(modifiers, caretPosition);
            } else if (caretPosition.getDataPosition() > 0) {
                caret.setCaretPosition(caretPosition.getDataPosition() - 1, true);
                updateSelection(modifiers, caretPosition);
            }
        } else if (caretPosition.getDataPosition() > 0) {
            caret.setCaretPosition(caretPosition.getDataPosition() - 1);
            updateSelection(modifiers, caretPosition);
        }
        notifyCaretMoved();
    }

    HexadecimalCaret getCaret() {
        return caret;
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        Point scrollPoint = getScrollPoint();
        int bytesPerLine = dimensionsCache.bytesPerLine;
        int cursorCharX = (me.getX() + scrollPoint.x) / dimensionsCache.charWidth;
        Rectangle rect = dimensionsCache.hexadecimalRectangle;
        int cursorLineY = (me.getY() - rect.y + scrollPoint.y) / dimensionsCache.lineHeight;
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (showLineNumbers) {
            if (cursorCharX < 9) {
                cursorCharX = 0;
            } else {
                cursorCharX -= 9;
            }
        }

        long dataPosition;
        boolean lowerHalf = false;
        int byteOnLine;
        if ((viewMode == ViewMode.DUAL && cursorCharX < bytesPerLine * 3) || viewMode == ViewMode.HEXADECIMAL) {
            activeSection = Section.HEXADECIMAL;
            int bytePosition = cursorCharX % 3;
            lowerHalf = bytePosition > 0;

            byteOnLine = cursorCharX / 3;
            if (byteOnLine >= bytesPerLine) {
                lowerHalf = true;
            }
        } else {
            activeSection = Section.PREVIEW;
            byteOnLine = cursorCharX;
            if (viewMode == ViewMode.DUAL) {
                byteOnLine -= bytesPerLine * 3;
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine);
        long dataSize = data.getDataSize();
        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            lowerHalf = false;
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, lowerHalf);
        notifyCaretMoved();
        commandHandler.caretMoved();

        updateSelection(modifiers, caretPosition);
    }

    public Point getScrollPoint() {
        return new Point((int) scrollPosition.scrollBytePosition * dimensionsCache.charWidth + scrollPosition.scrollByteOffset, (int) scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset);
    }

    public ScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public void paintComponent(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
//        super.paintComponent(g);
//        horizontalScrollBar.paint(g);
//        verticalScrollBar.repaint();

        if (charAntialiasingMode != CharAntialiasingMode.OFF && g instanceof Graphics2D) {
            Object antialiasingHint;
            switch (charAntialiasingMode) {
                case AUTO: {
                    // TODO detect if display is LCD?
                    if (((Graphics2D) g).getDeviceConfiguration().getDevice().getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                        antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                    } else {
                        antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
                    }
                    break;
                }
                case BASIC: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                    break;
                }
                case GASP: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
                    break;
                }
                case DEFAULT: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
                    break;
                }
                case LCD_HRGB: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                    break;
                }
                case LCD_HBGR: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
                    break;
                }
                case LCD_VRGB: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
                    break;
                }
                case LCD_VBGR: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected antialiasing type " + charAntialiasingMode.name());
                }
            }
            ((Graphics2D) g).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    antialiasingHint);
        }

        g.setFont(getFont());

        if (dimensionsCache.fontMetrics == null) {
            computeFontMetrics();
        }

        painter.paintOverall(g);
        Rectangle rect = dimensionsCache.hexadecimalRectangle;
        if (showHeader) {
            g.setClip(rect.x, 0, rect.width, rect.y);
            painter.paintHeader(g);
        }

        g.setClip(0, rect.y, rect.x + rect.width, rect.height);
        painter.paintBackground(g);
        if (showLineNumbers) {
            painter.paintLineNumbers(g);
            g.setClip(rect.x, rect.y, rect.width, rect.height);
        }

        painter.paintHexadecimal(g);

        caret.paint(g);
        g.setClip(clipBounds);
    }

    public void revealCursor() {
        boolean scrolled = false;
        CaretPosition caretPosition = caret.getCaretPosition();
        long caretLine = caretPosition.getDataPosition() / dimensionsCache.bytesPerLine;
        int caretByte = (int) (caretPosition.getDataPosition() % dimensionsCache.bytesPerLine);

        if (caretLine <= scrollPosition.scrollLinePosition) {
            scrollPosition.scrollLinePosition = caretLine;
            scrollPosition.scrollLineOffset = 0;
            scrolled = true;
        } else if (caretLine >= scrollPosition.scrollLinePosition + dimensionsCache.linesPerRect) {
            scrollPosition.scrollLinePosition = caretLine - dimensionsCache.linesPerRect + 1;
            scrollPosition.scrollLineOffset = 0; // TODO
            scrolled = true;
        }
        if (caretByte <= scrollPosition.scrollBytePosition) {
            scrollPosition.scrollBytePosition = caretByte;
            scrollPosition.scrollByteOffset = 0;
            scrolled = true;
        } else {
            scrolled |= scrolled; // TODO remove
            // TODO int visibleChars = dimensionsCache.hexadecimalRectangle.width / (dimensionsCache.charWidth * 3)

            if (caretByte >= scrollPosition.scrollBytePosition + dimensionsCache.bytesPerLine) {
                scrollPosition.scrollBytePosition = caretByte - dimensionsCache.bytesPerLine;
                scrollPosition.scrollByteOffset = 0; // TODO
                scrolled = true;
            }
        }

        if (scrolled) {
            updateScrollBars();
        }
    }

    void updateScrollBars() {
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            verticalScrollBar.setValue((int) scrollPosition.scrollLinePosition);
        } else {
            verticalScrollBar.setValue((int) (scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset));
        }

        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            horizontalScrollBar.setValue((int) scrollPosition.scrollBytePosition);
        } else {
            horizontalScrollBar.setValue((int) (scrollPosition.scrollBytePosition * dimensionsCache.charWidth * 3 + scrollPosition.scrollByteOffset));
        }
        repaint();
    }

    private void updateSelection(int modifiers, CaretPosition caretPosition) {
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            long currentPosition = caret.getDataPosition();
            long end = currentPosition;
            long start;
            if (selection != null) {
                start = selection.start;
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    selection.end = start < currentPosition ? end - 1 : end;
                }
            } else {
                start = caretPosition.getDataPosition();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    selection = new SelectionRange(start, start < currentPosition ? end - 1 : end);
                }
            }

            notifySelectionChanged();
        } else {
            clearSelection();
        }
        repaint();
    }

    public SelectionRange getSelection() {
        return selection;
    }

    public void selectAll() {
        long dataSize = data.getDataSize();
        if (dataSize > 0) {
            selection = new SelectionRange(0, dataSize - 1);
            notifySelectionChanged();
            repaint();
        }
    }

    public void clearSelection() {
        selection = null;
        notifySelectionChanged();
        repaint();
    }

    private void notifySelectionChanged() {
        for (SelectionChangedListener selectionChangedListener : selectionChangedListeners) {
            selectionChangedListener.selectionChanged(selection);
        }
    }

    public boolean hasSelection() {
        return selection != null;
    }

    public void setSelection(SelectionRange selection) {
        this.selection = selection;
        notifySelectionChanged();
    }

    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    private void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : caretMovedListeners) {
            caretMovedListener.caretMoved(caret.getCaretPosition(), activeSection);
        }
    }

    public void addCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void removeCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    /**
     * Returns main hexadecimal area rectangle.
     *
     * @return rectangle of main hexadecimal area
     */
    public Rectangle getHexadecimalRectangle() {
        return dimensionsCache.hexadecimalRectangle;
    }

    /**
     * Returns X start position of the ascii preview area.
     *
     * @return X position or -1 if area not present
     */
    public int getPreviewX() {
        return dimensionsCache.previewX;
    }

    public int getLineHeight() {
        return dimensionsCache.lineHeight;
    }

    public int getBytesPerBounds() {
        return dimensionsCache.bytesPerLine;
    }

    public int getCharWidth() {
        return dimensionsCache.charWidth;
    }

    public HexadecimalData getData() {
        return data;
    }

    public void setData(HexadecimalData data) {
        this.data = data;
        computeDimensions();
        repaint();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        computeFontMetrics();
    }

    private void computeFontMetrics() {
        Graphics g = getGraphics();
        if (g != null) {
            Font font = getFont();
            dimensionsCache.fontMetrics = g.getFontMetrics(font);
            /**
             * Use small 'w' character to guess normal font width.
             */
            dimensionsCache.charWidth = dimensionsCache.fontMetrics.charWidth('w');
            /**
             * Compare it to small 'i' to detect if font is monospaced.
             *
             * TODO: Is there better way?
             */
            dimensionsCache.monospaced = dimensionsCache.charWidth == dimensionsCache.fontMetrics.charWidth('i');
            int fontHeight = font.getSize();
            dimensionsCache.lineHeight = fontHeight + subFontSpace;
            computeDimensions();
        }
    }

    void computeDimensions() {
        if (dimensionsCache.fontMetrics == null) {
            return;
        }
        dimensionsCache.charsPerByte = viewMode.charsPerByte;

        boolean verticalScrollBarVisible;
        boolean horizontalScrollBarVisible;

        Rectangle panelBounds = getBounds();
        int charsPerRect = computeCharsPerRect(panelBounds.width);
        int bytesPerLine;
        if (wrapMode) {
            bytesPerLine = charsPerRect / dimensionsCache.charsPerByte;
            if (bytesPerLine == 0) {
                bytesPerLine = 1;
            }
        } else {
            bytesPerLine = lineLength;
        }
        int lines = (int) (data.getDataSize() / bytesPerLine) + 1;

        Rectangle rect = dimensionsCache.hexadecimalRectangle;
        rect.y = showHeader ? dimensionsCache.lineHeight * 2 : 0;
        rect.x = 0;

        dimensionsCache.linesPerRect = rect.height / dimensionsCache.lineHeight;
        verticalScrollBarVisible = verticalScrollMode != VerticalScrollMode.NONE && lines > dimensionsCache.linesPerRect;
        if (verticalScrollBarVisible) {
            charsPerRect = computeCharsPerRect(panelBounds.width - dimensionsCache.scrollBarThickness);
            if (wrapMode) {
                bytesPerLine = charsPerRect / dimensionsCache.charsPerByte;
                if (bytesPerLine <= 0) {
                    bytesPerLine = 1;
                }
                lines = (int) (data.getDataSize() / bytesPerLine) + 1;
            }
        }

        dimensionsCache.bytesPerLine = bytesPerLine;

        if (viewMode != ViewMode.PREVIEW) {
            if (showLineNumbers) {
                rect.x += dimensionsCache.charWidth * 9;
            }
        }

        int maxWidth = panelBounds.width - rect.x;
        if (verticalScrollBarVisible) {
            maxWidth -= dimensionsCache.scrollBarThickness;
        }
        horizontalScrollBarVisible = horizontalScrollMode != HorizontalScrollMode.NONE && dimensionsCache.bytesPerLine * dimensionsCache.charWidth * dimensionsCache.charsPerByte > maxWidth;
        if (horizontalScrollBarVisible) {
            dimensionsCache.linesPerRect = (rect.height - dimensionsCache.scrollBarThickness) / dimensionsCache.lineHeight;
        }

        rect.width = panelBounds.width - rect.x;
        if (verticalScrollBarVisible) {
            rect.width -= dimensionsCache.scrollBarThickness;
        }
        rect.height = panelBounds.height - rect.y;
        if (horizontalScrollBarVisible) {
            rect.height -= dimensionsCache.scrollBarThickness;
        }

        // Compute sections positions
        if (viewMode == ViewMode.HEXADECIMAL) {
            dimensionsCache.previewX = -1;
        } else {
            if (viewMode == ViewMode.PREVIEW) {
                dimensionsCache.previewX = 0;
            } else {
                dimensionsCache.previewX = dimensionsCache.bytesPerLine * dimensionsCache.charWidth * 3;
            }
            if (showLineNumbers) {
                dimensionsCache.previewX += dimensionsCache.charWidth * 9;
            }
        }

        int lineScroll = (int) (scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset);
        Rectangle bounds = getBounds();
        verticalScrollBar.setVisible(verticalScrollBarVisible);
        if (verticalScrollBarVisible) {
            int verticalScrollBarHeight = bounds.height - rect.y;
            if (horizontalScrollBarVisible) {
                verticalScrollBarHeight -= dimensionsCache.scrollBarThickness - 2;
            }
            verticalScrollBar.setBounds(bounds.width - dimensionsCache.scrollBarThickness, rect.y, dimensionsCache.scrollBarThickness, verticalScrollBarHeight);

            int verticalVisibleAmount;
            int verticalMaximum = lines;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                verticalVisibleAmount = rect.height;
                verticalMaximum *= dimensionsCache.lineHeight;
            } else {
                verticalVisibleAmount = rect.height / dimensionsCache.lineHeight;
            }
            verticalScrollBar.setMaximum(verticalMaximum);
            verticalScrollBar.setVisibleAmount(verticalVisibleAmount);

            int maxLineScroll = verticalMaximum - verticalVisibleAmount;
            if (lineScroll > maxLineScroll) {
                scrollPosition.scrollLinePosition = maxLineScroll / dimensionsCache.lineHeight;
                scrollPosition.scrollLineOffset = maxLineScroll % dimensionsCache.lineHeight;
                updateScrollBars();
            }
        } else if (lineScroll > 0) {
            scrollPosition.scrollLinePosition = 0;
            scrollPosition.scrollLineOffset = 0;
            updateScrollBars();
        }

        int byteScroll = (int) (scrollPosition.scrollBytePosition * dimensionsCache.charWidth + scrollPosition.scrollByteOffset);
        horizontalScrollBar.setVisible(horizontalScrollBarVisible);
        if (horizontalScrollBarVisible) {
            int horizontalScrollBarWidth = bounds.width - rect.x;
            if (verticalScrollBarVisible) {
                horizontalScrollBarWidth -= dimensionsCache.scrollBarThickness - 2;
            }
            horizontalScrollBar.setBounds(rect.x, bounds.height - dimensionsCache.scrollBarThickness, horizontalScrollBarWidth, dimensionsCache.scrollBarThickness);

            int horizontalVisibleAmount;
            int horizontalMaximum = dimensionsCache.bytesPerLine * dimensionsCache.charsPerByte;
            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                horizontalVisibleAmount = rect.width;
                horizontalMaximum *= dimensionsCache.charWidth;
            } else {
                horizontalVisibleAmount = rect.width / dimensionsCache.charWidth;
            }
            horizontalScrollBar.setMaximum(horizontalMaximum);
            horizontalScrollBar.setVisibleAmount(horizontalVisibleAmount);

            int maxByteScroll = horizontalMaximum - horizontalVisibleAmount;
            if (byteScroll > maxByteScroll) {
                scrollPosition.scrollBytePosition = maxByteScroll / dimensionsCache.charWidth;
                scrollPosition.scrollByteOffset = maxByteScroll % dimensionsCache.charWidth;
                updateScrollBars();
            }
        } else if (byteScroll > 0) {
            scrollPosition.scrollBytePosition = 0;
            scrollPosition.scrollByteOffset = 0;
            updateScrollBars();
        }
    }

    private int computeCharsPerRect(int width) {
        if (showLineNumbers) {
            width -= dimensionsCache.charWidth * 9;
        }

        return width / dimensionsCache.charWidth;
    }

    public Color getOddForegroundColor() {
        return oddForegroundColor;
    }

    public void setOddForegroundColor(Color oddForegroundColor) {
        this.oddForegroundColor = oddForegroundColor;
    }

    public Color getOddBackgroundColor() {
        return oddBackgroundColor;
    }

    public void setOddBackgroundColor(Color oddBackgroundColor) {
        this.oddBackgroundColor = oddBackgroundColor;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        this.selectionBackgroundColor = selectionBackgroundColor;
    }

    public Color getDualSelectionBackgroundColor() {
        return dualSelectionBackgroundColor;
    }

    public void setDualSelectionBackgroundColor(Color dualSelectionBackgroundColor) {
        this.dualSelectionBackgroundColor = dualSelectionBackgroundColor;
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == ViewMode.HEXADECIMAL) {
            activeSection = Section.HEXADECIMAL;
        } else if (viewMode == ViewMode.PREVIEW) {
            activeSection = Section.PREVIEW;
        }
        computeDimensions();
        repaint();
    }

    public BackgroundMode getBackgroundMode() {
        return backgroundMode;
    }

    public void setBackgroundMode(BackgroundMode backgroundMode) {
        this.backgroundMode = backgroundMode;
        repaint();
    }

    public DecorationMode getDecorationMode() {
        return decorationMode;
    }

    public void setDecorationMode(DecorationMode decorationMode) {
        this.decorationMode = decorationMode;
        repaint();
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public void setSubFontSpace(int subFontSpace) {
        this.subFontSpace = subFontSpace;
    }

    public Section getActiveSection() {
        return activeSection;
    }

    public void setActiveSection(Section activeSection) {
        this.activeSection = activeSection;
        repaint();
    }

    public EditationMode getEditationMode() {
        return editationMode;
    }

    public void setEditationMode(EditationMode editationMode) {
        this.editationMode = editationMode;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        computeDimensions();
        repaint();
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        computeDimensions();
        repaint();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        repaint();
    }

    public boolean isWrapMode() {
        return wrapMode;
    }

    public void setWrapMode(boolean wrapMode) {
        this.wrapMode = wrapMode;
        computeDimensions();
        repaint();
    }

    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
        if (!wrapMode) {
            computeDimensions();
            repaint();
        }
    }

    public CharRenderingMode getCharRenderingMode() {
        return charRenderingMode;
    }

    public boolean isCharFixedMode() {
        return charRenderingMode == CharRenderingMode.FIXED || (charRenderingMode == CharRenderingMode.AUTO && dimensionsCache.monospaced);
    }

    public void setCharRenderingMode(CharRenderingMode charRenderingMode) {
        this.charRenderingMode = charRenderingMode;
        computeFontMetrics();
        repaint();
    }

    public CharAntialiasingMode getCharAntialiasingMode() {
        return charAntialiasingMode;
    }

    public void setCharAntialiasingMode(CharAntialiasingMode charAntialiasingMode) {
        this.charAntialiasingMode = charAntialiasingMode;
        repaint();
    }

    public VerticalScrollMode getVerticalScrollMode() {
        return verticalScrollMode;
    }

    public void setVerticalScrollMode(VerticalScrollMode verticalScrollMode) {
        this.verticalScrollMode = verticalScrollMode;
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            scrollPosition.scrollLineOffset = 0;
        }
        computeDimensions();
        updateScrollBars();
    }

    public HorizontalScrollMode getHorizontalScrollMode() {
        return horizontalScrollMode;
    }

    public void setHorizontalScrollMode(HorizontalScrollMode horizontalScrollMode) {
        this.horizontalScrollMode = horizontalScrollMode;
        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            scrollPosition.scrollByteOffset = 0;
        }
        computeDimensions();
        updateScrollBars();
    }

    public boolean isLowerHalf() {
        return caret.isLowerHalf();
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    public void copy() {
        commandHandler.copy();
    }

    public void cut() {
        commandHandler.cut();
    }

    public void paste() {
        commandHandler.paste();
    }

    public void delete() {
        commandHandler.delete();
    }

    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    /**
     * Selection range is selection between two points where begin represents
     * originating point. End of the selection can be before or after begin.
     */
    public static class SelectionRange {

        private long start;
        private long end;

        public SelectionRange() {
        }

        public SelectionRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public long getFirst() {
            return end >= start ? start : end;
        }

        public long getLast() {
            return end >= start ? end : start - 1;
        }
    }

    /**
     * Selection change listener.
     *
     * Event is fired each time selection range changes or selection is created
     * or cleared.
     */
    public interface SelectionChangedListener {

        void selectionChanged(SelectionRange selection);
    }

    /**
     * Caret moved listener.
     *
     * Event is fired each time caret is moved or section is changed.
     */
    public interface CaretMovedListener {

        void caretMoved(CaretPosition caretPosition, Section section);
    }

    public static enum ViewMode {
        HEXADECIMAL(3), PREVIEW(1), DUAL(4);

        private int charsPerByte;

        ViewMode(int charsPerByte) {
            this.charsPerByte = charsPerByte;
        }
    }

    public static enum BackgroundMode {
        NONE, PLAIN, STRIPPED, GRIDDED
    }

    public static enum DecorationMode {
        NONE, LINES, BOX
    }

    public static enum Section {
        HEXADECIMAL, PREVIEW
    }

    public static enum EditationMode {
        OVERWRITE, INSERT, READ_ONLY
    }

    public static enum VerticalScrollMode {
        NONE, PER_LINE, PIXEL
    }

    public static enum HorizontalScrollMode {
        NONE, PER_CHAR, PIXEL
    }

    public static enum CharRenderingMode {
        AUTO, DYNAMIC, FIXED
    }

    public static enum CharAntialiasingMode {
        OFF, AUTO, DEFAULT, BASIC, GASP, LCD_HRGB, LCD_HBGR, LCD_VRGB, LCD_VBGR
    }

    /**
     * Precomputed dimensions for the hexadecimal editor.
     */
    private static class DimensionsCache {

        FontMetrics fontMetrics = null;
        int charWidth;
        int lineHeight;
        int charsPerByte;
        int bytesPerLine;
        boolean monospaced = false;

        final Rectangle hexadecimalRectangle = new Rectangle();
        int previewX;
        int bytesPerRect;
        int linesPerRect;
        int scrollBarThickness = 17;
    }

    public static class ScrollPosition {

        long scrollLinePosition = 0;
        int scrollLineOffset = 0;
        int scrollBytePosition = 0;
        int scrollByteOffset = 0;
    }

    private class HexMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

        private Cursor currentCursor = getCursor();
        private final Cursor defaultCursor = Cursor.getDefaultCursor();
        private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

        @Override
        public void mousePressed(MouseEvent me) {
            requestFocus();
            if (me.getButton() == MouseEvent.BUTTON1) {
                moveCaret(me, me.getModifiersEx());
                revealCursor();
            }
            mouseDown = true;
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            mouseDown = false;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            currentCursor = defaultCursor;
            setCursor(defaultCursor);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            updateMouseCursor(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            updateMouseCursor(e);
        }

        private void updateMouseCursor(MouseEvent e) {
            Cursor newCursor = defaultCursor;
            Rectangle rect = dimensionsCache.hexadecimalRectangle;
            if (e.getX() >= rect.x && e.getY() >= rect.y) {
                newCursor = textCursor;
            }

            if (newCursor != currentCursor) {
                currentCursor = newCursor;
                setCursor(newCursor);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            updateMouseCursor(me);
            if (mouseDown) {
                moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
                revealCursor();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isShiftDown() && horizontalScrollBar.isVisible()) {
                if (e.getWheelRotation() > 0) {
                    int visibleBytes = dimensionsCache.hexadecimalRectangle.width / (dimensionsCache.charWidth * dimensionsCache.charsPerByte);
                    int bytes = (int) dimensionsCache.bytesPerLine - visibleBytes;
                    if (scrollPosition.scrollBytePosition < bytes) {
                        if (scrollPosition.scrollBytePosition < bytes - MOUSE_SCROLL_LINES) {
                            scrollPosition.scrollBytePosition += MOUSE_SCROLL_LINES;
                        } else {
                            scrollPosition.scrollBytePosition = bytes;
                        }
                        updateScrollBars();
                    }
                } else if (scrollPosition.scrollBytePosition > 0) {
                    if (scrollPosition.scrollBytePosition > MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollBytePosition -= MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollBytePosition = 0;
                    }
                    updateScrollBars();
                }
            } else if (e.getWheelRotation() > 0) {
                long lines = (int) (data.getDataSize() / dimensionsCache.bytesPerLine) - dimensionsCache.linesPerRect;
                if (isShowHeader()) {
                    lines += 2;
                }
                if (scrollPosition.scrollLinePosition < lines) {
                    if (scrollPosition.scrollLinePosition < lines - MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollLinePosition += MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollLinePosition = lines;
                    }
                    updateScrollBars();
                }
            } else if (scrollPosition.scrollLinePosition > 0) {
                if (scrollPosition.scrollLinePosition > MOUSE_SCROLL_LINES) {
                    scrollPosition.scrollLinePosition -= MOUSE_SCROLL_LINES;
                } else {
                    scrollPosition.scrollLinePosition = 0;
                }
                updateScrollBars();
            }
        }
    }

    private class HexKeyListener extends KeyAdapter {

        public HexKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: {
                    moveLeft(e.getModifiersEx());
                    commandHandler.caretMoved();
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    moveRight(e.getModifiersEx());
                    commandHandler.caretMoved();
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_UP: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caret.isLowerHalf());
                            notifyCaretMoved();
                        }
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    commandHandler.caretMoved();
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesPerLine < dataSize
                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && !caretPosition.isLowerHalf())) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caret.isLowerHalf());
                            notifyCaretMoved();
                        }
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    commandHandler.caretMoved();
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_HOME: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    if (caretPosition.getDataPosition() > 0 || caret.isLowerHalf()) {
                        long targetPosition;
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            targetPosition = 0;
                        } else {
                            targetPosition = (caretPosition.getDataPosition() / bytesPerLine) * bytesPerLine;
                        }
                        caret.setCaretPosition(targetPosition);
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_END: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            caret.setCaretPosition(data.getDataSize());
                        } else if (activeSection == Section.HEXADECIMAL) {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize, true);
                        } else {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                        }
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_PAGE_UP: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = dimensionsCache.bytesPerLine * dimensionsCache.linesPerRect;
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesStep) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesStep, caret.isLowerHalf());
                        } else if (caretPosition.getDataPosition() >= dimensionsCache.bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() % dimensionsCache.bytesPerLine, caret.isLowerHalf());
                        }
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    if (scrollPosition.scrollLinePosition > dimensionsCache.linesPerRect) {
                        scrollPosition.scrollLinePosition -= dimensionsCache.linesPerRect;
                    }
                    revealCursor();
                    updateScrollBars();
                    break;
                }
                case KeyEvent.VK_PAGE_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = dimensionsCache.bytesPerLine * dimensionsCache.linesPerRect;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesStep < dataSize) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesStep, caret.isLowerHalf());
                        } else if (caretPosition.getDataPosition() + dimensionsCache.bytesPerLine < dataSize) {
                            caret.setCaretPosition(dataSize
                                    - (dataSize % dimensionsCache.bytesPerLine)
                                    + (caretPosition.getDataPosition() % dimensionsCache.bytesPerLine), caret.isLowerHalf());
                        }
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    if (scrollPosition.scrollLinePosition < data.getDataSize() / dimensionsCache.bytesPerLine - dimensionsCache.linesPerRect * 2) {
                        scrollPosition.scrollLinePosition += dimensionsCache.linesPerRect;
                    }
                    revealCursor();
                    updateScrollBars();
                    break;
                }
                case KeyEvent.VK_INSERT: {
                    if (editationMode != EditationMode.READ_ONLY) {
                        setEditationMode(editationMode == EditationMode.INSERT ? EditationMode.OVERWRITE : EditationMode.INSERT);
                        repaint();
                    }
                    break;
                }
                case KeyEvent.VK_TAB: {
                    if (viewMode == ViewMode.DUAL) {
                        activeSection = activeSection == Section.HEXADECIMAL ? Section.PREVIEW : Section.HEXADECIMAL;
                        if (activeSection == Section.PREVIEW) {
                            caret.setLowerHalf(false);
                        }
                        repaint();
                    }
                    break;
                }
                case KeyEvent.VK_DELETE: {
                    commandHandler.deletePressed();
                    break;
                }
                case KeyEvent.VK_BACK_SPACE: {
                    commandHandler.backSpacePressed();
                    break;
                }
                case KeyEvent.VK_ESCAPE: {
                    if (hasSelection()) {
                        clearSelection();
                    }
                    break;
                }
                default: {
                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
                        commandHandler.copy();
                    } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X) {
                        commandHandler.cut();
                    } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
                        commandHandler.paste();
                    } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_A) {
                        selectAll();
                    } else {
                        commandHandler.keyPressed(e.getKeyChar());
                    }
                }
            }
        }
    }

    private class HexComponentListener implements ComponentListener {

        public HexComponentListener() {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            computeDimensions();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
                scrollPosition.scrollLinePosition = verticalScrollBar.getValue();
            } else {
                scrollPosition.scrollLinePosition = verticalScrollBar.getValue() / dimensionsCache.lineHeight;
                scrollPosition.scrollLineOffset = verticalScrollBar.getValue() % dimensionsCache.lineHeight;
            }
            repaint();
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
                scrollPosition.scrollBytePosition = horizontalScrollBar.getValue();
            } else {
                scrollPosition.scrollBytePosition = horizontalScrollBar.getValue() / dimensionsCache.charWidth;
                scrollPosition.scrollByteOffset = horizontalScrollBar.getValue() % dimensionsCache.charWidth;
            }
            repaint();
        }
    }
}