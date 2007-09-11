/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This class behaves in the same way as its parent, 
 * but gives public access to its Text widget, and 
 * valueChanged() can be called outside.
 * 
 * It allows to add extra listeners to Text widget.
 */
public class StringFieldEditorM extends StringFieldEditor {
    public StringFieldEditorM(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    public Text getTextControl() {
        return super.getTextControl();
    }
    
    public void valueChanged() {
    	super.valueChanged();
    }
}
