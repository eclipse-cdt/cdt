/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class StringFieldEditorM extends StringFieldEditor {
    public StringFieldEditorM(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    @Override
	public Text getTextControl() {
        return super.getTextControl();
    }
       
    @Override
	public void valueChanged() {
        setPresentsDefaultValue(false);
        boolean oldState = super.isValid();
        super.refreshValidState();
        if (super.isValid() != oldState) {
			fireStateChanged(IS_VALID, oldState, super.isValid());
		}
        String newValue = this.getTextControl().getText();             
        if (!newValue.equals(oldValue)) {  
        	String oldValueTmp =oldValue;
        	oldValue = newValue;        	
            try {
				fireValueChanged(VALUE, oldValueTmp, newValue);
			} catch (Exception e) {
				oldValue = oldValueTmp;
				ManagedBuilderUIPlugin.log(e);
			}            
        }
    }
}
