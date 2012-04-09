/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.breakpoints;

import org.eclipse.cdt.debug.ui.breakpoints.IFieldEditorFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public class PDAWatchpointFunctionFieldEditorFactory implements IFieldEditorFactory {
    
    public FieldEditor createFieldEditor(String name, String labelText, Composite parent) {
        return new StringFieldEditor(name, labelText, parent);
    }
}
