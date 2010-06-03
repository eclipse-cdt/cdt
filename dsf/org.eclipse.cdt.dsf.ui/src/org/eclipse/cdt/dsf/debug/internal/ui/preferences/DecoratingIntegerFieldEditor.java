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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * An {@link IntegerFieldEditor} with field decoration.
 * 
 * @since 1.1
 */
public class DecoratingIntegerFieldEditor extends IntegerFieldEditor {

	private ControlDecoration fDecoration;

	protected DecoratingIntegerFieldEditor() {
	}

    /**
     * Creates an integer field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
	public DecoratingIntegerFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

    /**
     * Creates an integer field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     * @param textLimit the maximum number of characters in the text.
     */
	public DecoratingIntegerFieldEditor(String name, String labelText, Composite parent, int textLimit) {
		super(name, labelText, parent, textLimit);
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
