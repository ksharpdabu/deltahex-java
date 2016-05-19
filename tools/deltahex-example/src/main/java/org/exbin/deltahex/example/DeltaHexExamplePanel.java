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
package org.exbin.deltahex.example;

import java.awt.BorderLayout;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.Hexadecimal;
import org.exbin.deltahex.HexadecimalCaret;

/**
 * Hexadecimal editor example panel.
 *
 * @version 0.1.0 2016/05/18
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaHexExamplePanel extends javax.swing.JPanel {

    private Hexadecimal hexadecimal;

    public DeltaHexExamplePanel() {
        initComponents();
    }

    public void setHexadecimal(final Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
        add(hexadecimal, BorderLayout.CENTER);
        viewModeComboBox.setSelectedIndex(hexadecimal.getViewMode().ordinal());
        backgroundModeComboBox.setSelectedIndex(hexadecimal.getBackgroundMode().ordinal());
        charRenderingComboBox.setSelectedIndex(hexadecimal.getCharRenderingMode().ordinal());
        charAntialiasingComboBox.setSelectedIndex(hexadecimal.getCharAntialiasingMode().ordinal());
        verticalScrollBarVisibilityComboBox.setSelectedIndex(hexadecimal.getVerticalScrollBarVisibility().ordinal());
        verticalScrollModeComboBox.setSelectedIndex(hexadecimal.getVerticalScrollMode().ordinal());
        horizontalScrollBarVisibilityComboBox.setSelectedIndex(hexadecimal.getHorizontalScrollBarVisibility().ordinal());
        horizontalScrollModeComboBox.setSelectedIndex(hexadecimal.getHorizontalScrollMode().ordinal());
        showLineNumbersCheckBox.setSelected(hexadecimal.isShowLineNumbers());
        showHeaderCheckBox.setSelected(hexadecimal.isShowHeader());
        showNonprintableCharactersCheckBox.setSelected(hexadecimal.isShowNonprintingCharacters());
        editableCheckBox.setSelected(hexadecimal.isEditable());
        wrapModeCheckBox.setSelected(hexadecimal.isWrapMode());
        lineLengthSpinner.setValue(hexadecimal.getLineLength());

        int decorationMode = hexadecimal.getDecorationMode();
        decoratorLineNumLineCheckBox.setSelected((decorationMode & Hexadecimal.DECORATION_LINENUM_HEX_LINE) > 0);
        decoratorSplitLineCheckBox.setSelected((decorationMode & Hexadecimal.DECORATION_HEX_PREVIEW_LINE) > 0);
        decoratorBoxCheckBox.setSelected((decorationMode & Hexadecimal.DECORATION_BOX) > 0);
        hexadecimal.addCaretMovedListener(new Hexadecimal.CaretMovedListener() {
            @Override
            public void caretMoved(CaretPosition caretPosition, HexadecimalCaret.Section section) {
                cursorPositionTextField.setText(String.valueOf(caretPosition.getDataPosition()));
                cursorPositionCheckBox.setSelected(caretPosition.isLowerHalf());
            }
        });
        hexadecimal.addSelectionChangedListener(new Hexadecimal.SelectionChangedListener() {
            @Override
            public void selectionChanged(Hexadecimal.SelectionRange selection) {
                if (selection != null) {
                    long first = hexadecimal.getSelection().getFirst();
                    selectionBeginTextField.setText(String.valueOf(first));
                    long last = hexadecimal.getSelection().getLast();
                    selectionEndTextField.setText(String.valueOf(last));
                } else {
                    selectionBeginTextField.setText("");
                    selectionEndTextField.setText("");
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPanel = new javax.swing.JPanel();
        viewModeScrollModeLabel = new javax.swing.JLabel();
        viewModeComboBox = new javax.swing.JComboBox<>();
        charRenderingScrollModeLabel = new javax.swing.JLabel();
        charRenderingComboBox = new javax.swing.JComboBox<>();
        showHeaderCheckBox = new javax.swing.JCheckBox();
        showLineNumbersCheckBox = new javax.swing.JCheckBox();
        editableCheckBox = new javax.swing.JCheckBox();
        wrapModeCheckBox = new javax.swing.JCheckBox();
        lineLengthLabel = new javax.swing.JLabel();
        lineLengthSpinner = new javax.swing.JSpinner();
        verticalScrollModeLabel = new javax.swing.JLabel();
        verticalScrollModeComboBox = new javax.swing.JComboBox<>();
        horizontalScrollModeLabel = new javax.swing.JLabel();
        horizontalScrollModeComboBox = new javax.swing.JComboBox<>();
        cursorPositionLabel = new javax.swing.JLabel();
        cursorPositionTextField = new javax.swing.JTextField();
        cursorPositionCheckBox = new javax.swing.JCheckBox();
        selectionPositionsLabel = new javax.swing.JLabel();
        selectionBeginTextField = new javax.swing.JTextField();
        selectionEndTextField = new javax.swing.JTextField();
        backgroundModeLabel = new javax.swing.JLabel();
        backgroundModeComboBox = new javax.swing.JComboBox<>();
        charAntialiasingScrollModeLabel = new javax.swing.JLabel();
        charAntialiasingComboBox = new javax.swing.JComboBox<>();
        verticalScrollBarVisibilityModeLabel = new javax.swing.JLabel();
        verticalScrollBarVisibilityComboBox = new javax.swing.JComboBox<>();
        horizontalScrollBarVisibilityLabel = new javax.swing.JLabel();
        horizontalScrollBarVisibilityComboBox = new javax.swing.JComboBox<>();
        decoratorLineNumLineCheckBox = new javax.swing.JCheckBox();
        decoratorSplitLineCheckBox = new javax.swing.JCheckBox();
        decoratorBoxCheckBox = new javax.swing.JCheckBox();
        showNonprintableCharactersCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        viewModeScrollModeLabel.setText("View Mode");

        viewModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "HEXADECIMAL", "PREVIEW", "DUAL" }));
        viewModeComboBox.setSelectedIndex(2);
        viewModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewModeComboBoxActionPerformed(evt);
            }
        });

        charRenderingScrollModeLabel.setText("Character Rendering");

        charRenderingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "DYNAMIC", "FIXED" }));
        charRenderingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charRenderingComboBoxActionPerformed(evt);
            }
        });

        showHeaderCheckBox.setText("Show Header");
        showHeaderCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showHeaderCheckBoxItemStateChanged(evt);
            }
        });

        showLineNumbersCheckBox.setText("Show Line Numbers");
        showLineNumbersCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showLineNumbersCheckBoxItemStateChanged(evt);
            }
        });

        editableCheckBox.setSelected(true);
        editableCheckBox.setText("Editable");
        editableCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                editableCheckBoxItemStateChanged(evt);
            }
        });

        wrapModeCheckBox.setText("Wrap Mode");
        wrapModeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                wrapModeCheckBoxItemStateChanged(evt);
            }
        });

        lineLengthLabel.setText("Bytes Per Line");

        lineLengthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineLengthSpinnerStateChanged(evt);
            }
        });

        verticalScrollModeLabel.setText("Vertical Scroll Mode");

        verticalScrollModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PER_LINE", "PIXEL" }));
        verticalScrollModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalScrollModeComboBoxActionPerformed(evt);
            }
        });

        horizontalScrollModeLabel.setText("Horizontal Scroll Mode");

        horizontalScrollModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PER_CHAR", "PIXEL" }));
        horizontalScrollModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalScrollModeComboBoxActionPerformed(evt);
            }
        });

        cursorPositionLabel.setText("Cursor Position");

        cursorPositionTextField.setEditable(false);

        cursorPositionCheckBox.setText("Half");
        cursorPositionCheckBox.setEnabled(false);

        selectionPositionsLabel.setText("Selection Start/End");

        selectionBeginTextField.setEditable(false);

        selectionEndTextField.setEditable(false);

        backgroundModeLabel.setText("Background Mode");

        backgroundModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "PLAIN", "STRIPPED", "GRIDDED" }));
        backgroundModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundModeComboBoxActionPerformed(evt);
            }
        });

        charAntialiasingScrollModeLabel.setText("Character Antialiasing");

        charAntialiasingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OFF", "AUTO", "DEFAULT", "BASIC", "GASP", "LCD_HRGB", "LCD_HBGR", "LCD_VRGB", "LCD_VBGR" }));
        charAntialiasingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charAntialiasingComboBoxActionPerformed(evt);
            }
        });

        verticalScrollBarVisibilityModeLabel.setText("Vertical Scrollbar");

        verticalScrollBarVisibilityComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NEVER", "IF_NEEDED", "ALWAYS" }));
        verticalScrollBarVisibilityComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalScrollBarVisibilityComboBoxActionPerformed(evt);
            }
        });

        horizontalScrollBarVisibilityLabel.setText("Horizontal Scrollbar");

        horizontalScrollBarVisibilityComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NEVER", "IF_NEEDED", "ALWAYS" }));
        horizontalScrollBarVisibilityComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalScrollBarVisibilityComboBoxActionPerformed(evt);
            }
        });

        decoratorLineNumLineCheckBox.setText("LineNum Line");
        decoratorLineNumLineCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorLineNumLineCheckBoxItemStateChanged(evt);
            }
        });

        decoratorSplitLineCheckBox.setText("Split Line");
        decoratorSplitLineCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorSplitLineCheckBoxItemStateChanged(evt);
            }
        });

        decoratorBoxCheckBox.setText("Area Box");
        decoratorBoxCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorBoxCheckBoxItemStateChanged(evt);
            }
        });

        showNonprintableCharactersCheckBox.setText("Show Nonprintable Characters");
        showNonprintableCharactersCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showNonprintableCharactersCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(showLineNumbersCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(showHeaderCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(wrapModeCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(decoratorLineNumLineCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(decoratorSplitLineCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(decoratorBoxCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(viewModeScrollModeLabel)
                                    .addComponent(viewModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(backgroundModeLabel)
                                    .addComponent(backgroundModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(verticalScrollModeLabel)
                                    .addComponent(verticalScrollModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(verticalScrollBarVisibilityModeLabel)
                                    .addComponent(verticalScrollBarVisibilityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(horizontalScrollBarVisibilityLabel)
                                    .addComponent(horizontalScrollBarVisibilityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(horizontalScrollModeLabel)
                                    .addComponent(horizontalScrollModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(editableCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lineLengthLabel)
                            .addComponent(lineLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addComponent(cursorPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cursorPositionCheckBox))
                            .addComponent(cursorPositionLabel)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(charRenderingScrollModeLabel)
                                    .addComponent(charRenderingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(selectionBeginTextField)
                                        .addComponent(selectionPositionsLabel)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(charAntialiasingScrollModeLabel)
                                    .addComponent(charAntialiasingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(selectionEndTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(showNonprintableCharactersCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(viewModeScrollModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(backgroundModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backgroundModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showHeaderCheckBox)
                    .addComponent(decoratorLineNumLineCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showLineNumbersCheckBox)
                    .addComponent(decoratorSplitLineCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(decoratorBoxCheckBox)
                    .addComponent(wrapModeCheckBox, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showNonprintableCharactersCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editableCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineLengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cursorPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cursorPositionCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionPositionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectionBeginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectionEndTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(charRenderingScrollModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(charRenderingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(charAntialiasingScrollModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(charAntialiasingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(horizontalScrollBarVisibilityLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(horizontalScrollBarVisibilityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(verticalScrollBarVisibilityModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(verticalScrollBarVisibilityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(horizontalScrollModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(horizontalScrollModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(verticalScrollModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(verticalScrollModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        add(settingsPanel, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void viewModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeComboBoxActionPerformed
        hexadecimal.setViewMode(Hexadecimal.ViewMode.values()[viewModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_viewModeComboBoxActionPerformed

    private void lineLengthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineLengthSpinnerStateChanged
        int value = (Integer) lineLengthSpinner.getValue();
        if (value > 0) {
            hexadecimal.setLineLength(value);
        }
    }//GEN-LAST:event_lineLengthSpinnerStateChanged

    private void verticalScrollModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalScrollModeComboBoxActionPerformed
        hexadecimal.setVerticalScrollMode(Hexadecimal.VerticalScrollMode.values()[verticalScrollModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_verticalScrollModeComboBoxActionPerformed

    private void horizontalScrollModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalScrollModeComboBoxActionPerformed
        hexadecimal.setHorizontalScrollMode(Hexadecimal.HorizontalScrollMode.values()[horizontalScrollModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_horizontalScrollModeComboBoxActionPerformed

    private void charRenderingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charRenderingComboBoxActionPerformed
        hexadecimal.setCharRenderingMode(Hexadecimal.CharRenderingMode.values()[charRenderingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_charRenderingComboBoxActionPerformed

    private void backgroundModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundModeComboBoxActionPerformed
        hexadecimal.setBackgroundMode(Hexadecimal.BackgroundMode.values()[backgroundModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_backgroundModeComboBoxActionPerformed

    private void charAntialiasingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charAntialiasingComboBoxActionPerformed
        hexadecimal.setCharAntialiasingMode(Hexadecimal.CharAntialiasingMode.values()[charAntialiasingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_charAntialiasingComboBoxActionPerformed

    private void verticalScrollBarVisibilityComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalScrollBarVisibilityComboBoxActionPerformed
        hexadecimal.setVerticalScrollBarVisibility(Hexadecimal.ScrollBarVisibility.values()[verticalScrollBarVisibilityComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_verticalScrollBarVisibilityComboBoxActionPerformed

    private void horizontalScrollBarVisibilityComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalScrollBarVisibilityComboBoxActionPerformed
        hexadecimal.setHorizontalScrollBarVisibility(Hexadecimal.ScrollBarVisibility.values()[horizontalScrollBarVisibilityComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_horizontalScrollBarVisibilityComboBoxActionPerformed

    private void decoratorLineNumLineCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorLineNumLineCheckBoxItemStateChanged
        int decorationMode = hexadecimal.getDecorationMode();
        boolean selected = decoratorLineNumLineCheckBox.isSelected();
        if (((decorationMode & Hexadecimal.DECORATION_LINENUM_HEX_LINE) > 0) != selected) {
            hexadecimal.setDecorationMode(decorationMode ^ Hexadecimal.DECORATION_LINENUM_HEX_LINE);
        }
    }//GEN-LAST:event_decoratorLineNumLineCheckBoxItemStateChanged

    private void decoratorSplitLineCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorSplitLineCheckBoxItemStateChanged
        int decorationMode = hexadecimal.getDecorationMode();
        boolean selected = decoratorSplitLineCheckBox.isSelected();
        if (((decorationMode & Hexadecimal.DECORATION_HEX_PREVIEW_LINE) > 0) != selected) {
            hexadecimal.setDecorationMode(decorationMode ^ Hexadecimal.DECORATION_HEX_PREVIEW_LINE);
        }
    }//GEN-LAST:event_decoratorSplitLineCheckBoxItemStateChanged

    private void decoratorBoxCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorBoxCheckBoxItemStateChanged
        int decorationMode = hexadecimal.getDecorationMode();
        boolean selected = decoratorBoxCheckBox.isSelected();
        if (((decorationMode & Hexadecimal.DECORATION_BOX) > 0) != selected) {
            hexadecimal.setDecorationMode(decorationMode ^ Hexadecimal.DECORATION_BOX);
        }
    }//GEN-LAST:event_decoratorBoxCheckBoxItemStateChanged

    private void showHeaderCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showHeaderCheckBoxItemStateChanged
        hexadecimal.setShowHeader(showHeaderCheckBox.isSelected());
    }//GEN-LAST:event_showHeaderCheckBoxItemStateChanged

    private void showLineNumbersCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showLineNumbersCheckBoxItemStateChanged
        hexadecimal.setShowLineNumbers(showLineNumbersCheckBox.isSelected());
    }//GEN-LAST:event_showLineNumbersCheckBoxItemStateChanged

    private void editableCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_editableCheckBoxItemStateChanged
        hexadecimal.setEditable(editableCheckBox.isSelected());
    }//GEN-LAST:event_editableCheckBoxItemStateChanged

    private void wrapModeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_wrapModeCheckBoxItemStateChanged
        hexadecimal.setWrapMode(wrapModeCheckBox.isSelected());
    }//GEN-LAST:event_wrapModeCheckBoxItemStateChanged

    private void showNonprintableCharactersCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showNonprintableCharactersCheckBoxItemStateChanged
        hexadecimal.setShowNonprintingCharacters(showNonprintableCharactersCheckBox.isSelected());
    }//GEN-LAST:event_showNonprintableCharactersCheckBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> backgroundModeComboBox;
    private javax.swing.JLabel backgroundModeLabel;
    private javax.swing.JComboBox<String> charAntialiasingComboBox;
    private javax.swing.JLabel charAntialiasingScrollModeLabel;
    private javax.swing.JComboBox<String> charRenderingComboBox;
    private javax.swing.JLabel charRenderingScrollModeLabel;
    private javax.swing.JCheckBox cursorPositionCheckBox;
    private javax.swing.JLabel cursorPositionLabel;
    private javax.swing.JTextField cursorPositionTextField;
    private javax.swing.JCheckBox decoratorBoxCheckBox;
    private javax.swing.JCheckBox decoratorLineNumLineCheckBox;
    private javax.swing.JCheckBox decoratorSplitLineCheckBox;
    private javax.swing.JCheckBox editableCheckBox;
    private javax.swing.JComboBox<String> horizontalScrollBarVisibilityComboBox;
    private javax.swing.JLabel horizontalScrollBarVisibilityLabel;
    private javax.swing.JComboBox<String> horizontalScrollModeComboBox;
    private javax.swing.JLabel horizontalScrollModeLabel;
    private javax.swing.JLabel lineLengthLabel;
    private javax.swing.JSpinner lineLengthSpinner;
    private javax.swing.JTextField selectionBeginTextField;
    private javax.swing.JTextField selectionEndTextField;
    private javax.swing.JLabel selectionPositionsLabel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JCheckBox showHeaderCheckBox;
    private javax.swing.JCheckBox showLineNumbersCheckBox;
    private javax.swing.JCheckBox showNonprintableCharactersCheckBox;
    private javax.swing.JComboBox<String> verticalScrollBarVisibilityComboBox;
    private javax.swing.JLabel verticalScrollBarVisibilityModeLabel;
    private javax.swing.JComboBox<String> verticalScrollModeComboBox;
    private javax.swing.JLabel verticalScrollModeLabel;
    private javax.swing.JComboBox<String> viewModeComboBox;
    private javax.swing.JLabel viewModeScrollModeLabel;
    private javax.swing.JCheckBox wrapModeCheckBox;
    // End of variables declaration//GEN-END:variables
}
