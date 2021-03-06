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
package org.exbin.deltahex;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Insets;
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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * Also supports binary, octal and decimal codes.
 *
 * @version 0.1.0 2016/06/24
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends JComponent {

    public static final int NO_MODIFIER = 0;
    public static final int DECORATION_HEADER_LINE = 1;
    public static final int DECORATION_LINENUM_LINE = 2;
    public static final int DECORATION_PREVIEW_LINE = 4;
    public static final int DECORATION_BOX = 8;
    public static final int DECORATION_DEFAULT = DECORATION_PREVIEW_LINE | DECORATION_LINENUM_LINE | DECORATION_HEADER_LINE;
    public static final int MOUSE_SCROLL_LINES = 3;

    private int metaMask;

    private BinaryData data;
    private CodeAreaPainter painter;
    private CodeAreaCommandHandler commandHandler;
    private CodeAreaCaret caret;
    private SelectionRange selection;

    private ViewMode viewMode = ViewMode.DUAL;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;
    private BackgroundMode backgroundMode = BackgroundMode.STRIPPED;
    private Charset charset = Charset.defaultCharset();
    private int decorationMode = DECORATION_DEFAULT;
    private EditationMode editationMode = EditationMode.OVERWRITE;
    private CharRenderingMode charRenderingMode = CharRenderingMode.AUTO;
    private CharAntialiasingMode charAntialiasingMode = CharAntialiasingMode.AUTO;
    private HexCharactersCase hexCharactersCase = HexCharactersCase.UPPER;
    private final CodeAreaSpace headerSpace = new CodeAreaSpace(CodeAreaSpace.SpaceType.HALF_UNIT);
    private final CodeAreaSpace lineNumberSpace = new CodeAreaSpace();
    private final CodeAreaLineNumberLength lineNumberLength = new CodeAreaLineNumberLength();

    private int lineLength = 16;
    private int subFontSpace = 3;
    private boolean showHeader = true;
    private boolean showLineNumbers = true;
    private boolean mouseDown;
    private boolean editable = true;
    private boolean wrapMode = false;
    private boolean handleClipboard = true;
    private boolean showUnprintableCharacters = false;
    private boolean showShadowCursor = true;

    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollMode verticalScrollMode = VerticalScrollMode.PER_LINE;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollMode horizontalScrollMode = HorizontalScrollMode.PIXEL;
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private ScrollPosition scrollPosition = new ScrollPosition();

    /**
     * Component colors. Parent foreground and background are used for header
     * and line numbers section.
     */
    private final ColorsGroup mainColors = new ColorsGroup();
    private final ColorsGroup alternateColors = new ColorsGroup();
    private final ColorsGroup selectionColors = new ColorsGroup();
    private final ColorsGroup mirrorSelectionColors = new ColorsGroup();
    private Color cursorColor;
    private Color decorationLineColor;

    /**
     * Listeners.
     */
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();
    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();

    private final DimensionsCache dimensionsCache = new DimensionsCache();

    public CodeArea() {
        super();
        caret = new CodeAreaCaret(this);
        painter = new DefaultCodeAreaPainter(this);
        commandHandler = new DefaultCodeAreaCommandHandler(this);

        Color textColor = UIManager.getColor("TextArea.foreground");
        Color backgroundColor = UIManager.getColor("TextArea.background");
        super.setForeground(textColor);
        super.setBackground(createOddColor(backgroundColor));
        Color unprintablesColor = new Color(textColor.getRed(), (textColor.getGreen() + 128) % 256, textColor.getBlue());
        mainColors.setTextColor(textColor);
        mainColors.setBothBackgroundColors(backgroundColor);
        mainColors.setUnprintablesColor(unprintablesColor);
        alternateColors.setTextColor(textColor);
        alternateColors.setBothBackgroundColors(createOddColor(backgroundColor));
        alternateColors.setUnprintablesColor(unprintablesColor);
        Color selectionTextColor = UIManager.getColor("TextArea.selectionForeground");
        Color selectionBackgroundColor = UIManager.getColor("TextArea.selectionBackground");
        selectionColors.setTextColor(selectionTextColor);
        selectionColors.setBothBackgroundColors(selectionBackgroundColor);
        selectionColors.setUnprintablesColor(unprintablesColor);
        mirrorSelectionColors.setTextColor(selectionTextColor);
        int grayLevel = (selectionBackgroundColor.getRed() + selectionBackgroundColor.getGreen() + selectionBackgroundColor.getBlue()) / 3;
        mirrorSelectionColors.setBothBackgroundColors(new Color(grayLevel, grayLevel, grayLevel));
        mirrorSelectionColors.setUnprintablesColor(unprintablesColor);

        cursorColor = UIManager.getColor("TextArea.caretForeground");
        decorationLineColor = Color.GRAY;

        try {
            metaMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMask = java.awt.Event.CTRL_MASK;
        }
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
        addComponentListener(new CodeAreaComponentListener());

        CodeAreaMouseListener codeAreaMouseListener = new CodeAreaMouseListener();
        addMouseListener(codeAreaMouseListener);
        addMouseMotionListener(codeAreaMouseListener);
        addMouseWheelListener(codeAreaMouseListener);
        addKeyListener(new CodeAreaKeyListener());
    }

    @Override
    public void paintComponent(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        if (charAntialiasingMode != CharAntialiasingMode.OFF && g instanceof Graphics2D) {
            Object antialiasingHint = getAntialiasingHint((Graphics2D) g);
            ((Graphics2D) g).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    antialiasingHint);
        }

        if (dimensionsCache.fontMetrics == null) {
            computeFontMetrics();
        }

        painter.paintOverall(g);
        Rectangle hexRect = dimensionsCache.codeSectionRectangle;
        if (showHeader) {
            g.setClip(clipBounds.createIntersection(new Rectangle(hexRect.x, 0, hexRect.width, hexRect.y)));
            painter.paintHeader(g);
        }

        g.setClip(clipBounds.createIntersection(new Rectangle(0, hexRect.y, hexRect.x + hexRect.width, hexRect.height)));
        painter.paintBackground(g);
        if (showLineNumbers) {
            painter.paintLineNumbers(g);
            g.setClip(clipBounds.createIntersection(new Rectangle(hexRect.x, hexRect.y, hexRect.width, hexRect.height)));
        }

        painter.paintMainArea(g);

        if (hasFocus()) {
            caret.paint(g);
        }
        g.setClip(clipBounds);
    }

    private Object getAntialiasingHint(Graphics2D g) {
        Object antialiasingHint;
        switch (charAntialiasingMode) {
            case AUTO: {
                // TODO detect if display is LCD?
                if (g.getDeviceConfiguration().getDevice().getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
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

        return antialiasingHint;
    }

    public CodeAreaCaret getCaret() {
        return caret;
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        Rectangle hexRect = dimensionsCache.codeSectionRectangle;
        int codeDigits = getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        Point scrollPoint = getScrollPoint();
        int bytesPerLine = dimensionsCache.bytesPerLine;
        int cursorCharX = (me.getX() - hexRect.x + scrollPoint.x) / dimensionsCache.charWidth;
        int cursorLineY = (me.getY() - hexRect.y + scrollPoint.y) / dimensionsCache.lineHeight;
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == ViewMode.DUAL && cursorCharX < bytesPerLine * charsPerByte) || viewMode == ViewMode.CODE_MATRIX) {
            caret.setSection(Section.CODE_MATRIX);
            int bytePosition = cursorCharX % (codeType.maxDigits + 1);
            codeOffset = bytePosition;
            if (codeOffset >= codeType.maxDigits) {
                codeOffset = codeType.maxDigits - 1;
            }

            byteOnLine = cursorCharX / (codeType.maxDigits + 1);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            }
        } else {
            caret.setSection(Section.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == ViewMode.DUAL) {
                byteOnLine -= bytesPerLine * charsPerByte;
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine);
        long dataSize = data.getDataSize();
        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
        commandHandler.caretMoved();

        updateSelection(modifiers, caretPosition);
    }

    private void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : caretMovedListeners) {
            caretMovedListener.caretMoved(caret.getCaretPosition(), caret.getSection());
        }
    }

    public void notifyScrolled() {
        for (ScrollingListener scrollingListener : scrollingListeners) {
            scrollingListener.scrolled();
        }
    }

    public void notifyDataChanged() {
        if (caret.getDataPosition() > data.getDataSize()) {
            caret.setCaretPosition(0);
            notifyCaretMoved();
        }

        for (DataChangedListener dataChangedListener : dataChangedListeners) {
            dataChangedListener.dataChanged();
        }
    }

    public Point getScrollPoint() {
        return new Point(scrollPosition.scrollBytePosition * dimensionsCache.charWidth + scrollPosition.scrollByteOffset, (int) scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset);
    }

    public ScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }

    public void revealPosition(long position, Section section) {
        if (dimensionsCache.fontMetrics == null) {
            // Ignore if no font data is available
            return;
        }
        boolean scrolled = false;
        Rectangle hexRect = dimensionsCache.codeSectionRectangle;
        long caretLine = position / dimensionsCache.bytesPerLine;
        int codeDigits = getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        int positionByte;
        if (section == Section.CODE_MATRIX) {
            positionByte = (int) (position % dimensionsCache.bytesPerLine) * charsPerByte + caret.getCodeOffset();
        } else {
            positionByte = (int) (position % dimensionsCache.bytesPerLine);
            if (viewMode == ViewMode.DUAL) {
                positionByte += dimensionsCache.bytesPerLine * charsPerByte;
            }
        }

        if (caretLine <= scrollPosition.scrollLinePosition) {
            scrollPosition.scrollLinePosition = caretLine;
            scrollPosition.scrollLineOffset = 0;
            scrolled = true;
        } else if (caretLine >= scrollPosition.scrollLinePosition + dimensionsCache.linesPerRect) {
            scrollPosition.scrollLinePosition = caretLine - dimensionsCache.linesPerRect;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                scrollPosition.scrollLineOffset = dimensionsCache.lineHeight - (hexRect.height % dimensionsCache.lineHeight);
            } else {
                scrollPosition.scrollLinePosition++;
            }
            scrolled = true;
        }
        if (positionByte <= scrollPosition.scrollBytePosition) {
            scrollPosition.scrollBytePosition = positionByte;
            scrollPosition.scrollByteOffset = 0;
            scrolled = true;
        } else if (positionByte >= scrollPosition.scrollBytePosition + dimensionsCache.bytesPerRect) {
            scrollPosition.scrollBytePosition = positionByte - dimensionsCache.bytesPerRect;
            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                scrollPosition.scrollByteOffset = dimensionsCache.charWidth - (hexRect.width % dimensionsCache.charWidth);
            } else {
                scrollPosition.scrollBytePosition++;
            }
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void updateScrollBars() {
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            verticalScrollBar.setValue((int) scrollPosition.scrollLinePosition);
        } else {
            verticalScrollBar.setValue((int) (scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset));
        }

        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            horizontalScrollBar.setValue(scrollPosition.scrollBytePosition);
        } else {
            horizontalScrollBar.setValue(scrollPosition.scrollBytePosition * dimensionsCache.charWidth + scrollPosition.scrollByteOffset);
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

    public void moveRight(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() < data.getDataSize()) {
            if (caret.getSection() == Section.CODE_MATRIX) {
                int codeOffset = caret.getCodeOffset();
                if (caretPosition.getDataPosition() < data.getDataSize()) {
                    if (codeOffset < codeType.maxDigits - 1) {
                        caret.setCodeOffset(codeOffset + 1);
                    } else {
                        caret.setCaretPosition(caretPosition.getDataPosition() + 1, 0);
                    }
                    updateSelection(modifiers, caretPosition);
                    notifyCaretMoved();
                }
            } else {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        }
    }

    public void moveLeft(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caret.getSection() == Section.CODE_MATRIX) {
            int codeOffset = caret.getCodeOffset();
            if (codeOffset > 0) {
                caret.setCodeOffset(codeOffset - 1);
                updateSelection(modifiers, caretPosition);
            } else if (caretPosition.getDataPosition() > 0) {
                caret.setCaretPosition(caretPosition.getDataPosition() - 1, codeType.maxDigits - 1);
                updateSelection(modifiers, caretPosition);
            }
        } else if (caretPosition.getDataPosition() > 0) {
            caret.setCaretPosition(caretPosition.getDataPosition() - 1);
            updateSelection(modifiers, caretPosition);
        }
        notifyCaretMoved();
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

    public void addCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void removeCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    public void addEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.add(editationModeChangedListener);
    }

    public void removeEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.remove(editationModeChangedListener);
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public void addScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    public void removeScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    /**
     * Returns component area rectangle.
     *
     * Computed as component size minus border insets.
     *
     * @return rectangle of component area
     */
    public Rectangle getComponentRectangle() {
        return dimensionsCache.componentRectangle;
    }

    /**
     * Returns main hexadecimal area rectangle.
     *
     * @return rectangle of main hexadecimal area
     */
    public Rectangle getCodeSectionRectangle() {
        return dimensionsCache.codeSectionRectangle;
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

    public int getBytesPerLine() {
        return dimensionsCache.bytesPerLine;
    }

    public int getCharWidth() {
        return dimensionsCache.charWidth;
    }

    public FontMetrics getFontMetrics() {
        return dimensionsCache.fontMetrics;
    }

    /**
     * Returns header space size.
     *
     * @return header space size
     */
    public int getHeaderSpace() {
        return dimensionsCache.headerSpace;
    }

    /**
     * Returns line number space size.
     *
     * @return line number space size
     */
    public int getLineNumberSpace() {
        return dimensionsCache.lineNumberSpace;
    }

    /**
     * Returns current line number length in characters.
     *
     * @return line number length
     */
    public int getLineNumberLength() {
        return dimensionsCache.lineNumbersLength;
    }

    public BinaryData getData() {
        return data;
    }

    public void setData(BinaryData data) {
        this.data = data;
        notifyDataChanged();
        computeDimensions();
        repaint();
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
        repaint();
    }

    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        repaint();
    }

    public boolean isValidChar(char value) {
        return charset.canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = charset.encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        computeFontMetrics();
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        computeDimensions();
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
            dimensionsCache.monospaceFont = dimensionsCache.charWidth == dimensionsCache.fontMetrics.charWidth('i');
            int fontHeight = font.getSize();
            if (dimensionsCache.charWidth == 0) {
                dimensionsCache.charWidth = fontHeight;
            }
            dimensionsCache.lineHeight = fontHeight + subFontSpace;
            computeDimensions();
        }
    }

    public void computeDimensions() {
        if (dimensionsCache.fontMetrics == null) {
            return;
        }

        // TODO byte groups
        switch (viewMode) {
            case CODE_MATRIX: {
                dimensionsCache.charsPerByte = codeType.maxDigits + 1;
                break;
            }
            case TEXT_PREVIEW: {
                dimensionsCache.charsPerByte = 1;
                break;
            }
            case DUAL: {
                dimensionsCache.charsPerByte = codeType.maxDigits + 2;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected view mode " + viewMode.name());
        }

        boolean verticalScrollBarVisible;
        boolean horizontalScrollBarVisible;

        Insets insets = getInsets();
        Dimension size = getSize();
        Rectangle compRect = dimensionsCache.componentRectangle;
        compRect.x = insets.left;
        compRect.y = insets.top;
        compRect.width = size.width - insets.left - insets.right;
        compRect.height = size.height - insets.top - insets.bottom;

        switch (lineNumberLength.getLineNumberType()) {
            case AUTO: {
                double natLog = Math.log(getData().getDataSize());
                dimensionsCache.lineNumbersLength = (int) Math.ceil(natLog / positionCodeType.baseLog);
                if (dimensionsCache.lineNumbersLength == 0) {
                    dimensionsCache.lineNumbersLength = 1;
                }
                break;
            }
            case SPECIFIED: {
                dimensionsCache.lineNumbersLength = lineNumberLength.getLineNumberLength();
                break;
            }
        }

        int charsPerRect = computeCharsPerRect(compRect.width);
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
        CodeAreaSpace.SpaceType headerSpaceType = headerSpace.getSpaceType();
        switch (headerSpaceType) {
            case NONE: {
                dimensionsCache.headerSpace = 0;
                break;
            }
            case SPECIFIED: {
                dimensionsCache.headerSpace = headerSpace.getSpaceSize();
                break;
            }
            case QUARTER_UNIT: {
                dimensionsCache.headerSpace = dimensionsCache.lineHeight / 4;
                break;
            }
            case HALF_UNIT: {
                dimensionsCache.headerSpace = dimensionsCache.lineHeight / 2;
                break;
            }
            case ONE_UNIT: {
                dimensionsCache.headerSpace = dimensionsCache.lineHeight;
                break;
            }
            case ONE_AND_HALF_UNIT: {
                dimensionsCache.headerSpace = (int) (dimensionsCache.lineHeight * 1.5f);
                break;
            }
            case DOUBLE_UNIT: {
                dimensionsCache.headerSpace = dimensionsCache.lineHeight * 2;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected header space type " + headerSpaceType.name());
        }

        CodeAreaSpace.SpaceType lineNumberSpaceType = lineNumberSpace.getSpaceType();
        switch (lineNumberSpaceType) {
            case NONE: {
                dimensionsCache.lineNumberSpace = 0;
                break;
            }
            case SPECIFIED: {
                dimensionsCache.lineNumberSpace = lineNumberSpace.getSpaceSize();
                break;
            }
            case QUARTER_UNIT: {
                dimensionsCache.lineNumberSpace = dimensionsCache.charWidth / 4;
                break;
            }
            case HALF_UNIT: {
                dimensionsCache.lineNumberSpace = dimensionsCache.charWidth / 2;
                break;
            }
            case ONE_UNIT: {
                dimensionsCache.lineNumberSpace = dimensionsCache.charWidth;
                break;
            }
            case ONE_AND_HALF_UNIT: {
                dimensionsCache.lineNumberSpace = (int) (dimensionsCache.charWidth * 1.5f);
                break;
            }
            case DOUBLE_UNIT: {
                dimensionsCache.lineNumberSpace = dimensionsCache.charWidth * 2;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected line number space type " + lineNumberSpaceType.name());
        }

        Rectangle hexRect = dimensionsCache.codeSectionRectangle;
        hexRect.y = insets.top + (showHeader ? dimensionsCache.lineHeight + dimensionsCache.headerSpace : 0);
        hexRect.x = insets.left + (showLineNumbers ? dimensionsCache.charWidth * dimensionsCache.lineNumbersLength + dimensionsCache.lineNumberSpace : 0);

        if (verticalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
            verticalScrollBarVisible = lines > dimensionsCache.linesPerRect;
        } else {
            verticalScrollBarVisible = verticalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
        }
        if (verticalScrollBarVisible) {
            charsPerRect = computeCharsPerRect(compRect.x + compRect.width - dimensionsCache.scrollBarThickness);
            if (wrapMode) {
                bytesPerLine = charsPerRect / dimensionsCache.charsPerByte;
                if (bytesPerLine <= 0) {
                    bytesPerLine = 1;
                }
                lines = (int) (data.getDataSize() / bytesPerLine) + 1;
            }
        }

        dimensionsCache.bytesPerLine = bytesPerLine;

        int maxWidth = compRect.x + compRect.width - hexRect.x;
        if (verticalScrollBarVisible) {
            maxWidth -= dimensionsCache.scrollBarThickness;
        }

        if (horizontalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
            horizontalScrollBarVisible = dimensionsCache.bytesPerLine * dimensionsCache.charWidth * dimensionsCache.charsPerByte > maxWidth;
        } else {
            horizontalScrollBarVisible = horizontalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
        }
        if (horizontalScrollBarVisible) {
            dimensionsCache.linesPerRect = (hexRect.height - dimensionsCache.scrollBarThickness) / dimensionsCache.lineHeight;
        }

        hexRect.width = compRect.x + compRect.width - hexRect.x;
        if (verticalScrollBarVisible) {
            hexRect.width -= dimensionsCache.scrollBarThickness;
        }
        hexRect.height = compRect.y + compRect.height - hexRect.y;
        if (horizontalScrollBarVisible) {
            hexRect.height -= dimensionsCache.scrollBarThickness;
        }

        dimensionsCache.bytesPerRect = hexRect.width / dimensionsCache.charWidth;
        dimensionsCache.linesPerRect = hexRect.height / dimensionsCache.lineHeight;

        int codeDigits = getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;
        // Compute sections positions
        if (viewMode == ViewMode.CODE_MATRIX) {
            dimensionsCache.previewX = -1;
        } else {
            dimensionsCache.previewX = hexRect.x;
            if (viewMode == ViewMode.DUAL) {
                dimensionsCache.previewX += dimensionsCache.bytesPerLine * dimensionsCache.charWidth * charsPerByte;
            }
        }

        // Compute scrollbar positions
        boolean scrolled = false;
        verticalScrollBar.setVisible(verticalScrollBarVisible);
        if (verticalScrollBarVisible) {
            int verticalScrollBarHeight = compRect.y + compRect.height - hexRect.y;
            if (horizontalScrollBarVisible) {
                verticalScrollBarHeight -= dimensionsCache.scrollBarThickness - 2;
            }
            verticalScrollBar.setBounds(compRect.x + compRect.width - dimensionsCache.scrollBarThickness, hexRect.y, dimensionsCache.scrollBarThickness, verticalScrollBarHeight);

            int verticalVisibleAmount;
            int verticalMaximum = lines;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                verticalVisibleAmount = hexRect.height;
                verticalMaximum *= dimensionsCache.lineHeight;
            } else {
                verticalVisibleAmount = hexRect.height / dimensionsCache.lineHeight;
            }
            verticalScrollBar.setMaximum(verticalMaximum);
            verticalScrollBar.setVisibleAmount(verticalVisibleAmount);

            // Cap vertical scrolling
            if (verticalVisibleAmount < verticalMaximum) {
                long maxLineScroll = verticalMaximum - verticalVisibleAmount;
                if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                    long lineScroll = scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset;
                    if (lineScroll > maxLineScroll) {
                        scrollPosition.scrollLinePosition = maxLineScroll / dimensionsCache.lineHeight;
                        scrollPosition.scrollLineOffset = (int) (maxLineScroll % dimensionsCache.lineHeight);
                        scrolled = true;
                    }
                } else {
                    long lineScroll = scrollPosition.scrollLinePosition;
                    if (lineScroll > maxLineScroll) {
                        scrollPosition.scrollLinePosition = maxLineScroll;
                        scrolled = true;
                    }
                }
            }
        } else if (scrollPosition.scrollLinePosition > 0 || scrollPosition.scrollLineOffset > 0) {
            scrollPosition.scrollLinePosition = 0;
            scrollPosition.scrollLineOffset = 0;
            scrolled = true;
        }

        horizontalScrollBar.setVisible(horizontalScrollBarVisible);
        if (horizontalScrollBarVisible) {
            int horizontalScrollBarWidth = compRect.x + compRect.width - hexRect.x;
            if (verticalScrollBarVisible) {
                horizontalScrollBarWidth -= dimensionsCache.scrollBarThickness - 2;
            }
            horizontalScrollBar.setBounds(hexRect.x, compRect.y + compRect.height - dimensionsCache.scrollBarThickness, horizontalScrollBarWidth, dimensionsCache.scrollBarThickness);

            int horizontalVisibleAmount;
            int horizontalMaximum = dimensionsCache.bytesPerLine * dimensionsCache.charsPerByte;
            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                horizontalVisibleAmount = hexRect.width;
                horizontalMaximum *= dimensionsCache.charWidth;
            } else {
                horizontalVisibleAmount = hexRect.width / dimensionsCache.charWidth;
            }
            horizontalScrollBar.setMaximum(horizontalMaximum);
            horizontalScrollBar.setVisibleAmount(horizontalVisibleAmount);

            // Cap horizontal scrolling
            int maxByteScroll = horizontalMaximum - horizontalVisibleAmount;
            if (horizontalVisibleAmount < horizontalMaximum) {
                if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                    int byteScroll = scrollPosition.scrollBytePosition * dimensionsCache.charWidth + scrollPosition.scrollByteOffset;
                    if (byteScroll > maxByteScroll) {
                        scrollPosition.scrollBytePosition = maxByteScroll / dimensionsCache.charWidth;
                        scrollPosition.scrollByteOffset = maxByteScroll % dimensionsCache.charWidth;
                        scrolled = true;
                    }
                } else {
                    int byteScroll = scrollPosition.scrollBytePosition;
                    if (byteScroll > maxByteScroll) {
                        scrollPosition.scrollBytePosition = maxByteScroll;
                        scrolled = true;
                    }
                }
            }
        } else if (scrollPosition.scrollBytePosition > 0 || scrollPosition.scrollByteOffset > 0) {
            scrollPosition.scrollBytePosition = 0;
            scrollPosition.scrollByteOffset = 0;
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    private int computeCharsPerRect(int width) {
        if (showLineNumbers) {
            width -= dimensionsCache.charWidth * dimensionsCache.lineNumbersLength + getLineNumberSpace();
        }

        return width / dimensionsCache.charWidth;
    }

    public ColorsGroup getMainColors() {
        return new ColorsGroup(mainColors);
    }

    public ColorsGroup getAlternateColors() {
        return new ColorsGroup(alternateColors);
    }

    public ColorsGroup getSelectionColors() {
        return new ColorsGroup(selectionColors);
    }

    public ColorsGroup getMirrorSelectionColors() {
        return new ColorsGroup(mirrorSelectionColors);
    }

    public void setMainColors(ColorsGroup colorsGroup) {
        mainColors.setColors(colorsGroup);
        repaint();
    }

    public void setAlternateColors(ColorsGroup colorsGroup) {
        alternateColors.setColors(colorsGroup);
        repaint();
    }

    public void setSelectionColors(ColorsGroup colorsGroup) {
        selectionColors.setColors(colorsGroup);
        repaint();
    }

    public void setMirrorSelectionColors(ColorsGroup colorsGroup) {
        mirrorSelectionColors.setColors(colorsGroup);
        repaint();
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
        repaint();
    }

    public Color getDecorationLineColor() {
        return decorationLineColor;
    }

    public void setDecorationLineColor(Color decorationLineColor) {
        this.decorationLineColor = decorationLineColor;
        repaint();
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == ViewMode.CODE_MATRIX) {
            caret.setSection(Section.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == ViewMode.TEXT_PREVIEW) {
            caret.setSection(Section.TEXT_PREVIEW);
            notifyCaretMoved();
        }
        computeDimensions();
        repaint();
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        computeDimensions();
        repaint();
    }

    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    public void setPositionCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = positionCodeType;
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

    public int getDecorationMode() {
        return decorationMode;
    }

    public void setDecorationMode(int decorationMode) {
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
        return caret.getSection();
    }

    public void setActiveSection(Section activeSection) {
        caret.setSection(activeSection);
        revealCursor();
        repaint();
    }

    public EditationMode getEditationMode() {
        return editationMode;
    }

    public void setEditationMode(EditationMode editationMode) {
        boolean chaged = editationMode != this.editationMode;
        this.editationMode = editationMode;
        if (chaged) {
            for (EditationModeChangedListener listener : editationModeChangedListeners) {
                listener.editationModeChanged(editationMode);
            }
            repaint();
        }
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

    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    public void setHandleClipboard(boolean handleClipboard) {
        this.handleClipboard = handleClipboard;
    }

    public boolean isShowUnprintableCharacters() {
        return showUnprintableCharacters;
    }

    public void setShowUnprintableCharacters(boolean showUnprintableCharacters) {
        this.showUnprintableCharacters = showUnprintableCharacters;
        repaint();
    }

    public boolean isShowShadowCursor() {
        return showShadowCursor;
    }

    public void setShowShadowCursor(boolean showShadowCursor) {
        this.showShadowCursor = showShadowCursor;
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

    public boolean isMonospaceFontDetected() {
        return dimensionsCache.monospaceFont;
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

    public HexCharactersCase getHexCharactersCase() {
        return hexCharactersCase;
    }

    public void setHexCharactersCase(HexCharactersCase hexCharactersCase) {
        this.hexCharactersCase = hexCharactersCase;
        painter.setHexCharacters(hexCharactersCase == HexCharactersCase.LOWER ? CodeAreaUtils.LOWER_HEX_CODES : CodeAreaUtils.UPPER_HEX_CODES);
        repaint();
    }

    public CodeAreaSpace.SpaceType getHeaderSpaceType() {
        return headerSpace.getSpaceType();
    }

    public void setHeaderSpaceType(CodeAreaSpace.SpaceType spaceType) {
        if (spaceType == null) {
            throw new NullPointerException();
        }
        headerSpace.setSpaceType(spaceType);
        computeDimensions();
        repaint();
    }

    public int getHeaderSpaceSize() {
        return headerSpace.getSpaceSize();
    }

    public void setHeaderSpaceSize(int spaceSize) {
        if (spaceSize < 0) {
            throw new IllegalArgumentException("Negative space size is not valid");
        }
        headerSpace.setSpaceSize(spaceSize);
        computeDimensions();
        repaint();
    }

    public CodeAreaSpace.SpaceType getLineNumberSpaceType() {
        return lineNumberSpace.getSpaceType();
    }

    public void setLineNumberSpaceType(CodeAreaSpace.SpaceType spaceType) {
        if (spaceType == null) {
            throw new NullPointerException();
        }
        lineNumberSpace.setSpaceType(spaceType);
        computeDimensions();
        repaint();
    }

    public int getLineNumberSpaceSize() {
        return lineNumberSpace.getSpaceSize();
    }

    public void setLineNumberSpaceSize(int spaceSize) {
        if (spaceSize < 0) {
            throw new IllegalArgumentException("Negative space size is not valid");
        }
        lineNumberSpace.setSpaceSize(spaceSize);
        computeDimensions();
        repaint();
    }

    public CodeAreaLineNumberLength.LineNumberType getLineNumberType() {
        return lineNumberLength.getLineNumberType();
    }

    public void setLineNumberType(CodeAreaLineNumberLength.LineNumberType lineNumberType) {
        if (lineNumberType == null) {
            throw new NullPointerException("Line number type cannot be null");
        }
        lineNumberLength.setLineNumberType(lineNumberType);
        computeDimensions();
        repaint();
    }

    public int getLineNumberSpecifiedLength() {
        return lineNumberLength.getLineNumberLength();
    }

    public void setLineNumberSpecifiedLength(int lineNumberSize) {
        if (lineNumberSize < 1) {
            throw new NullPointerException("Line number type cannot be less then 1");
        }
        lineNumberLength.setLineNumberLength(lineNumberSize);
        computeDimensions();
        repaint();
    }

    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        computeDimensions();
        updateScrollBars();
    }

    public VerticalScrollMode getVerticalScrollMode() {
        return verticalScrollMode;
    }

    public void setVerticalScrollMode(VerticalScrollMode verticalScrollMode) {
        this.verticalScrollMode = verticalScrollMode;
        long linePosition = scrollPosition.scrollLinePosition;
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            scrollPosition.scrollLineOffset = 0;
        }
        computeDimensions();
        scrollPosition.scrollLinePosition = linePosition;
        updateScrollBars();
        notifyScrolled();
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        computeDimensions();
        updateScrollBars();
    }

    public HorizontalScrollMode getHorizontalScrollMode() {
        return horizontalScrollMode;
    }

    public void setHorizontalScrollMode(HorizontalScrollMode horizontalScrollMode) {
        this.horizontalScrollMode = horizontalScrollMode;
        int bytePosition = scrollPosition.scrollBytePosition;
        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            scrollPosition.scrollByteOffset = 0;
        }
        computeDimensions();
        scrollPosition.scrollBytePosition = bytePosition;
        updateScrollBars();
        notifyScrolled();
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
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

    private static Color createOddColor(Color color) {
        return new Color(
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    private static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
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

        /**
         * Returns first data position of the selection.
         *
         * @return data position
         */
        public long getFirst() {
            return end >= start ? start : end;
        }

        /**
         * Returns last data position of the selection.
         *
         * @return data position
         */
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

    /**
     * Editation mode change listener.
     *
     * Event is fired each time editation mode is changed.
     */
    public interface EditationModeChangedListener {

        void editationModeChanged(EditationMode editationMode);
    }

    /**
     * Data changed listener.
     *
     * Event is fired each time data is modified.
     */
    public interface DataChangedListener {

        void dataChanged();
    }

    /**
     * Scrolling listener.
     *
     * Event is fired each time component is scrolled.
     */
    public interface ScrollingListener {

        void scrolled();
    }

    /**
     * Component supports showing numerical codes or textual preview, or both.
     */
    public static enum ViewMode {
        DUAL, CODE_MATRIX, TEXT_PREVIEW;
    }

    public static enum CodeType {
        BINARY(8), OCTAL(3), DECIMAL(3), HEXADECIMAL(2);

        private int maxDigits;

        CodeType(int maxDigits) {
            this.maxDigits = maxDigits;
        }

        /**
         * Maximum number of digits per single byte.
         *
         * @return number of digits
         */
        public int getMaxDigits() {
            return maxDigits;
        }
    }

    public static enum PositionCodeType {
        OCTAL(8), DECIMAL(10), HEXADECIMAL(16);

        int base;
        double baseLog;

        private PositionCodeType(int base) {
            this.base = base;
            baseLog = Math.log(base);
        }

        public int getBase() {
            return base;
        }

        public double getBaseLog() {
            return baseLog;
        }
    }

    public static enum Section {
        CODE_MATRIX, TEXT_PREVIEW
    }

    public static enum BackgroundMode {
        NONE, PLAIN, STRIPPED, GRIDDED
    }

    public static enum EditationMode {
        OVERWRITE, INSERT, READ_ONLY
    }

    public static enum VerticalScrollMode {
        PER_LINE, PIXEL
    }

    public static enum HorizontalScrollMode {
        PER_CHAR, PIXEL
    }

    public static enum ScrollBarVisibility {
        NEVER, IF_NEEDED, ALWAYS
    }

    /**
     * Character rendering mode.
     *
     * AUTO - Centers characters if width is not default and detects monospace
     * fonts to render characters as string if possible
     *
     * LINE_AT_ONCE - Render sequence of characters from top left corner of the
     * line ignoring character width. It's fastest, but render correctly only
     * for monospaced fonts and charsets where all characters have same width
     *
     * TOP_LEFT - Render each character from top left corner of it's position
     *
     * CENTER - Centers each character in it's area
     */
    public static enum CharRenderingMode {
        AUTO, LINE_AT_ONCE, TOP_LEFT, CENTER
    }

    public static enum CharAntialiasingMode {
        OFF, AUTO, DEFAULT, BASIC, GASP, LCD_HRGB, LCD_HBGR, LCD_VRGB, LCD_VBGR
    }

    public static enum HexCharactersCase {
        LOWER, UPPER
    }

    /**
     * Precomputed dimensions for the component.
     */
    private static class DimensionsCache {

        FontMetrics fontMetrics = null;
        int charWidth;
        int lineHeight;
        int bytesPerLine;
        int charsPerByte;
        int lineNumbersLength;
        boolean monospaceFont = false;

        /**
         * Component area without border insets.
         */
        final Rectangle componentRectangle = new Rectangle();
        /**
         * Space between header and code area.
         */
        int headerSpace;
        /**
         * Space between line numbers and code area.
         */
        int lineNumberSpace;
        /**
         * Space between main code area and preview.
         */
        int previewSpace;

        /**
         * Main data area.
         *
         * Component area without header, line numbers and scrollbars.
         */
        final Rectangle codeSectionRectangle = new Rectangle();
        int previewX;
        int bytesPerRect;
        int linesPerRect;
        int scrollBarThickness = 17;
    }

    /**
     * Scrolling position.
     */
    public static class ScrollPosition {

        long scrollLinePosition = 0;
        int scrollLineOffset = 0;
        int scrollBytePosition = 0;
        int scrollByteOffset = 0;

        public long getScrollLinePosition() {
            return scrollLinePosition;
        }

        public int getScrollLineOffset() {
            return scrollLineOffset;
        }

        public int getScrollBytePosition() {
            return scrollBytePosition;
        }

        public int getScrollByteOffset() {
            return scrollByteOffset;
        }
    }

    /**
     * Set of colors for different sections of component rendering.
     */
    public static class ColorsGroup {

        private Color textColor;
        private Color backgroundColor;
        private Color unprintablesColor;
        private Color unprintablesBackgroundColor;

        public ColorsGroup() {
        }

        /**
         * Copy constructor.
         *
         * @param colorsGroup colors group
         */
        public ColorsGroup(ColorsGroup colorsGroup) {
            setColorsFromGroup(colorsGroup);
        }

        private void setColorsFromGroup(ColorsGroup colorsGroup) {
            textColor = colorsGroup.getTextColor();
            backgroundColor = colorsGroup.getBackgroundColor();
            unprintablesColor = colorsGroup.getUnprintablesColor();
            unprintablesBackgroundColor = colorsGroup.getUnprintablesBackgroundColor();
        }

        public Color getTextColor() {
            return textColor;
        }

        public void setTextColor(Color textColor) {
            this.textColor = textColor;
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public Color getUnprintablesColor() {
            return unprintablesColor;
        }

        public void setUnprintablesColor(Color unprintablesColor) {
            this.unprintablesColor = unprintablesColor;
        }

        public Color getUnprintablesBackgroundColor() {
            return unprintablesBackgroundColor;
        }

        public void setUnprintablesBackgroundColor(Color unprintablesBackgroundColor) {
            this.unprintablesBackgroundColor = unprintablesBackgroundColor;
        }

        public void setBothBackgroundColors(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            this.unprintablesBackgroundColor = backgroundColor;
        }

        public void setColors(ColorsGroup colorsGroup) {
            setColorsFromGroup(colorsGroup);
        }

        public Color getColor(ColorType colorType) {
            switch (colorType) {
                case TEXT:
                    return textColor;
                case BACKGROUND:
                    return backgroundColor;
                case UNPRINTABLES:
                    return unprintablesColor;
                case UNPRINTABLES_BACKGROUND:
                    return unprintablesBackgroundColor;
                default:
                    throw new IllegalStateException();
            }
        }

        public void setColor(ColorType colorType, Color color) {
            switch (colorType) {
                case TEXT: {
                    textColor = color;
                    break;
                }
                case BACKGROUND: {
                    backgroundColor = color;
                    break;
                }
                case UNPRINTABLES: {
                    unprintablesColor = color;
                    break;
                }
                case UNPRINTABLES_BACKGROUND: {
                    unprintablesBackgroundColor = color;
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * Enumeration of color types in group.
     */
    public static enum ColorType {
        TEXT,
        BACKGROUND,
        UNPRINTABLES,
        UNPRINTABLES_BACKGROUND
    }

    private class CodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

        private Cursor currentCursor = getCursor();
        private final Cursor defaultCursor = Cursor.getDefaultCursor();
        private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

        @Override
        public void mousePressed(MouseEvent me) {
            requestFocus();
            if (isEnabled() && me.getButton() == MouseEvent.BUTTON1) {
                moveCaret(me, me.getModifiersEx());
                revealCursor();
                mouseDown = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            mouseDown = false;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            currentCursor = defaultCursor;
            setCursor(defaultCursor);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            updateMouseCursor(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateMouseCursor(e);
        }

        private void updateMouseCursor(MouseEvent e) {
            Cursor newCursor = defaultCursor;
            Rectangle hexRect = dimensionsCache.codeSectionRectangle;
            if (e.getX() >= hexRect.x && e.getY() >= hexRect.y) {
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
            if (isEnabled() && mouseDown) {
                moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
                revealCursor();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!isEnabled()) {
                return;
            }

            if (e.isShiftDown() && horizontalScrollBar.isVisible()) {
                if (e.getWheelRotation() > 0) {
                    int visibleBytes = dimensionsCache.codeSectionRectangle.width / (dimensionsCache.charWidth * dimensionsCache.charsPerByte);
                    int bytes = dimensionsCache.bytesPerLine - visibleBytes;
                    if (scrollPosition.scrollBytePosition < bytes) {
                        if (scrollPosition.scrollBytePosition < bytes - MOUSE_SCROLL_LINES) {
                            scrollPosition.scrollBytePosition += MOUSE_SCROLL_LINES;
                        } else {
                            scrollPosition.scrollBytePosition = bytes;
                        }
                        updateScrollBars();
                        notifyScrolled();
                    }
                } else if (scrollPosition.scrollBytePosition > 0) {
                    if (scrollPosition.scrollBytePosition > MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollBytePosition -= MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollBytePosition = 0;
                    }
                    updateScrollBars();
                    notifyScrolled();
                }
            } else if (e.getWheelRotation() > 0) {
                long lines = (int) (data.getDataSize() / dimensionsCache.bytesPerLine);
                if (lines * dimensionsCache.bytesPerLine < data.getDataSize()) {
                    lines++;
                }
                lines -= dimensionsCache.linesPerRect;
                if (scrollPosition.scrollLinePosition < lines) {
                    if (scrollPosition.scrollLinePosition < lines - MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollLinePosition += MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollLinePosition = lines;
                    }
                    updateScrollBars();
                    notifyScrolled();
                }
            } else if (scrollPosition.scrollLinePosition > 0) {
                if (scrollPosition.scrollLinePosition > MOUSE_SCROLL_LINES) {
                    scrollPosition.scrollLinePosition -= MOUSE_SCROLL_LINES;
                } else {
                    scrollPosition.scrollLinePosition = 0;
                }
                updateScrollBars();
                notifyScrolled();
            }
        }
    }

    private class CodeAreaKeyListener extends KeyAdapter {

        public CodeAreaKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() != 0xffff) {
                commandHandler.keyPressed(e.getKeyChar());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!isEnabled()) {
                return;
            }

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
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caret.getCodeOffset());
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
                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && caretPosition.getCodeOffset() == 0)) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caret.getCodeOffset());
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
                    if (caretPosition.getDataPosition() > 0 || caret.getCodeOffset() > 0) {
                        long targetPosition;
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            targetPosition = 0;
                        } else {
                            targetPosition = (caretPosition.getDataPosition() / bytesPerLine) * bytesPerLine;
                        }
                        caret.setCaretPosition(targetPosition);
                        commandHandler.caretMoved();
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
                        } else if (caret.getSection() == Section.CODE_MATRIX) {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize, newPosition < dataSize ? codeType.maxDigits - 1 : 0);
                        } else {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                        }
                        commandHandler.caretMoved();
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
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesStep, caret.getCodeOffset());
                        } else if (caretPosition.getDataPosition() >= dimensionsCache.bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() % dimensionsCache.bytesPerLine, caret.getCodeOffset());
                        }
                        commandHandler.caretMoved();
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    if (scrollPosition.scrollLinePosition > dimensionsCache.linesPerRect) {
                        scrollPosition.scrollLinePosition -= dimensionsCache.linesPerRect;
                    }
                    revealCursor();
                    updateScrollBars();
                    notifyScrolled();
                    break;
                }
                case KeyEvent.VK_PAGE_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = dimensionsCache.bytesPerLine * dimensionsCache.linesPerRect;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesStep < dataSize) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesStep, caret.getCodeOffset());
                        } else if (caretPosition.getDataPosition() + dimensionsCache.bytesPerLine < dataSize) {
                            caret.setCaretPosition(dataSize
                                    - (dataSize % dimensionsCache.bytesPerLine)
                                    + (caretPosition.getDataPosition() % dimensionsCache.bytesPerLine), caret.getCodeOffset());
                        }
                        commandHandler.caretMoved();
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    if (scrollPosition.scrollLinePosition < data.getDataSize() / dimensionsCache.bytesPerLine - dimensionsCache.linesPerRect * 2) {
                        scrollPosition.scrollLinePosition += dimensionsCache.linesPerRect;
                    }
                    revealCursor();
                    updateScrollBars();
                    notifyScrolled();
                    break;
                }
                case KeyEvent.VK_INSERT: {
                    if (editationMode != EditationMode.READ_ONLY) {
                        setEditationMode(editationMode == EditationMode.INSERT ? EditationMode.OVERWRITE : EditationMode.INSERT);
                    }
                    break;
                }
                case KeyEvent.VK_TAB: {
                    if (viewMode == ViewMode.DUAL) {
                        Section activeSection = caret.getSection() == Section.CODE_MATRIX ? Section.TEXT_PREVIEW : Section.CODE_MATRIX;
                        if (activeSection == Section.TEXT_PREVIEW) {
                            caret.setCodeOffset(0);
                        }
                        caret.setSection(activeSection);
                        revealCursor();
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
                    if (handleClipboard) {
                        if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_C) {
                            commandHandler.copy();
                            break;
                        } else if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_X) {
                            commandHandler.cut();
                            break;
                        } else if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_V) {
                            commandHandler.paste();
                            break;
                        } else if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_A) {
                            selectAll();
                            break;
                        }
                    }
                }
            }
        }
    }

    private class CodeAreaComponentListener implements ComponentListener {

        public CodeAreaComponentListener() {
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
            notifyScrolled();
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
            notifyScrolled();
        }
    }
}
