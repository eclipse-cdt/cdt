/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.preferences;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A {@link StringFieldEditor} with field decoration.
 * @since 1.1
 */
public class DecoratingStringFieldEditor extends StringFieldEditor {

	private ControlDecoration fDecoration;

	protected DecoratingStringFieldEditor() {
	}

    /**
     * Creates a string field editor of unlimited width.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
	public DecoratingStringFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

    /**
     * Creates a string field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param width the width of the text input field in characters,
     *  or <code>UNLIMITED</code> for no limit
     * @param parent the parent of the field editor's control
     */
	public DecoratingStringFieldEditor(String name, String labelText, int width, Composite parent) {
		super(name, labelText, width, parent);
	}

    /**
     * Creates a string field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param width the width of the text input field in characters,
     *  or <code>UNLIMITED</code> for no limit
     * @param strategy either <code>VALIDATE_ON_KEY_STROKE</code> to perform
     *  on the fly checking (the default), or <code>VALIDATE_ON_FOCUS_LOST</code> to
     *  perform validation only after the text has been typed in
     * @param parent the parent of the field editor's control
     */
	public DecoratingStringFieldEditor(String name, String labelText, int width, int strategy, Composite parent) {
		super(name, labelText, width, strategy, parent);
	}

	@Override
	public Text getTextControl(Composite parent) {
		Text control = super.getTextControl(parent);
		if (fDecoration == null) {
			fDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
			FieldDecoration errorDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
			fDecoration.setImage(errorDecoration.getImage());
			fDecoration.setDescriptionText(getErrorMessage());

			// validate on focus gain
            control.addFocusListener(new FocusAdapter() {
                @Override
				public void focusGained(FocusEvent e) {
                    refreshValidState();
                }
            });
		}
		return control;
	}
	
	@Override
	protected void showErrorMessage(String msg) {
		super.showErrorMessage(msg);
		if (fDecoration != null) {
			fDecoration.setDescriptionText(msg);
			fDecoration.show();
		}
	}

	@Override
	protected void clearErrorMessage() {
		super.clearErrorMessage();
		if (fDecoration != null) {
			fDecoration.hide();
		}
	}
}
