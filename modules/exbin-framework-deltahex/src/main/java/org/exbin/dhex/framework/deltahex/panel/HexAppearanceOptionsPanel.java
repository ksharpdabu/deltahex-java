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
package org.exbin.dhex.framework.deltahex.panel;

import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import org.exbin.framework.gui.options.api.OptionsPanel;
import org.exbin.framework.gui.options.api.OptionsPanel.ModifiedOptionListener;
import org.exbin.framework.gui.utils.ActionUtils;

/**
 * Hexadecimal appearance options panel.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class HexAppearanceOptionsPanel extends javax.swing.JPanel implements OptionsPanel {
    public static final String PREFERENCES_TEXT_WORD_WRAPPING = "textAppearance.wordWrap";

    private ModifiedOptionListener modifiedOptionListener;
    private ResourceBundle resourceBundle = ActionUtils.getResourceBundleByClass(HexAppearanceOptionsPanel.class);
    private HexAppearancePanelFrame frame;

    public HexAppearanceOptionsPanel(HexAppearancePanelFrame frame) {
        this.frame = frame;

        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        wordWrapCheckBox = new javax.swing.JCheckBox();

        setName("Form"); // NOI18N

        wordWrapCheckBox.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/exbin/dhex/framework/deltahex/panel/resources/HexAppearanceOptionsPanel"); // NOI18N
        wordWrapCheckBox.setText(bundle.getString("HexAppearanceOptionsPanel.wordWrapCheckBox.text")); // NOI18N
        wordWrapCheckBox.setName("wordWrapCheckBox"); // NOI18N
        wordWrapCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                wordWrapCheckBoxjCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wordWrapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wordWrapCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void wordWrapCheckBoxjCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_wordWrapCheckBoxjCheckBoxItemStateChanged
        setModified(true);
    }//GEN-LAST:event_wordWrapCheckBoxjCheckBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox wordWrapCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public List<OptionsPanel.PathItem> getPath() {
        return null;
    }

    @Override
    public void loadFromPreferences(Preferences preferences) {
        wordWrapCheckBox.setSelected(Boolean.parseBoolean(preferences.get(PREFERENCES_TEXT_WORD_WRAPPING, Boolean.FALSE.toString())));
    }

    @Override
    public void saveToPreferences(Preferences preferences) {
        preferences.put(PREFERENCES_TEXT_WORD_WRAPPING, Boolean.toString(wordWrapCheckBox.isSelected()));
    }

    @Override
    public void applyPreferencesChanges() {
        frame.setWordWrapMode(wordWrapCheckBox.isSelected());
    }

    private void setModified(boolean b) {
        if (modifiedOptionListener != null) {
            modifiedOptionListener.wasModified();
        }
    }

    @Override
    public void setModifiedOptionListener(ModifiedOptionListener listener) {
        modifiedOptionListener = listener;
    }
}