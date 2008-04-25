/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class LabelFieldEditor extends StringFieldEditor{
    public LabelFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
        Text textControl = getTextControl();
        textControl.setEditable(false);
        textControl.setBackground(parent.getBackground());
    }
}
